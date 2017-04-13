////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////


package com.exactprosystems.jf.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

public class CommonHelper
{
    public static Reader readerFromFileName(String fileName) throws UnsupportedEncodingException, FileNotFoundException
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
    }

    public static Reader readerFromFile(File file) throws UnsupportedEncodingException, FileNotFoundException
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }
    
    public static Reader readerFromString(String contains) 
    {
        return new StringReader(contains);
    }
}
