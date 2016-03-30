package com.exactprosystems.jf.app.jna;

import com.sun.jna.Native;

public class CSharpDllHandler {
    private JnaAdapter jnaAdapter = null;
    private String jnaSearchPath = null;

    public CSharpDllHandler(String jnaSearchPath) {
        this.jnaSearchPath = jnaSearchPath;
        System.out.println("## path to jna : " + this.jnaSearchPath);
        System.setProperty("jna.library.path", this.jnaSearchPath);
        System.out.println("start load library");
        jnaAdapter = (JnaAdapter) Native.loadLibrary("UIAdapter", JnaAdapter.class);
        System.out.println("## end load library");
    }

    public String loadAssemblies(String frameworkDllPath, String log4netDllPath) {
        System.out.println("## add log4net dll, path : " + log4netDllPath);
        String csResult = jnaAdapter.loadAssemblies(frameworkDllPath, log4netDllPath);
        System.out.println("log4net has added");
        System.out.println("## result : " + csResult);
        return csResult;
    }

    public JnaAdapter getJnaAdapter() {
        return jnaAdapter;
    }
}
