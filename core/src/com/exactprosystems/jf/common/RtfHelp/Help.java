package com.exactprosystems.jf.common.RtfHelp;

import java.net.URL;

public class Help {

    public Help() {
    }

    public URL header(){
        return this.getClass().getResource("Header.png");
    }

    public URL introPicture(){
        return this.getClass().getResource("Intro.png");
    }

    public URL footer(){
        return this.getClass().getResource("Footer.png");
    }

    public URL introduction(){
        return this.getClass().getResource("intro.txt");
    }

    public URL mvel(){
        return this.getClass().getResource("mvel.txt");
    }
}
