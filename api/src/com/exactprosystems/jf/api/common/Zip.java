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
import java.util.zip.*;


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
        /*Path pathToFile = Paths.get(path);
        try(ZipFile zf = new ZipFile(pathToFile.toFile());){
            while(zf.entries().hasMoreElements()){
                zf.entries().
            }
        }
*/
        
        return this;
    }

    @DescriptionAttribute(text = "Saves the archive to @path.")
    public Zip save(String path) throws IOException, DataFormatException
    //https://stackoverflow.com/questions/10103861/adding-files-to-zip-file
    {
        File file = new File(path);
        Path pathToFile = Paths.get(path);
        if(!pathToFile.getParent().toFile().exists()){
            Files.createDirectories(pathToFile.getParent());
        }
        if(!pathToFile.toFile().exists()){
            Files.createFile(pathToFile);
        }

        try(ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(file))){
            zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);
            for(Map.Entry<String, byte[]> entry: this.entries.entrySet()){
                zipOut.putNextEntry(new ZipEntry(entry.getKey()));
                zipOut.write(decompress(entry.getValue()));
                zipOut.closeEntry();
            }
            zipOut.flush();
        }

        return this;
    }
    
    @DescriptionAttribute(text = "Adds one file into zip archive from @path.")
    public Zip add(String path) throws IOException
    {
        //https://stackoverflow.com/questions/23612864/create-a-zip-file-in-memory
        File file = new File(path);

        if (file.isFile() && file.exists()){
            this.entries.put(file.getName(), compress(getBytesFromFile(file)));
        }
        if (file.isDirectory()){
            //not support yet
        }

        return this;
    }

    @DescriptionAttribute(text = "Remove element from Zip by name")
    public Zip remove(String name)
    {
        this.entries.entrySet().removeIf(e-> e.getKey() == name );
        return this;
    }

    @DescriptionAttribute(text = "Extracts one file with @name from zip archive to @path.")
    public Zip extract(String name, String path) throws IOException, DataFormatException
    {
        if (this.entries.containsKey(name)) {
            File file = new File(path);
            Path pathToFile = Paths.get(path);
            if(!pathToFile.getParent().toFile().exists()){
                Files.createDirectories(pathToFile.getParent());
            }
            if(!pathToFile.toFile().exists()){
                Files.createFile(pathToFile);
            }

            try(FileOutputStream fos = new FileOutputStream(file)){
                byte[] preparedBytes = decompress(this.entries.get(name));
                fos.write(preparedBytes);
                fos.close();
            }
        }
        return this;
    }

    
    public List<String> names()
    {
        return this.entries.keySet().stream().collect(Collectors.toList());
                
    }

    private byte[] getBytesFromFile(File file) throws IOException{
        String separator = System.lineSeparator();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = br.readLine()) != null) {
                baos.write(line.getBytes());
                baos.write(separator.getBytes());
            }
        }
        return baos.toByteArray();
    }

    private byte[] compress(byte[] input){
        byte[] output = new byte[input.length];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        compresser.end();
        return output;
    }

    private byte[] decompress(byte[] input) throws DataFormatException {
        Inflater decompresser = new Inflater();
        decompresser.setInput(input, 0, input.length);
        byte[] result = new byte[input.length];
        int resultLength = decompresser.inflate(result);
        decompresser.end();
        return result;
    }
}
