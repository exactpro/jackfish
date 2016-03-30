package com.exactprosystems.jf.app.jna;

import com.sun.jna.Native;

import java.io.File;

public class Main {

 /*   public static void main(String[] args)
    {
	    System.out.println("Starting to load a library");
        //TOOD need full path to jna
        JnaAdapter in = (JnaAdapter) Native.loadLibrary("C:\\jackfish\\AppWinGui\\src\\com\\exactprosystems\\jf\\app\\jna\\UIAdapter.dll",JnaAdapter.class);
        System.out.println("Library loaded");

        System.out.println("Starting app");
        in.run("C:\\Users\\user_adm\\Downloads\\dllexp-x64\\dllexp.exe","C:\\Users\\user_adm\\Downloads\\dllexp-x64","");
        System.out.println("App started");

        System.out.println("Refreshing app");
        in.refresh();
        System.out.println("App refreshed");

        System.out.println("Closing app");
        in.stop();
        System.out.println("App closed");
    }*/

    public static void main(String[] args) {
        String pathToDlls = System.getProperty("user.dir") + "\\dlls\\";
        String uiAdapterPath = pathToDlls + "UIAdapter.dll";
        String log4netPath = pathToDlls + "log4net.dll";
        //TODO need to write path to
        String log4netConfigurePath = pathToDlls + "Log4net.configure";

        CSharpDllHandler dllHandler = new CSharpDllHandler(pathToDlls);
        dllHandler.loadAssemblies(uiAdapterPath, log4netPath);
        JnaAdapter jnaAdapter = dllHandler.getJnaAdapter();
        jnaAdapter.configureLog4net(log4netConfigurePath);


        System.out.println("Starting app");
        jnaAdapter.run("C:\\Users\\user_adm\\Downloads\\dllexp-x64\\dllexp.exe", "C:\\Users\\user_adm\\Downloads\\dllexp-x64", "");
        System.out.println("App started");

        System.out.println("Refreshing app");
        jnaAdapter.refresh();
        System.out.println("App refreshed");

        System.out.println("Closing app");
        jnaAdapter.stop();
        System.out.println("App closed");
    }
}
