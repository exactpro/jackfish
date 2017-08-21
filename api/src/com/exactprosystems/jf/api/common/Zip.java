////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.io.*;
import java.nio.file.*;
import java.util.*;
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
        File file = new File(path);
        if(file.isFile() && file.exists()){
            try(ZipFile zf = new ZipFile(file)){
                Enumeration<? extends ZipEntry> enumeration = zf.entries();
                ZipEntry entry;
                while(enumeration.hasMoreElements()){
                    entry = enumeration.nextElement();
                    if (!entry.isDirectory()){
                        InputStream is = zf.getInputStream(entry);
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                        int nRead;
                        byte[] data = new byte[16384];

                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();

                        this.entries.put(entry.getName(), compress(buffer.toByteArray()));
                    }
                }
            }
        }
        
        return this;
    }

    @DescriptionAttribute(text = "Saves the archive to @path.")
    public Zip save(String path) throws IOException, DataFormatException
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
        File file = new File(path);

        if (file.isFile() && file.exists()){
            this.entries.put(file.getName(), compress(getBytesFromFile(file)));
        }
        /*if (file.isDirectory() && file.exists()){
            File[] files = file.listFiles();
            for (File f : files){
                if(f.isFile()){
                    this.entries.put(f.getName(), compress(getBytesFromFile(f)));
                }
            }
        }*/

        return this;
    }

    @DescriptionAttribute(text = "Remove element from Zip by name")
    public Zip remove(String name)
    {
        this.entries.entrySet().removeIf(e-> e.getKey().equals(name));
        return this;
    }

    @DescriptionAttribute(text = "Extracts one file with @name from zip archive to @path.")
    public Zip extract(String name, String path) throws IOException, DataFormatException
    {
        if (this.entries.containsKey(name)) {
            Path p = Paths.get(path);
            if(p.toFile().isDirectory()){
                Path pathToFile = p.resolve(name);
                if(!pathToFile.getParent().toFile().exists()){
                    Files.createDirectories(pathToFile.getParent());
                }
                if(!pathToFile.toFile().exists()){
                    Files.createFile(pathToFile);
                }

                try(FileOutputStream fos = new FileOutputStream(pathToFile.toFile())){
                    byte[] preparedBytes = decompress(this.entries.get(name));
                    fos.write(preparedBytes);
                    fos.close();
                }
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
