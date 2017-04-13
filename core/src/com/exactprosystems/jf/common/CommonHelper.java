////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////


package com.exactprosystems.jf.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class CommonHelper
{
    private final static String UTF8 = "UTF-8"; 
    
    
    public static Reader readerFromFileName(String fileName) throws UnsupportedEncodingException, FileNotFoundException
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), UTF8));
    }

    public static Reader readerFromFile(File file) throws UnsupportedEncodingException, FileNotFoundException
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));
    }
    
    public static Reader readerFromString(String contains) 
    {
        return new StringReader(contains);
    }
    
    public static Writer writerToFileName(String fileName) throws UnsupportedEncodingException, FileNotFoundException
    {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), UTF8));
    }

    public static Writer writerToFile(File file) throws UnsupportedEncodingException, FileNotFoundException
    {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF8));
    }

}
