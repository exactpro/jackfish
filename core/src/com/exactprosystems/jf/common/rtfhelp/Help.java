package com.exactprosystems.jf.common.rtfhelp;

import java.io.InputStream;

public class Help {

    public Help() {
    }

    public InputStream headerPicture(){
        return this.getClass().getResourceAsStream("Header.png");
    }

    public InputStream introPicture(){
        return this.getClass().getResourceAsStream("Intro.png");
    }

    public InputStream footerPicture(){
        return this.getClass().getResourceAsStream("Footer.png");
    }

    public InputStream introduction(){ return this.getClass().getResourceAsStream("intro.txt"); }

    public InputStream mvel(){
        return this.getClass().getResourceAsStream("mvel.txt");
    }

    public InputStream panel(){
        return this.getClass().getResourceAsStream("panel.txt");
    }
}
