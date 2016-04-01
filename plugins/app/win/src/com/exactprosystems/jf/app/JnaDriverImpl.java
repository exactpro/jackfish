package com.exactprosystems.jf.app;

import java.io.File;

import com.sun.jna.Native;
import com.sun.jna.Platform;

public class JnaDriverImpl
{
	private static String dllDir = "C:\\workspaces\\cs\\UIAdapter\\UIAdapter\\bin\\x64\\Release\\UIAdapter.dll";

	public JnaDriverImpl() throws Exception
	{
		if (Platform.is64Bit())
		{}
		
		if (new File(dllDir).exists())
		{
			this.driver = (JnaDriver) Native.loadLibrary(dllDir, JnaDriver.class);
		}
		else
		{
			throw new Exception("Dll is  not found");
		}
	}
	
	public void run(String exec, String workDir, String param) throws Exception
	{
		this.driver.run(exec, workDir, param);
		String error = this.driver.lastError();
		if (error != null)
		{
			throw new Exception(error);
		}
	}

	public void refresh() throws Exception
	{
		this.driver.refresh();
		String error = this.driver.lastError();
		if (error != null)
		{
			throw new Exception(error);
		}
	}

	public void stop() throws Exception
	{
		this.driver.stop();
		String error = this.driver.lastError();
		if (error != null)
		{
			throw new Exception(error);
		}
	}
	


	private JnaDriver driver;

}
