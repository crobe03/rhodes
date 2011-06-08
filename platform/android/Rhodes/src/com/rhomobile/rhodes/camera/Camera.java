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

import java.io.File;

import android.content.Intent;

import com.rhomobile.rhodes.Capabilities;
import com.rhomobile.rhodes.Logger;
import com.rhomobile.rhodes.RhodesActivity;
import com.rhomobile.rhodes.RhodesAppOptions;
import com.rhomobile.rhodes.RhodesService;
import com.rhomobile.rhodes.util.PerformOnUiThread;

public class Camera {

	private static final String TAG = "Camera";
	
	public static final String INTENT_EXTRA_PREFIX = RhodesService.INTENT_EXTRA_PREFIX + "camera.";
	
	private static void reportFail(String name, Exception e) {
		Logger.E(TAG, "Call of \"" + name + "\" failed: " + e.getMessage());
	}
	
	private static void init() {
		File f = new File(RhodesAppOptions.getBlobPath());
		if (!f.exists())
			f.mkdirs();
	}
	
	private static class CameraDisabled implements Runnable {
		
		private String url;
		
		public CameraDisabled(String u) {
			url = u;
		}
		
		public void run() {
			callback(url, "", "Camera disabled", false);
		}
		
	};
	
	private static class Picture implements Runnable {
		private String url;
		private Class<?> klass;
		private CameraSettings settings;
		
		public Picture(String u, Class<?> c, Object settingsObj) {
			url = u;
			klass = c;
			settings = new CameraSettings(settingsObj);
		}
		
		public void run() {
			init();
			RhodesActivity ra = RhodesActivity.getInstance();
			Intent intent = new Intent(ra, klass);
			intent.putExtra(INTENT_EXTRA_PREFIX + "callback", url);
			intent.putExtra(INTENT_EXTRA_PREFIX + "settings", settings);
			ra.startActivity(intent);
		}
	};
	
	public static void takePicture(String url, Object params_obj) {
		try {
			Runnable runnable = Capabilities.CAMERA_ENABLED ? new Picture(url, ImageCapture.class, params_obj) :
				new CameraDisabled(url);
			PerformOnUiThread.exec(runnable, false);
		}
		catch (Exception e) {
			reportFail("takePicture", e);
		}
	}

	public static void choosePicture(String url) {
		try {
			PerformOnUiThread.exec(new Picture(url, FileList.class, null), false);
		}
		catch (Exception e) {
			reportFail("choosePicture", e);
		}
	}
	
	public static void doCallback(String callbackUrl, String filePath) {
		String fp = filePath == null ? "" : filePath;
		int idx = fp.lastIndexOf('/');
		if (idx != -1)
			fp = fp.substring(idx + 1);
		callback(callbackUrl, fp, "", fp.length() == 0);
	}

	public static native void callback(String callbackUrl, String filePath, String error, boolean cancelled);

}
