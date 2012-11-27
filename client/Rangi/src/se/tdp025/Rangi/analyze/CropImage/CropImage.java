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
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.*;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import se.tdp025.Rangi.R;
import se.tdp025.Rangi.analyze.AnalyzeView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;


/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImage extends MonitoredActivity {
    private static final String TAG = "Rangi_CropImage";

    // These are various options can be specified in the intent.
    private Bitmap.CompressFormat mOutputFormat =
            Bitmap.CompressFormat.JPEG; // only used with mSaveUri
    private Uri mSaveUri = null;
    private int mAspectX, mAspectY;
    private boolean mCircleCrop = false;
    private final Handler mHandler = new Handler();

    // These options specifiy the output image size and whether we should
    // scale the output to fit it (or just crop it).
    private int mOutputX, mOutputY;
    private boolean mScale;
    private boolean mScaleUp = true;

    private boolean mDoFaceDetection = true;

    public boolean mWaitingToPick; // Whether we are wait the user to pick a face.
    public boolean mSaving;  // Whether the "save" button is already clicked.

    private CropImageView mImageView;
    private ContentResolver mContentResolver;

    private Bitmap mBitmap;
    private final BitmapManager.ThreadSet mDecodingThreads =
            new BitmapManager.ThreadSet();
    public HighlightView mCrop;

    private IImage mImage;
    
    private int disableMenuButton = R.id.m_square_tool;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mContentResolver = getContentResolver();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cropimage);

        mImageView = (CropImageView) findViewById(R.id.image);

        showStorageToast(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.getString("circleCrop") != null) {
                mCircleCrop = true;
                mAspectX = 1;
                mAspectY = 1;
            }

            mSaveUri = (Uri) this.getIntent().getExtras().get("image-uri");
            mBitmap = getBitmap();
            Log.v(TAG, "HEIGHT: " + mBitmap.getHeight());
            Log.v(TAG, "WIDTH: " + mBitmap.getWidth());
            mAspectX = extras.getInt("aspectX");
            mAspectY = extras.getInt("aspectY");
            mOutputX = extras.getInt("outputX");
            mOutputY = extras.getInt("outputY");
            mScale = extras.getBoolean("scale", true);
            mScaleUp = extras.getBoolean("scaleUpIfNeeded", true);
        }



        if (mBitmap == null) {
            Log.d(TAG, "finish!!!");
            finish();
            return;
        }

        startFaceDetection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.analyze_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        Log.e(TAG, "onPrepareOptionsMenu ");
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if(item.getItemId() == disableMenuButton) {
                item.setEnabled(false);
            }
            else {
                item.setEnabled(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.m_analyze_tool:
                analyze();

                break;
            case R.id.m_circle_tool:
                disableMenuButton = R.id.m_circle_tool;
                circleTool();
                break;
            case R.id.m_square_tool:
                disableMenuButton = R.id.m_square_tool;
                squareTool();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void squareTool() {
        Log.d(TAG, "squareTool function.");
        Toast.makeText(this, "squareTool", Toast.LENGTH_SHORT).show();
        mCircleCrop = false;
        mAspectX = 0;
        mAspectY = 0;
        mImageView.mCurrentScaleFactor = 1f;
        startFaceDetection();
    }

    public void circleTool() {
        Log.d(TAG, "circleTool function.");
        Toast.makeText(this, "circleTool", Toast.LENGTH_SHORT).show();
        mCircleCrop = true;
        mAspectX = 1;
        mAspectY = 1;
        mImageView.mCurrentScaleFactor = 1f;
        startFaceDetection();
    }

    public void analyze() {
        Log.d(TAG, "analyze function.");
        Toast.makeText(this, "analyze", Toast.LENGTH_SHORT).show();
        Bitmap croppedImage = onSaveClicked();
        Log.v(TAG, "analyze - Height: " + croppedImage.getHeight());
        Log.v(TAG, "analyze - Width: " + croppedImage.getWidth());

        mSaving = false;
        if(croppedImage != null) {
            final Bitmap b = croppedImage;
            final Intent analyze = new Intent(this, AnalyzeView.class);
            Util.startBackgroundJob(this, null, "Cutting image",
                    new Runnable() {
                        public void run() {
                            //saveOutput(analyze, b);
                            ByteArrayOutputStream bs = new ByteArrayOutputStream();
                            b.compress(Bitmap.CompressFormat.PNG, 50, bs);
                            analyze.putExtra("image-byteArray", bs.toByteArray());
                            startActivity(analyze);
                        }
                    }, mHandler);
        }
        else {
            Log.v(TAG, "analyze - croppedImage is null");
        }
    }

    private Bitmap getBitmap() {
        try {
            Log.v(TAG, "file " + mSaveUri.getPath());
            return MediaStore.Images.Media.getBitmap(getContentResolver(), mSaveUri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "file " + mSaveUri.getPath() + " not found");
        } catch (IOException e) {
            Log.e(TAG, "file " + mSaveUri.getPath() + " not found");
        }
        return null;
    }


    private void startFaceDetection() {
        if (isFinishing()) {
            return;
        }

        mImageView.setImageBitmapResetBase(mBitmap, true);

        Util.startBackgroundJob(this, null,
                "Please wait\u2026",
                new Runnable() {
                    public void run() {
                        final CountDownLatch latch = new CountDownLatch(1);
                        final Bitmap b = (mImage != null)
                                ? mImage.fullSizeBitmap(IImage.UNCONSTRAINED,
                                1024 * 1024)
                                : mBitmap;
                        mHandler.post(new Runnable() {
                            public void run() {
                                if (b != mBitmap && b != null) {
                                    mImageView.setImageBitmapResetBase(b, true);
                                    mBitmap.recycle();
                                    mBitmap = b;
                                }
                                if (mImageView.getScale() == 1F) {
                                    mImageView.center(true, true);
                                }
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        mRunFaceDetection.run();
                    }
                }, mHandler);
    }



    private Bitmap onSaveClicked() {
        // TODO this code needs to change to use the decode/crop/encode single
        // step api so that we don't require that the whole (possibly large)
        // bitmap doesn't have to be read into memory
        if (mSaving) return null;

        if (mCrop == null) {
            return null;
        }

        mSaving = true;

        Rect r = mCrop.getCropRect();

        int width = r.width();
        int height = r.height();

        // If we are circle cropping, we want alpha channel, which is the
        // third param here.
        Bitmap croppedImage = Bitmap.createBitmap(width, height,
                mCircleCrop
                        ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        {
            Canvas canvas = new Canvas(croppedImage);
            Rect dstRect = new Rect(0, 0, width, height);
            canvas.drawBitmap(mBitmap, r, dstRect, null);
        }

        if (mCircleCrop) {
            // OK, so what's all this about?
            // Bitmaps are inherently rectangular but we want to return
            // something that's basically a circle.  So we fill in the
            // area around the circle with alpha.  Note the all important
            // PortDuff.Mode.CLEAR.
            Canvas c = new Canvas(croppedImage);
            Path p = new Path();
            p.addCircle(width / 2F, height / 2F, width / 2F,
                    Path.Direction.CW);
            c.clipPath(p, Region.Op.DIFFERENCE);
            c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        }

		/* If the output is required to a specific size then scale or fill */
        if (mOutputX != 0 && mOutputY != 0) {
            if (mScale) {
				/* Scale the image to the required dimensions */
                Bitmap old = croppedImage;
                croppedImage = Util.transform(new Matrix(),
                        croppedImage, mOutputX, mOutputY, mScaleUp);
                if (old != croppedImage) {
                    old.recycle();
                }
            } else {

				/* Don't scale the image crop it to the size requested.
				 * Create an new image with the cropped image in the center and
				 * the extra space filled.
				 */

                // Don't scale the image but instead fill it so it's the
                // required dimension
                Bitmap b = Bitmap.createBitmap(mOutputX, mOutputY,
                        Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(b);

                Rect srcRect = mCrop.getCropRect();
                Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);

                int dx = (srcRect.width() - dstRect.width()) / 2;
                int dy = (srcRect.height() - dstRect.height()) / 2;

				/* If the srcRect is too big, use the center part of it. */
                srcRect.inset(Math.max(0, dx), Math.max(0, dy));

				/* If the dstRect is too big, use the center part of it. */
                dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

				/* Draw the cropped bitmap in the center */
                canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

				/* Set the cropped bitmap as the new bitmap */
                croppedImage.recycle();
                croppedImage = b;
            }
        }
        return croppedImage;

        /*// Return the cropped image directly or save it to the specified URI.
        Bundle myExtras = getIntent().getExtras();
        if (myExtras != null && (myExtras.getParcelable("data") != null
                || myExtras.getBoolean("return-data"))) {
            Bundle extras = new Bundle();
            extras.putParcelable("data", croppedImage);
            setResult(Activity.RESULT_OK,
                    (new Intent()).setAction("inline-data").putExtras(extras));
            finish();
        } else {
            final Bitmap b = croppedImage;
            Util.startBackgroundJob(this, null, "Saving image",
                    new Runnable() {
                        public void run() {
                            saveOutput(b);
                        }
                    }, mHandler);
        }    */
    }

    private void saveOutput(Intent intent, Bitmap croppedImage) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Rangi");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File croppedFile = new File(mediaStorageDir.getPath() + File.separator +
                "RANGI_IMG_"+ timeStamp + ".jpg");
        Uri croppedUri = Uri.fromFile(croppedFile);

        if (croppedUri != null) {
            OutputStream outputStream = null;

            try {

                outputStream = mContentResolver.openOutputStream(croppedUri);
                if (outputStream != null) {
                    croppedImage.compress(mOutputFormat, 75, outputStream);
                }
            } catch (IOException ex) {
                // TODO: report error to caller
                Log.e(TAG, "Cannot open file: " + mSaveUri, ex);
            } finally {
                Util.closeSilently(outputStream);
            }
            Bundle extras = new Bundle();
            intent.putExtra("cropped-image-uri", croppedUri);
            startActivity(intent);
        } else {
            Log.e(TAG, "not defined image url");
        }
        croppedImage.recycle();
        //finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BitmapManager.instance().cancelThreadDecoding(mDecodingThreads);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBitmap.recycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BitmapManager.instance().allowThreadDecoding(mDecodingThreads);
    }

    public Runnable mRunFaceDetection = new Runnable() {
        @SuppressWarnings("hiding")
        float mScale = 1F;
        Matrix mImageMatrix;
        FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
        int mNumFaces;

        // For each face, we create a HightlightView for it.
        private void handleFace(FaceDetector.Face f) {
            PointF midPoint = new PointF();

            int r = ((int) (f.eyesDistance() * mScale)) * 2;
            f.getMidPoint(midPoint);
            midPoint.x *= mScale;
            midPoint.y *= mScale;

            int midX = (int) midPoint.x;
            int midY = (int) midPoint.y;

            HighlightView hv = new HighlightView(mImageView);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            RectF faceRect = new RectF(midX, midY, midX, midY);
            faceRect.inset(-r, -r);
            if (faceRect.left < 0) {
                faceRect.inset(-faceRect.left, -faceRect.left);
            }

            if (faceRect.top < 0) {
                faceRect.inset(-faceRect.top, -faceRect.top);
            }

            if (faceRect.right > imageRect.right) {
                faceRect.inset(faceRect.right - imageRect.right,
                        faceRect.right - imageRect.right);
            }

            if (faceRect.bottom > imageRect.bottom) {
                faceRect.inset(faceRect.bottom - imageRect.bottom,
                        faceRect.bottom - imageRect.bottom);
            }

            hv.setup(mImageMatrix, imageRect, faceRect, mCircleCrop,
                    mAspectX != 0 && mAspectY != 0);

            mImageView.add(hv);
        }

        // Create a default HightlightView if we found no face in the picture.
        private void makeDefault() {
            HighlightView hv = new HighlightView(mImageView);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            // make the default size about 4/5 of the width or height
            // CROPSIZE
            int cropWidth = Math.min(width, height) * 4 / 5;
            int cropHeight =  cropWidth;

            if(cropWidth > 500) {
                cropWidth = 400;
            }

            if(cropHeight > 500) {
                cropHeight = 400;
            }



            if (mAspectX != 0 && mAspectY != 0) {
                if (mAspectX > mAspectY) {
                    cropHeight = cropWidth * mAspectY / mAspectX;
                } else {
                    cropWidth = cropHeight * mAspectX / mAspectY;
                }
            }

            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;

            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop,
                    mAspectX != 0 && mAspectY != 0);

            mImageView.mHighlightViews.clear(); // Thong added for rotate

            mImageView.add(hv);
        }

        // Scale the image down for faster face detection.
        private Bitmap prepareBitmap() {
            if (mBitmap == null) {
                return null;
            }

            // 256 pixels wide is enough.
            if (mBitmap.getWidth() > 256) {
                mScale = 256.0F / mBitmap.getWidth();
            }
            Matrix matrix = new Matrix();
            matrix.setScale(mScale, mScale);
            Bitmap faceBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap
                    .getWidth(), mBitmap.getHeight(), matrix, true);
            return faceBitmap;
        }

        public void run() {
            mImageMatrix = mImageView.getImageMatrix();
            Bitmap faceBitmap = prepareBitmap();

            mScale = 1.0F / mScale;
            if (faceBitmap != null && mDoFaceDetection) {
                FaceDetector detector = new FaceDetector(faceBitmap.getWidth(),
                        faceBitmap.getHeight(), mFaces.length);
                mNumFaces = detector.findFaces(faceBitmap, mFaces);
            }

            if (faceBitmap != null && faceBitmap != mBitmap) {
                faceBitmap.recycle();
            }

            mHandler.post(new Runnable() {
                public void run() {
                    mWaitingToPick = mNumFaces > 1;
                    if (mNumFaces > 0) {
                        for (int i = 0; i < mNumFaces; i++) {
                            handleFace(mFaces[i]);
                        }
                    } else {
                        makeDefault();
                    }
                    mImageView.invalidate();
                    if (mImageView.mHighlightViews.size() == 1) {
                        mCrop = mImageView.mHighlightViews.get(0);
                        mCrop.setFocus(true);
                    }

                    if (mNumFaces > 1) {
                        Toast t = Toast.makeText(CropImage.this,
                                "Multi face crop help",
                                Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            });
        }
    };

    public static final int NO_STORAGE_ERROR = -1;
    public static final int CANNOT_STAT_ERROR = -2;

    public static void showStorageToast(Activity activity) {
        showStorageToast(activity, calculatePicturesRemaining());
    }

    public static void showStorageToast(Activity activity, int remaining) {
        String noStorageText = null;

        if (remaining == NO_STORAGE_ERROR) {
            String state = Environment.getExternalStorageState();
            if (state == Environment.MEDIA_CHECKING) {
                noStorageText = "Preparing card";
            } else {
                noStorageText = "No storage card";
            }
        } else if (remaining < 1) {
            noStorageText = "Not enough space";
        }

        if (noStorageText != null) {
            Toast.makeText(activity, noStorageText, 5000).show();
        }
    }

    public static int calculatePicturesRemaining() {
        try {
			/*if (!ImageManager.hasStorage()) {
                return NO_STORAGE_ERROR;
            } else {*/
            String storageDirectory =
                    Environment.getExternalStorageDirectory().toString();
            StatFs stat = new StatFs(storageDirectory);
            float remaining = ((float) stat.getAvailableBlocks()
                    * (float) stat.getBlockSize()) / 400000F;
            return (int) remaining;
            //}
        } catch (Exception ex) {
            // if we can't stat the filesystem then we don't know how many
            // pictures are remaining.  it might be zero but just leave it
            // blank since we really don't know.
            return CANNOT_STAT_ERROR;
        }
    }
}