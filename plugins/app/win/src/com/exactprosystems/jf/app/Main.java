package com.company;

import com.sun.jna.Native;

public class Main {

    public static void main(String[] args)
    {
	    System.out.println("Starting to load a library");
        dllint in = (dllint) Native.loadLibrary("C:\\Users\\user_adm\\Documents\\Projects\\UIAdapterCheckout\\UIAdapter\\bin\\x64\\Release\\UIAdapter.dll",dllint.class);
        System.out.println("Library loaded");
        System.out.println("Starting app");
        in.run("C:\\Users\\user_adm\\Downloads\\dllexp-x64\\dllexp.exe","C:\\Users\\user_adm\\Downloads\\dllexp-x64","");
        System.out.println("App started");
        System.out.println("Refreshing app");
        //in.refresh();
        System.out.println("App refreshed");
        System.out.println("Closing app");
        //in.stop();
        System.out.println("App closed");
    }
}
