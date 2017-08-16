////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class Zip
{
    private Map<String, byte[]> entries;
    
    private Zip()
    {
    } 
    
    @DescriptionAttribute(text = "Creates new Zip object.")
    public static Zip create()
    {
        Zip zip = new Zip();
        zip.entries = new HashMap<>();
        return zip;
    }
    
    @DescriptionAttribute(text = "Loads an archive from @path.")
    public Zip load(String path) throws IOException
    {
        
        try (InputStream in = new FileInputStream(path);
                ZipInputStream zis = new ZipInputStream(in))
        {
            
            ZipEntry nextEntry = null;
            while((nextEntry = zis.getNextEntry()) != null)
            {
                String name = nextEntry.getName();

                zis.closeEntry();
            }
        }
        
        return this;
    }

    @DescriptionAttribute(text = "Saves the archive to @path.")
    public Zip save(String path) throws IOException //https://stackoverflow.com/questions/10103861/adding-files-to-zip-file
    {
        File file = new File(path);
        Path pathToFile = Paths.get(path);
        if(!pathToFile.getParent().toFile().exists()){
            Files.createDirectories(pathToFile.getParent());
        }
        if(!pathToFile.toFile().exists()){
            Files.createFile(pathToFile);
        }
        ZipOutputStream zipOut = null;

        try{
            zipOut = new ZipOutputStream(new FileOutputStream(file));
            zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);
            for(Map.Entry<String, byte[]> entry: this.entries.entrySet()){
                zipOut.putNextEntry(new ZipEntry(entry.getKey()));
                zipOut.write(entry.getValue());
                zipOut.closeEntry();
            }
            zipOut.flush();
        } finally {
            zipOut.close();
        }

        return this;
    }
    
    @DescriptionAttribute(text = "Adds one file into zip archive from @path.")
    public Zip add(String path) throws IOException
    {
        //https://stackoverflow.com/questions/23612864/create-a-zip-file-in-memory
        File file = new File(path);
        String fileName = file.getName();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        if (file.isFile() && file.exists()){
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                ZipEntry ze = new ZipEntry(fileName);
                zos.putNextEntry(ze);
                String line;
                while ((line = br.readLine()) != null) {
                    zos.write(line.getBytes());
                }
                zos.closeEntry();
            } finally {
                //zos.flush();
                zos.close();
            }
            this.entries.put(fileName, baos.toByteArray());
        }
        if (file.isDirectory()){
            //don't support yet
        }

        return this;
    }

    @DescriptionAttribute(text = "Extracts one file with @name from zip archive to @path.")
    public Zip extract(String name, String path) throws IOException
    {
        if (this.entries.containsKey(name)) {
            Files.write(Paths.get(path), this.entries.get(name));
        }
        return this;
    }

    
    public List<String> names()
    {
        return this.entries.keySet().stream().collect(Collectors.toList());
                
    }
}
