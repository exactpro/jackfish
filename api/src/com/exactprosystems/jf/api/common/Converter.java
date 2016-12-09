////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.rowset.serial.SerialBlob;

import com.exactprosystems.jf.api.app.ImageWrapper;

public class Converter
{
    public static Blob filesToBlob(List<String> list) throws Exception
    {
        ByteArrayOutputStream outputStream = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos))
        {
            for (String filename : list)
            {
                Path path = Paths.get(filename);
                byte[] data = Files.readAllBytes(path);
                ZipEntry entry = new ZipEntry(filename);
                entry.setSize(data.length);
                zos.putNextEntry(entry);
                zos.write(data);
                zos.closeEntry();
            }
            outputStream = baos;
        }
        
        Blob blob = new SerialBlob(outputStream.toByteArray());
        return blob;
    }
    
    public static Blob imageToBlob(ImageWrapper wrapper) throws Exception
    {
        if (wrapper == null)
        {
            return null;
        }
        wrapper.clearFile();
        
        ByteArrayOutputStream outputStream = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
                ObjectOutput out = new ObjectOutputStream(bos))
        {
            out.writeObject(wrapper);
            out.flush();
            outputStream = bos;
        }
        
        return new SerialBlob(outputStream.toByteArray());
    }

    public static Object blobToObject(Blob blob) throws Exception
    {
        if (blob == null)
        {
            return null;
        }
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(blob.getBytes(1, (int) blob.length()));
                ObjectInput in = new ObjectInputStream(bis))
        {
            Object a = in.readObject();
            System.out.println(((ImageWrapper)a).getFileName());
            return a;
        }
    }
    
	public static void setFormats(Collection<String> formats)
	{
		if (formats != null)
		{
			formats.forEach(f -> additionFormats.add(new SimpleDateFormat(f.trim())));
		}
	}

	public static byte[] convertToByteArray(Object object)
	{
		if (object == null)
		{
			return null;
		}
		Object[] array = (Object[])object; 
		
		byte[] ret = new byte[array.length];
		for (int i = 0; i < array.length; i++)
		{
			ret[i] = (byte) array[i];
		}
		
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] convertByteArray(Class<T> clazz, Object object) throws Exception
	{
		if (object == null)
		{
			return null;
		}
		byte[] array = (byte[])object; 
		
		T[] ret = (T[])Array.newInstance(clazz, array.length);
		for (int i = 0; i < array.length; i++)
		{
			ret[i] = (T)convertToType(array[i], clazz);
		}
		
		
		return ret;
	}
	

	@SuppressWarnings("unchecked")
	public static <T> T[] convertArray(Class<T> clazz, Object object)
	{
		if (object == null)
		{
			return null;
		}
		Object[] array = (Object[])object; 
		
		T[] ret = (T[])Array.newInstance(clazz, array.length);
		for (int i = 0; i < array.length; i++)
		{
			ret[i] = (T) array[i];
		}
		
		
		return ret;
	}
	
	public static Map<String, String> toStringMap(Map<String, Object> map)
	{
		Map<String, String> ret = new HashMap<String, String>(map.size());
		for(Entry<String, Object> entry : map.entrySet())
		{
			ret.put(entry.getKey(), "" + entry.getValue());
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T convertToType(Object object, Class<T> type) throws Exception
	{
		if (object == null)
		{
			return null;
		}
		
		if (type.isAssignableFrom(object.getClass()))
		{
			return (T)object;
		}
		
		if (type.isAssignableFrom(Character.class))
		{
			return (T)new Character(String.valueOf(object).charAt(0));
		}
		else if (type.isAssignableFrom(String.class))
		{
			return (T)String.valueOf(object);
		}
		else if (type.isAssignableFrom(Date.class))
		{
			return (T)parseDate(String.valueOf(object));
		}
		else if (type.isAssignableFrom(Boolean.class))
		{
			if (object instanceof String)
			{
				return (T)new Boolean(Boolean.parseBoolean(String.valueOf(object)));
			}
			else if (object instanceof Boolean)
			{
				return (T)(Boolean)object;
			}
		}
		else if (type.isAssignableFrom(Integer.class))
		{
			if (object instanceof String)
			{
				return (T)new Integer(Integer.parseInt(String.valueOf(object)));
			}
			else if (object instanceof Number)
			{
				return (T)new Integer(((Number)object).intValue());
			}
		}
		else if (type.isAssignableFrom(Double.class))
		{
			if (object instanceof String)
			{
				return (T)new Double(Double.parseDouble(String.valueOf(object)));
			}
			else if (object instanceof Number)
			{
				return (T)new Double(((Number)object).doubleValue());
			}
		}
		else if (type.isAssignableFrom(BigInteger.class))
		{
			if (object instanceof String)
			{
				return (T)new BigInteger(String.valueOf(object).trim());
			}
			else if (object instanceof Number)
			{
				return (T)new BigInteger(String.valueOf(((Number)object).intValue()));
			}
		}
		else if (type.isAssignableFrom(BigDecimal.class))
		{
			if (object instanceof String)
			{
				return (T)new BigDecimal(String.valueOf(object).trim());
			}
			else if (object instanceof Number)
			{
				return (T)new BigDecimal(String.valueOf(((Number)object).intValue()));
			}
		}

		throw new Exception("Can not convert " + object + " to type " + type);
	}
	
	public static Date parseDate(String date) throws ParseException
	{
		Date ret = null;

		for (DateFormat formatter :  additionFormats)
		{
			try
			{
				ret = formatter.parse(date);
				if (ret != null)
				{
					return ret;
				}
			}
			catch (ParseException e)
			{
				// in real nothing to do 
			}
		}
			
		throw new ParseException("Can not parse date from " + date, 0);
	}

	/**
	 * Added index to duplicate columns
	 * @param headers columns names.
	 * @return for these columns
	 * 		A B A C B A
	 * 	returned
	 * 		A~0 B~0 A~1 C B~1 A~2
	 */
	public static List<String> convertColumns(List<String> headers)
	{
		List<String> result = new ArrayList<>();
		List<String> tempList = new ArrayList<>();
		HashMap<String, Integer> indexes = new HashMap<>();

		for (String column : headers)
		{
			indexes.put(column, null);
		}

		for (String word : headers)
		{
			boolean contains = tempList.contains(word);
			tempList.add(word);
			if (contains)
			{
				Integer index = indexes.get(word);
				if (index == null)
				{
					index = 0;
				}
				indexes.put(word, index + 1);
			}
		}
		HashMap<String, Integer> maxValues = new HashMap<>(indexes);
		for (String tmp : tempList)
		{
			Integer counter = indexes.get(tmp);
			String newValue = tmp;
			if (counter != null)
			{
				newValue += "~" + (maxValues.get(tmp) - counter);
				indexes.replace(tmp, --counter);
			}
			result.add(newValue);
		}
		return result;
	}

	private static List<DateFormat> additionFormats = new ArrayList<DateFormat>();
	
}
