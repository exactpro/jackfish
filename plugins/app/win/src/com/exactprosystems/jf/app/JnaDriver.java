package com.exactprosystems.jf.app;

import com.sun.jna.Library;

public interface JnaDriver extends Library
{
	String lastError();
	
    void run(String exec, String workDir, String param);
    void refresh();
    void stop();
}