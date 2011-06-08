package com.rho;

import com.xruby.runtime.builtin.RubyArray;
import com.xruby.runtime.lang.RubyProgram;
import com.rho.net.NetResponse;
import com.rho.file.*;

public interface IRhoRubyHelper {
	public abstract void initRubyExtensions();
	public abstract RubyProgram createMainObject() throws Exception;
	public abstract String getPlatform();
	
	public abstract IFileAccess createFileAccess();
	public abstract IRAFile createRAFile();
	public abstract IRAFile createFSRAFile();
	
	public void loadBackTrace(RubyArray backtrace);
	public String getDeviceId();
	
	public String getAppProperty(String name);
	public String getModuleName();
	public boolean isSimulator();
	
	public void showLog();
	
	public String getGeoLocationText();
	public void wakeUpGeoLocation();
	
	public NetResponse postUrl(String url, String body);
	public void postUrlNoWait(String url, String body);
	public NetResponse postUrlSync(String url, String body)throws Exception;
	
	public void navigateUrl(String url);
	public void navigateBack();
	public void app_exit();
	
	public void unzip_file(String strPath)throws Exception;
}
