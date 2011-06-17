/*
 ============================================================================
 Author	    : Dmitry Moskalchuk
 Version	: 1.5
 Copyright  : Copyright (C) 2008 Rhomobile. All rights reserved.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ============================================================================
 */
package com.rhomobile.rhodes.camera;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.rhomobile.rhodes.AndroidR;
import com.rhomobile.rhodes.Logger;
import com.rhomobile.rhodes.BaseActivity;
import com.rhomobile.rhodes.RhodesAppOptions;

import android.content.ContentValues;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

public class ImageCapture extends BaseActivity implements SurfaceHolder.Callback, OnClickListener
 {
	
	private static final String TAG = "ImageCapture";
	
	private String callbackUrl;
	private Camera camera;
	private boolean isPreviewRunning = false;
	private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private ImageButton cameraButton;
    private OrientationEventListener myOrientationEventListener;
    private int m_rotation = 0;
    
    private CameraSettings mSettings = null;
    private boolean mIsFrontCamera = false;
    
    
	// private Uri target = Media.EXTERNAL_CONTENT_URI;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Logger.D(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(AndroidR.layout.camera);
		
		Bundle extras = getIntent().getExtras();
		callbackUrl = extras.getString(com.rhomobile.rhodes.camera.Camera.INTENT_EXTRA_PREFIX + "callback");
		mSettings = (CameraSettings)extras.getSerializable(com.rhomobile.rhodes.camera.Camera.INTENT_EXTRA_PREFIX + "settings");
		
		surfaceView = (SurfaceView) findViewById(AndroidR.id.surface);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		cameraButton = (ImageButton)findViewById(AndroidR.id.cameraButton);
		cameraButton.setOnClickListener(this);
		
        myOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL)
        {
            @Override
            public void onOrientationChanged(int orientation) 
            { 
                //Logger.D(TAG, "onOrientationChanged: " + orientation); 
                if (orientation == ORIENTATION_UNKNOWN) 
                    return; 
                 
                m_rotation = orientation;    
             }   
        };
    
        if (myOrientationEventListener.canDetectOrientation())
        {
           Logger.I(TAG, "myOrientationEventListener.enable()"); 
           myOrientationEventListener.enable();
        }
        else
        {
           Logger.I(TAG, "cannot detect!"); 
           myOrientationEventListener = null;
        }		
	}

	@Override
	public void finish() 
	{
	    Logger.D(TAG, "finish");
	    if ( myOrientationEventListener != null )
	        myOrientationEventListener.disable();
	        
        myOrientationEventListener = null;	        
		super.finish();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	PictureCallback mPictureCallbackRaw = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera c) {
			Logger.D(TAG, "PICTURE CALLBACK RAW");
		}
	};

	Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		public void onShutter() {
			Logger.D(TAG, "SHUTTER CALLBACK");
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_CAMERA:
			takePictureWithAutofocus();
			cameraButton.setVisibility(View.INVISIBLE);
			return true;
		case KeyEvent.KEYCODE_BACK:
			return super.onKeyDown(keyCode, event);
		default:
			return false;
		}
	}

	protected void onResume() {
		Logger.D(TAG, "onResume");
		super.onResume();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected void onStop() {
		Logger.D(TAG, "onStop");
		super.onStop();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Logger.D(TAG, "surfaceCreated");
		try {
			int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
			if (sdkVersion >= Build.VERSION_CODES.GINGERBREAD) {
				if (mSettings.getCameraType() == mSettings.CAMERA_TYPE_FRONT) {
					// find front camera
					int camera_count = Camera.getNumberOfCameras();
					int i;
					for (i = 0 ; i < camera_count; i++) {
						Camera.CameraInfo info = new Camera.CameraInfo();
						Camera.getCameraInfo(i, info);
						if (info.facing == info.CAMERA_FACING_FRONT) {
							camera = Camera.open(i);
							mIsFrontCamera = true;
							break;
						}
					}
				}
				else {
					camera = Camera.open();
				}
			}
			else {
				camera = Camera.open();
			}
		}
		catch (Exception e) {
			Logger.E(TAG, e.getMessage());
			finish();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		try {
			Logger.D(TAG, "surfaceChanged");
			if (camera == null) {
				Logger.E(TAG, "Camera was not opened");
				return;
			}
			
			if (isPreviewRunning) {
				camera.stopPreview();
			}
			Camera.Parameters p = camera.getParameters();
			
			int newW = (w >> 3) << 3;
			int newH = (h >> 3) << 3;
			List<Size> sizes = p.getSupportedPreviewSizes();
			Iterator<Size> iter = sizes.iterator();
			// find closest preview size
			float min_r = -1;
			int minW = 0;
			int minH = 0;
			while (iter.hasNext()) {
				Size s = iter.next();
				if (min_r < 0) {
					min_r = (float)s.width*(float)s.width+(float)s.height*(float)s.height;
					minW = s.width;
					minH = s.height;
				}
				else {
					float cur_r = ((float)newW-(float)s.width)*((float)newW-(float)s.width)+((float)newH-(float)s.height)*((float)newH-(float)s.height);
					if (cur_r < min_r) {
						min_r = cur_r;
						minW = s.width;
						minH = s.height;
					}
				}
			}
			if (min_r >= 0) {
				newW = minW;
				newH = minH;
			}
			
			p.setPreviewSize(newW, newH);
			if (mSettings != null) {
	            if ((mSettings.getWidth() > 0) && (mSettings.getHeight() > 0)) {
	                p.setPictureSize(mSettings.getWidth(), mSettings.getHeight());
	            }
	            if (mSettings.getColorModel() == mSettings.CAMERA_COLOR_MODEL_GRAYSCALE) {
	            	p.setColorEffect(Camera.Parameters.EFFECT_MONO);
	            }
			}
			camera.setParameters(p);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
			isPreviewRunning = true;
		} catch (Exception e) {
			Logger.E(TAG, e.getMessage());
		}

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Logger.D(TAG, "surfaceDestroyed");
		if (camera != null) {
			camera.stopPreview();
			isPreviewRunning = false;
			camera.release();
			camera = null;
		}
	}

	public void onClick(View v) {
		if (v.getId() == AndroidR.id.cameraButton) {
			takePictureWithAutofocus();
			cameraButton.setVisibility(View.INVISIBLE);
		}
	}

	private void takePictureWithAutofocus() {
		if (camera == null) {
			Logger.E(TAG, "Attempt of auto focus while camera was not opened");
			return;
		}
		
		//this only from API v.5 and higher
		//String focus_mode = camera.getParameters().getFocusMode();
		//if ((focus_mode != Camera.Parameters.FOCUS_MODE_FIXED) && (focus_mode != Camera.Parameters.FOCUS_MODE_INFINITY)) {
		camera.autoFocus(new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				takePicture();
			}
		});
		//}
	}

	
	private void takePicture() {
		if (camera == null) {
			Logger.E(TAG, "Attempt of take picture while camera was not opened");
			return;
		}
		
		ImageCaptureCallback iccb = null;
		try {
			String filename = "Image_" + timeStampFormat.format(new Date());
			ContentValues values = new ContentValues(5);
			values.put(Media.TITLE, filename);
			values.put(Media.DISPLAY_NAME, filename);
			values.put(Media.DATE_TAKEN, new Date().getTime());
			values.put(Media.MIME_TYPE, "image/jpeg");
			values.put(Media.DESCRIPTION, "Image capture by camera");

			Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
			// String filename = timeStampFormat.format(new Date());
			String dir = RhodesAppOptions.getBlobPath();
			
			OutputStream osCommon = getContentResolver().openOutputStream(uri);
			
	        Camera.Parameters parameters = camera.getParameters();
	        
	        int imgW = 0;
	        int imgH = 0;
	        
            //int nOrient = RhodesService.getInstance().getScreenOrientation();
            int nCamRotate = 90;
            if ( (m_rotation > 45 && m_rotation < 135) || (m_rotation > 225 && m_rotation < 315) )
                nCamRotate = 0;
            if (mIsFrontCamera) {
                nCamRotate = 0;
                parameters.setRotation(270);
            }
	        Logger.D(TAG, "Camera rotation: " + nCamRotate );
            parameters.set("rotation", nCamRotate );
            if ((mSettings.getWidth() > 0) && (mSettings.getHeight() > 0)) {
            
    			Camera.Parameters p = camera.getParameters();
    			
    			int newW = mSettings.getWidth();
    			int newH = mSettings.getHeight();

    	        Logger.D(TAG, "Preferred size : " + String.valueOf(newW) + " x " + String.valueOf(newH) );
   			
    			List<Size> sizes = p.getSupportedPictureSizes();
    			Iterator<Size> iter = sizes.iterator();
    			// find closest preview size
    			float min_r = -1;
    			int minW = 0;
    			int minH = 0;
    	        Logger.D(TAG, "    Supported sizes : ");
    			while (iter.hasNext()) {
    				Size s = iter.next();
        	        Logger.D(TAG, "         size : " + String.valueOf((int)s.width) + " x " + String.valueOf((int)s.height) );
    				if (min_r < 0) {
    					min_r = (float)s.width*(float)s.width+(float)s.height*(float)s.height;
    					minW = s.width;
    					minH = s.height;
    				}
    				else {
    					float cur_r = ((float)newW-(float)s.width)*((float)newW-(float)s.width)+((float)newH-(float)s.height)*((float)newH-(float)s.height);
    					if (cur_r < min_r) {
    						min_r = cur_r;
    						minW = s.width;
    						minH = s.height;
    					}
    				}
    			}
    			if (min_r >= 0) {
    				newW = minW;
    				newH = minH;
    			}
    	        Logger.D(TAG, "    Selected size : " + String.valueOf(newW) + " x " + String.valueOf(newH) );
            	parameters.setPictureSize(newW, newH);
            	imgW = newW;
            	imgH = newH;
            }
            else {
            	// detect camera resolution
    			Camera.Parameters p = camera.getParameters();
    			
    			List<Size> sizes = p.getSupportedPictureSizes();
    			Iterator<Size> iter = sizes.iterator();
    			// find closest preview size
    			float max_S = -1;
    			int maxW = 0;
    			int maxH = 0;
    			while (iter.hasNext()) {
    				Size s = iter.next();
					float cur_S = ((float)s.width)*((float)s.height);
					if (cur_S > max_S) {
						max_S = cur_S;
						maxW = s.width;
						maxH = s.height;
					}
    			}
    			imgW = maxW;
    			imgH = maxH;
            }
            if (mSettings.getColorModel() == mSettings.CAMERA_COLOR_MODEL_GRAYSCALE) {
            	parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
            }
			iccb = new ImageCaptureCallback(this, callbackUrl, osCommon, dir + "/" + filename + ".jpg", imgW, imgH, "jpg");
            camera.setParameters(parameters);

		} catch (Exception ex) {
			Logger.E(TAG, ex.getMessage());
		}
		
		camera.takePicture(mShutterCallback, mPictureCallbackRaw, iccb);
	}

}
