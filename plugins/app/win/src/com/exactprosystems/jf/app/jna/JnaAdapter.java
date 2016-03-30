package com.exactprosystems.jf.app.jna;

import com.sun.jna.Library;

public interface JnaAdapter extends Library
{
    // configures methods
    void configureLog4net(String log4netConfigPath);
    String loadAssemblies(String frameworkDllPath, String log4netDllPath);

    //run methods
    void run(String exec, String workDir, String param);
    void refresh();
    void stop();
}
