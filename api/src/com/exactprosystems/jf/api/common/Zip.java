////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Zip
{
    private Map<String, ZipEntry> entries;
    
    private Zip()
    {
    } 
    
    @DescriptionAttribute(text = "Creates new Zip object.")
    public static Zip create()
    {
        Zip zip = new Zip();
        
        
        return zip;
    }
    
    @DescriptionAttribute(text = "Loads an archieve from @path.")
    public Zip load(String path) throws IOException
    {
        
        try (InputStream in = new FileInputStream(path);
                ZipInputStream zis = new ZipInputStream(in))
        {
            
            ZipEntry nextEntry = null;
            while((nextEntry = zis.getNextEntry()) != null)
            {
                String name = nextEntry.getName();
                System.err.println(">> " + name);
                
                zis.closeEntry();
            }
        }
        
        return this;
    }

    @DescriptionAttribute(text = "Saves the archieve to @path.")
    public Zip save(String path) throws IOException
    {
        
        
        return this;
    }
    
    @DescriptionAttribute(text = "Adds one file into zip archieve from @path.")
    public Zip add(String path) throws IOException
    {
        
        
        return this;
    }

    @DescriptionAttribute(text = "Extracts one file with @name from zip archieve to @path.")
    public Zip extract(String name, String path) throws IOException
    {
        
        
        return this;
    }

    
    public List<String> names()
    {
        return this.entries.keySet().stream().collect(Collectors.toList());
                
    }
}
