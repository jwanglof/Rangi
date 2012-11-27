/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

abstract class ImageViewTouchBase extends ImageView {

    @SuppressWarnings("unused")
    private static final String TAG = "ImageViewTouchBase";

    // This is the base transformation which is used to show the image
    // initially.  The current computation for this shows the image in
    // it's entirety, letterboxing as needed.  One could choose to
    // show the image as cropped instead.
    //
    // This matrix is recomputed when we go from the thumbnail image to
    // the full size image.
    protected Matrix mBaseMatrix = new Matrix();

    // This is the supplementary transformation which reflects what
    // the user has done in terms of zooming and panning.
    //
    // This matrix remains the same when we go from the thumbnail image
    // to the full size image.
    protected Matrix mSuppMatrix = new Matrix();

    // This is the final matrix which is computed as the concatentation
    // of the base matrix and the supplementary matrix.
    private final Matrix mDisplayMatrix = new Matrix();

    // Temporary buffer used for getting the values out of a matrix.
    private final float[] mMatrixValues = new float[9];

    // The current bitmap being displayed.
    final protected RotateBitmap mBitmapDisplayed = new RotateBitmap(null);

    int mThisWidth = -1, mThisHeight = -1;


    int mLeft;

    int mRight;

    int mTop;

    int mBottom;

    protected Handler mHandler = new Handler();

    private Runnable mOnLayoutRunnable = null;


    protected Cubic mCubicEasing = new Cubic();

    protected float mMaxZoom;
    protected float mMinZoom = -1;
    protected boolean mFitToScreen = false;
    final protected float MAX_ZOOM = 2.0f;
    final protected int DEFAULT_ANIMATION_DURATION = 200;

    protected RectF mBitmapRect = new RectF();
    protected RectF mCenterRect = new RectF();
    protected RectF mScrollRect = new RectF();




    public ImageViewTouchBase(Context context) {
        super(context);
        init();
    }

    public ImageViewTouchBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void clear() {
        setImageBitmapResetBase(null, true);
    }

    protected void init() {
        setScaleType(ScaleType.MATRIX);
    }

    public void setFitToScreen(boolean value) {
        if (value != mFitToScreen) {
            mFitToScreen = value;
            requestLayout();
        }
    }

    public void setMinZoom(float value) {
        Log.d(TAG,"minZoom: " + value);
        mMinZoom = value;         }

    @Override //?
    protected void onLayout(boolean changed, int left, int top,
                            int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeft = left;
        mRight = right;
        mTop = top;
        mBottom = bottom;
        mThisWidth = right - left;
        mThisHeight = bottom - top;
        Runnable r = mOnLayoutRunnable;
        if (r != null) {
            mOnLayoutRunnable = null;
            r.run();
        }
        if (mBitmapDisplayed.getBitmap() != null) {
            getProperBaseMatrix(mBitmapDisplayed, mBaseMatrix);
            setImageMatrix(getImageViewMatrix());
        }
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap, 0);
    }

    private void setImageBitmap(Bitmap bitmap, int rotation) {
        super.setImageBitmap(bitmap);
        Drawable d = getDrawable();
        if (d != null) {
            d.setDither(true);
        }

        Bitmap old = mBitmapDisplayed.getBitmap();
        mBitmapDisplayed.setBitmap(bitmap);
        mBitmapDisplayed.setRotation(rotation);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getScale() > 1.0f) {
            // If we're zoomed in, pressing Back jumps out to show the entire
            // image, otherwise Back returns the user to the gallery.
            zoomTo(1.0f);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    // This function changes bitmap, reset base matrix according to the size
    // of the bitmap, and optionally reset the supplementary matrix.
    public void setImageBitmapResetBase(final Bitmap bitmap,
                                        final boolean resetSupp) {
        setImageRotateBitmapResetBase(new RotateBitmap(bitmap), resetSupp);
    }

    public void setImageRotateBitmapResetBase(final RotateBitmap bitmap,
                                              final boolean resetSupp) {
        final int viewWidth = getWidth();

        if (viewWidth <= 0)  {
            mOnLayoutRunnable = new Runnable() {
                public void run() {
                    setImageRotateBitmapResetBase(bitmap, resetSupp);
                }
            };
            return;
        }

        if (bitmap.getBitmap() != null) {
            getProperBaseMatrix(bitmap, mBaseMatrix);
            setImageBitmap(bitmap.getBitmap(), bitmap.getRotation());
        } else {
            mBaseMatrix.reset();
            setImageBitmap(null);
        }

        if (resetSupp) {
            mSuppMatrix.reset();
        }
        setImageMatrix(getImageViewMatrix());
        mMaxZoom = maxZoom();
    }

    protected float minZoom() {
        return 1F;
    }

    public float getMaxZoom() {
        if ( mMaxZoom < 1 ) {
            mMaxZoom = maxZoom();
        }
        return mMaxZoom;
    }

    public float getMinZoom() {
        if ( mMinZoom < 0 ) {
            mMinZoom = minZoom();
        }
        return mMinZoom;
    }


    // Center as much as possible in one or both axis.  Centering is
    // defined as follows:  if the image is scaled down below the
    // view's dimensions then center it (literally).  If the image
    // is scaled larger than the view and is translated out of view
    // then translate it back into view (i.e. eliminate black bars).
    public void center(boolean horizontal, boolean vertical) {
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }

        Matrix m = getImageViewMatrix();

        RectF rect = new RectF(0, 0,
                mBitmapDisplayed.getBitmap().getWidth(),
                mBitmapDisplayed.getBitmap().getHeight());

        m.mapRect(rect);

        float height = rect.height();
        float width  = rect.width();

        float deltaX = 0, deltaY = 0;

        if (vertical) {
            int viewHeight = getHeight();
            if (height < viewHeight) {
                deltaY = (viewHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < viewHeight) {
                deltaY = getHeight() - rect.bottom;
            }
        }

        if (horizontal) {
            int viewWidth = getWidth();
            if (width < viewWidth) {
                deltaX = (viewWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < viewWidth) {
                deltaX = viewWidth - rect.right;
            }
        }

        postTranslate(deltaX, deltaY);
        setImageMatrix(getImageViewMatrix());
    }




    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    // Get the scale factor out of the matrix.
    public float getScale(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    public float getScale() {
        return getScale(mSuppMatrix);
    }

    // Setup the base matrix so that the image is centered and scaled properly.
    private void getProperBaseMatrix(RotateBitmap bitmap, Matrix matrix) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();
        int rotation = bitmap.getRotation();
        matrix.reset();

        // We limit up-scaling to 2x otherwise the result may look bad if it's
        // a small icon.
        float widthScale = Math.min(viewWidth / w, 2.0f);
        float heightScale = Math.min(viewHeight / h, 2.0f);
        float scale = Math.min(widthScale, heightScale);

        matrix.postConcat(bitmap.getRotateMatrix());
        matrix.postScale(scale, scale);

        matrix.postTranslate(
                (viewWidth  - w * scale) / 2F,
                (viewHeight - h * scale) / 2F);
    }

    // Combine the base matrix and the supp matrix to make the final matrix.
    protected Matrix getImageViewMatrix() {
        // The final matrix is computed as the concatentation of the base matrix
        // and the supplementary matrix.
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(mSuppMatrix);
        return mDisplayMatrix;
    }

    static final float SCALE_RATE = 1.25F;

    // Sets the maximum zoom, which is a scale relative to the base matrix. It
    // is calculated to show the image at 400% zoom regardless of screen or
    // image orientation. If in the future we decode the full 3 megapixel image,
    // rather than the current 1024x768, this should be changed down to 200%.
    protected float maxZoom() {
        if (mBitmapDisplayed.getBitmap() == null) {
            return 1F;
        }

        float fw = (float) mBitmapDisplayed.getWidth()  / (float) mThisWidth;
        float fh = (float) mBitmapDisplayed.getHeight() / (float) mThisHeight;
        float max = Math.max(fw, fh) * 4;
        return max;
    }

    protected void zoomTo(float scale, float centerX, float centerY) {
        if (scale > mMaxZoom) {
            scale = mMaxZoom;
        }

        float oldScale = getScale();
        float deltaScale = scale / oldScale;

        mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }

    public void zoomTo( float scale, float durationMs ) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;
        zoomTo( scale, cx, cy, durationMs );
    }

    protected void zoomTo(float scale) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        zoomTo(scale, cx, cy);
    }

    protected void zoomIn() {
        zoomIn(SCALE_RATE);
    }

    protected void zoomOut() {
        zoomOut(SCALE_RATE);
    }

    protected void zoomIn(float rate) {
        if (getScale() >= mMaxZoom) {
            return;     // Don't let the user zoom into the molecular level.
        }
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        mSuppMatrix.postScale(rate, rate, cx, cy);
        setImageMatrix(getImageViewMatrix());
    }

    protected void zoomOut(float rate) {
        if (mBitmapDisplayed.getBitmap() == null) {
            return;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        // Zoom out to at most 1x.
        Matrix tmp = new Matrix(mSuppMatrix);
        tmp.postScale(1F / rate, 1F / rate, cx, cy);

        if (getScale(tmp) < 1F) {
            mSuppMatrix.setScale(1F, 1F, cx, cy);
        } else {
            mSuppMatrix.postScale(1F / rate, 1F / rate, cx, cy);
        }
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }

    protected void postTranslate(float dx, float dy) {
        mSuppMatrix.postTranslate(dx, dy);
        setImageMatrix( getImageViewMatrix() );
    }

    protected void panBy(float dx, float dy) {
        center( true, true );
        postTranslate(dx, dy);
        setImageMatrix(getImageViewMatrix());
    }

    protected void panBy( double dx, double dy ) {
        RectF rect = getBitmapRect();
        mScrollRect.set( (float) dx, (float) dy, 0, 0 );
        updateRect( rect, mScrollRect );
        postTranslate( mScrollRect.left, mScrollRect.top );
        center( true, true );
    }

    public void scrollBy( double x, double y ) {
        panBy( x, y );
    }


    protected RectF getBitmapRect() {
        return getBitmapRect( mSuppMatrix );
    }

    protected RectF getBitmapRect( Matrix supportMatrix ) {
        final Drawable drawable = getDrawable();

        if ( drawable == null ) return null;
        Matrix m = getImageViewMatrix( supportMatrix );
        mBitmapRect.set( 0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight() );
        m.mapRect( mBitmapRect );
        return mBitmapRect;
    }

    public Matrix getImageViewMatrix( Matrix supportMatrix ) {
        mDisplayMatrix.set( mBaseMatrix );
        mDisplayMatrix.postConcat( supportMatrix );
        return mDisplayMatrix;
    }

    protected void updateRect( RectF bitmapRect, RectF scrollRect ) {
        float width = getWidth();
        float height = getHeight();

        if ( bitmapRect.top >= 0 && bitmapRect.bottom <= height ) scrollRect.top = 0;
        if ( bitmapRect.left >= 0 && bitmapRect.right <= width ) scrollRect.left = 0;
        if ( bitmapRect.top + scrollRect.top >= 0 && bitmapRect.bottom > height ) scrollRect.top = (int) ( 0 - bitmapRect.top );
        if ( bitmapRect.bottom + scrollRect.top <= ( height - 0 ) && bitmapRect.top < 0 )
            scrollRect.top = (int) ( ( height - 0 ) - bitmapRect.bottom );
        if ( bitmapRect.left + scrollRect.left >= 0 ) scrollRect.left = (int) ( 0 - bitmapRect.left );
        if ( bitmapRect.right + scrollRect.left <= ( width - 0 ) ) scrollRect.left = (int) ( ( width - 0 ) - bitmapRect.right );
    }

    protected void scrollBy( float distanceX, float distanceY, final double durationMs ) {
        final double dx = distanceX;
        final double dy = distanceY;
        final long startTime = System.currentTimeMillis();
        mHandler.post( new Runnable() {

            double old_x = 0;
            double old_y = 0;

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                double currentMs = Math.min( durationMs, now - startTime );
                double x = mCubicEasing.easeOut( currentMs, 0, dx, durationMs );
                double y = mCubicEasing.easeOut( currentMs, 0, dy, durationMs );
                panBy( ( x - old_x ), ( y - old_y ) );
                old_x = x;
                old_y = y;
                if ( currentMs < durationMs ) {
                    mHandler.post( this );
                } else {
                    RectF centerRect = getCenter( mSuppMatrix, true, true );
                    if ( centerRect.left != 0 || centerRect.top != 0 ) scrollBy( centerRect.left, centerRect.top );
                }
            }
        } );
    }

    protected RectF getCenter( Matrix supportMatrix, boolean horizontal, boolean vertical ) {
        final Drawable drawable = getDrawable();

        if ( drawable == null ) return new RectF( 0, 0, 0, 0 );

        mCenterRect.set( 0, 0, 0, 0 );
        RectF rect = getBitmapRect( supportMatrix );
        float height = rect.height();
        float width = rect.width();
        float deltaX = 0, deltaY = 0;
        if ( vertical ) {
            int viewHeight = getHeight();
            if ( height < viewHeight ) {
                deltaY = ( viewHeight - height ) / 2 - rect.top;
            } else if ( rect.top > 0 ) {
                deltaY = -rect.top;
            } else if ( rect.bottom < viewHeight ) {
                deltaY = getHeight() - rect.bottom;
            }
        }
        if ( horizontal ) {
            int viewWidth = getWidth();
            if ( width < viewWidth ) {
                deltaX = ( viewWidth - width ) / 2 - rect.left;
            } else if ( rect.left > 0 ) {
                deltaX = -rect.left;
            } else if ( rect.right < viewWidth ) {
                deltaX = viewWidth - rect.right;
            }
        }
        mCenterRect.set( deltaX, deltaY, 0, 0 );
        return mCenterRect;
    }

    protected void zoomTo( float scale, float centerX, float centerY, final float durationMs ) {
        if ( scale > getMaxZoom() ) scale = getMaxZoom();
        final long startTime = System.currentTimeMillis();
        final float oldScale = getScale();

        final float deltaScale = scale - oldScale;

        Matrix m = new Matrix( mSuppMatrix );
        m.postScale( scale, scale, centerX, centerY );
        RectF rect = getCenter( m, true, true );

        final float destX = centerX + rect.left * scale;
        final float destY = centerY + rect.top * scale;

        mHandler.post( new Runnable() {

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                float currentMs = Math.min( durationMs, now - startTime );
                float newScale = (float) mCubicEasing.easeInOut( currentMs, 0, deltaScale, durationMs );
                zoomTo( oldScale + newScale, destX, destY );
                if ( currentMs < durationMs ) {
                    mHandler.post( this );
                } else {
                    onZoomAnimationCompleted( getScale() );
                    center( true, true );
                }
            }
        } );
    }

    protected void onZoom( float scale ) {}

    protected void onZoomAnimationCompleted( float scale ) {}
}
