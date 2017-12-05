////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////


package com.exactprosystems.jf.common;

import com.exactprosystems.jf.actions.ReadableValue;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

	public static <T extends Enum<T>> List<ReadableValue> convertEnumsToReadableList(Enum<T>[] array, java.util.function.Function<T, String> description)
	{
		return Arrays.stream(array)
				.map(e -> new ReadableValue(e.getClass().getSimpleName() + "." + e.name(), description.apply(Enum.valueOf(e.getDeclaringClass(), e.name()))))
				.collect(Collectors.toList());
	}

	public static <T extends Enum<T>> List<ReadableValue> convertEnumsToReadableList(Enum<T>[] array)
	{
		return convertEnumsToReadableList(array, en -> "");
	}

}
