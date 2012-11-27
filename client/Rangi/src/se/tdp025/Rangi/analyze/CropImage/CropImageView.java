/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.tdp025.Rangi.analyze.CropImage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;

import java.util.ArrayList;

public class CropImageView extends ImageViewTouchBase {
    private static final String TAG = "RANGI_CropImageView";

    public ArrayList<HighlightView> mHighlightViews = new ArrayList<HighlightView>();
    public HighlightView mMotionHighlightView = null;
    float mLastX, mLastY;
    int mMotionEdge;

    /* NEW */
    protected ScaleGestureDetector mScaleDetector;
    protected ScaleGestureDetector mScaleCropDetector;
    protected GestureDetector mGestureDetector;
    protected GestureDetector.OnGestureListener mGestureListener;
    protected ScaleGestureDetector.OnScaleGestureListener mScaleListener;
    protected ScaleGestureDetector.OnScaleGestureListener mCropScaleListener;
    protected int mTouchSlop;
    protected boolean mDoubleTapEnabled = true;
    protected boolean mScaleEnabled = true;
    protected boolean mScrollEnabled = true;
    public float mCurrentScaleFactor;
    protected float mScaleFactor;
    protected int mDoubleTapDirection;
    private Context mContext;

    private OnImageViewTouchDoubleTapListener mDoubleTapListener;


    /* NEW */

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void init() {
        super.init();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mGestureListener = new GestureListener();
        mScaleListener = new ScaleListener();
        mCropScaleListener = new ScaleCropListener();


        mScaleDetector = new ScaleGestureDetector( getContext(), mScaleListener );
        mScaleCropDetector = new ScaleGestureDetector(getContext(), mCropScaleListener);

        mGestureDetector = new GestureDetector( getContext(), mGestureListener, null, true );

        mCurrentScaleFactor = 1f;
        mDoubleTapDirection = 1;
        setLongClickable(true);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top,
                            int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mBitmapDisplayed.getBitmap() != null) {
            for (HighlightView hv : mHighlightViews) {
                hv.mMatrix.set(getImageMatrix());
                hv.invalidate();
                if (hv.mIsFocused) {
                    centerBasedOnHighlightView(hv);
                }
            }
        }
    }



    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomIn() {
        super.zoomIn();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomOut() {
        super.zoomOut();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }

    // According to the event's position, change the focus to the first
    // hitting cropping rectangle.
    private void recomputeFocus(MotionEvent event) {
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.setFocus(false);
            hv.invalidate();
        }

        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            int edge = hv.getHit(event.getX(), event.getY());
            if (edge != HighlightView.GROW_NONE) {
                if (!hv.hasFocus()) {
                    hv.setFocus(true);
                    hv.invalidate();
                }
                break;
            }
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        CropImage cropImage = (CropImage) mContext;
        if (cropImage.mSaving) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (cropImage.mWaitingToPick) {
                    recomputeFocus(event);
                } else {
                    Log.v(TAG, "Touch!");

                    for (int i = 0; i < mHighlightViews.size(); i++) {
                        HighlightView hv = mHighlightViews.get(i);
                        int edge = hv.getHit(event.getX(), event.getY());
                        if (edge != HighlightView.GROW_NONE) {

                            mMotionEdge = edge;
                            mMotionHighlightView = hv;
                            mLastX = event.getX();
                            mLastY = event.getY();
                            mMotionHighlightView.setMode(
                                    (edge == HighlightView.MOVE)
                                            ? HighlightView.ModifyMode.Move
                                            : HighlightView.ModifyMode.Grow);
                            break;
                        }
                    }

                }
                break;
            case MotionEvent.ACTION_UP:

                if (cropImage.mWaitingToPick) {
                    for (int i = 0; i < mHighlightViews.size(); i++) {
                        HighlightView hv = mHighlightViews.get(i);
                        if (hv.hasFocus()) {
                            cropImage.mCrop = hv;
                            for (int j = 0; j < mHighlightViews.size(); j++) {
                                if (j == i) {
                                    continue;
                                }
                                mHighlightViews.get(j).setHidden(true);
                            }
                            centerBasedOnHighlightView(hv);
                            ((CropImage) mContext).mWaitingToPick = false;
                            return true;
                        }
                    }
                } else if (mMotionHighlightView != null) {
                    centerBasedOnHighlightView(mMotionHighlightView);
                    mMotionHighlightView.setMode(
                            HighlightView.ModifyMode.None);
                }
                mMotionHighlightView = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (cropImage.mWaitingToPick) {
                    recomputeFocus(event);
                } else if (mMotionHighlightView != null) {
                    setLongClickable(false);
                    mMotionHighlightView.handleMotion(mMotionEdge,
                            event.getX() - mLastX,
                            event.getY() - mLastY);
                    mLastX = event.getX();
                    mLastY = event.getY();

                    if (true) {
                        // This section of code is optional. It has some user
                        // benefit in that moving the crop rectangle against
                        // the edge of the screen causes scrolling but it means
                        // that the crop rectangle is no longer fixed under
                        // the user's finger.
                        ensureVisible(mMotionHighlightView);
                    }
                    return true;
                }
                break;
        }


        mScaleDetector.onTouchEvent( event );

        if ( !mScaleDetector.isInProgress() ) mGestureDetector.onTouchEvent( event );

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                // center(true, true);
                break;
            case MotionEvent.ACTION_MOVE:
                // if we're not zoomed then there's no point in even allowing
                // the user to move the image around.  This call to center puts
                // it back to the normalized location (with false meaning don't
                // animate).
                if (getScale() == 1F) {
                    //center(true, true);
                }
                break;
        }

        setLongClickable(true);
        return true;
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView hv) {
        Rect r = hv.mDrawRect;

        int panDeltaX1 = Math.max(0, mLeft - r.left);
        int panDeltaX2 = Math.min(0, mRight - r.right);

        int panDeltaY1 = Math.max(0, mTop - r.top);
        int panDeltaY2 = Math.min(0, mBottom - r.bottom);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private void centerBasedOnHighlightView(HighlightView hv) {
        Rect drawRect = hv.mDrawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();

        float z1 = thisWidth / width * .6F;
        float z2 = thisHeight / height * .6F;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);
        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float [] coordinates = new float[] {hv.mCropRect.centerX(),
                    hv.mCropRect.centerY()};
            getImageMatrix().mapPoints(coordinates);
            //zoomTo(zoom, coordinates[0], coordinates[1], 300F);
        }

        ensureVisible(hv);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mHighlightViews.size(); i++) {
            mHighlightViews.get(i).draw(canvas);
        }
    }

    public void add(HighlightView hv) {
        mHighlightViews.add(hv);
        invalidate();
    }

    /* NEW */
    public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
        if ( !mScrollEnabled ) return false;

        if ( e1 == null || e2 == null ) return false;
        if ( e1.getPointerCount() > 1 || e2.getPointerCount() > 1 ) return false;
        if ( mScaleDetector.isInProgress() ) return false;
        if ( getScale() == 1f ) return false;

        Log.d( TAG, "onScroll: " + distanceX + ", " + distanceY );
        scrollBy( -distanceX, -distanceY );
        invalidate();
        return true;
    }

    public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
        if ( !mScrollEnabled ) return false;

        if ( e1.getPointerCount() > 1 || e2.getPointerCount() > 1 ) return false;
        if ( mScaleDetector.isInProgress() ) return false;

        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        if ( Math.abs( velocityX ) > 800 || Math.abs( velocityY ) > 800 ) {
            scrollBy( diffX / 2, diffY / 2, 300 );
            invalidate();
            return true;
        }
        return false;
    }

    private void onLongPressed(MotionEvent e) {
        Log.v(TAG, "LongPressed");
        Activity activity = (Activity)mContext;
        activity.openOptionsMenu();
    }

    @Override
    protected void onZoom( float scale ) {
        super.onZoom( scale );
        if ( !mScaleDetector.isInProgress() ) mCurrentScaleFactor = scale;

    }

    @Override
    protected void onZoomAnimationCompleted( float scale ) {
        super.onZoomAnimationCompleted( scale );
        if ( !mScaleDetector.isInProgress() ) mCurrentScaleFactor = scale;

        if( scale < getMinZoom() ) {
            zoomTo( getMinZoom(), 50 );
        }
    }

    protected float onDoubleTapPost( float scale, float maxZoom ) {
        if ( mDoubleTapDirection == 1 ) {
            if ( ( scale + ( mScaleFactor * 2 ) ) <= maxZoom ) {
                return scale + mScaleFactor;
            } else {
                mDoubleTapDirection = -1;
                return maxZoom;
            }
        } else {
            mDoubleTapDirection = 1;
            return 1f;
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap( MotionEvent e ) {
            Log.i(TAG, "onDoubleTap. double tap enabled? " + mDoubleTapEnabled);
            if ( mDoubleTapEnabled ) {
                float scale = getScale();
                float targetScale = scale;
                targetScale = onDoubleTapPost( scale, getMaxZoom() );
                targetScale = Math.min( getMaxZoom(), Math.max( targetScale, getMinZoom() ) );
                mCurrentScaleFactor = targetScale;
                zoomTo( targetScale, e.getX(), e.getY(), DEFAULT_ANIMATION_DURATION );
                invalidate();
            }

            if( null != mDoubleTapListener ){
                mDoubleTapListener.onDoubleTap();
            }

            return super.onDoubleTap( e );
        }

        @Override
        public void onLongPress( MotionEvent e ) {
            Log.v(TAG, "onLongPress");
            if ( isLongClickable() ) {
                if ( !mScaleDetector.isInProgress() ) {
                    setPressed( true );
                    performLongClick();
                    CropImageView.this.onLongPressed(e);
                }
            }
        }

        @Override
        public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
            return CropImageView.this.onScroll( e1, e2, distanceX, distanceY );
        }

        @Override
        public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
            return CropImageView.this.onFling( e1, e2, velocityX, velocityY );
        }
    }



    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @SuppressWarnings("unused")
        @Override
        public boolean onScale( ScaleGestureDetector detector ) {
            Log.d( TAG, "onScale" );
            float span = detector.getCurrentSpan() - detector.getPreviousSpan();
            float targetScale = mCurrentScaleFactor * detector.getScaleFactor();
            if ( mScaleEnabled ) {
                targetScale = Math.min( getMaxZoom(), Math.max( targetScale, getMinZoom()-0.1f ) );
                zoomTo( targetScale, detector.getFocusX(), detector.getFocusY() );
                mCurrentScaleFactor = Math.min( getMaxZoom(), Math.max( targetScale, getMinZoom()-1.0f ) );
                mDoubleTapDirection = 1;
                invalidate();
                return true;
            }
            return false;
        }
    }

    public class ScaleCropListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @SuppressWarnings("unused")
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            Log.v(TAG, "Zoooooooom!");
            return false;
        }
    }

    public interface OnImageViewTouchDoubleTapListener {
        void onDoubleTap();
    }

}