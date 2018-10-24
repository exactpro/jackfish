/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.api.common;

import com.exactprosystems.jf.api.common.i18n.R;

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
    
    @DescriptionAttribute(text = R.ZIP_CREATE_DESCRIPTION)
    public static Zip create()
    {
        Zip zip = new Zip();
        zip.entries = new HashMap<>();
        return zip;
    }
    
    @DescriptionAttribute(text = R.ZIP_LOAD_DESCRIPTION)
    public Zip load(String path) throws Exception
    {
        File file = new File(path);
        if(file.isFile()){
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
        } else {
            throw new Exception(String.format(R.ZIP_NOT_EXISTS_OR_NOT_FILE.get(), file.getName()));
        }
        
        return this;
    }

    @DescriptionAttribute(text = R.ZIP_SAVE_DESCRIPTION)
    public Zip save(String path) throws IOException, DataFormatException
    {
        Path pathToFile = Paths.get(path).toAbsolutePath();
        File file = new File(pathToFile.toString());

        try(ZipOutputStream zipOut = new ZipOutputStream(
                file.exists() ? new FileOutputStream(file) : new FileOutputStream(path)
        )){
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
    
    @DescriptionAttribute(text = R.ZIP_ADD_DESCRIPTION)
    public Zip add(String path) throws Exception
    {
        File file = new File(path);

        if (file.isFile() && file.exists()){
            this.entries.put(file.getName(), compress(getBytesFromFile(file)));
        } else {
            throw new Exception(String.format(R.ZIP_FILE_NOT_EXIST_OR_ITS_DIR.get(), file.getName()));
        }
        return this;
    }

    @DescriptionAttribute(text = R.ZIP_REMOVE_DESCRIPTION)
    public Zip remove(String name)
    {
        this.entries.entrySet().removeIf(e-> e.getKey().equals(name));
        return this;
    }

    @DescriptionAttribute(text = R.ZIP_EXTRACT_DESCRIPTION)
    public Zip extract(String name, String path) throws Exception
    {
        if (this.entries.containsKey(name)) {
            Path p = Paths.get(path).toAbsolutePath();
            if(new File(p.toString()).isDirectory()){
                Path pathToFile = p.resolve(name);
                if(!pathToFile.toFile().exists()){
                    Files.createFile(pathToFile);
                }

                try(FileOutputStream fos = new FileOutputStream(pathToFile.toFile())){
                    byte[] preparedBytes = decompress(this.entries.get(name));
                    fos.write(preparedBytes);
                    fos.close();
                }
            } else {
                throw new Exception(String.format(R.ZIP_PATH_NOT_EXIST_OR_ITS_FILE.get(), path));
            }
        } else {
            throw new Exception(String.format(R.ZIP_NOT_CONTAINS_FILE.get(), name));
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
