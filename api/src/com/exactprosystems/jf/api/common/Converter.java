////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.sql.rowset.serial.SerialBlob;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Converter
{
	private static final String CLASS_NAME = "class.name";
	private static final String XML_DOCUMENT = "document.xml";

	//region methods with xml document
	public static byte[] convertXmlDocumentToZipByteArray(Document tree) throws Exception
	{
		byte[] bytes = convertToByteArray(tree);

		ByteArrayOutputStream outputStream;

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos))
		{
			ZipEntry zipEntry = new ZipEntry(XML_DOCUMENT);
			zos.putNextEntry(zipEntry);
			zos.write(bytes, 0, bytes.length);
			zos.closeEntry();
			outputStream = baos;
		}
		byte[] newBytes = outputStream.toByteArray();
		return newBytes;
	}

	public static Document convertByteArrayToXmlDocument(byte[] array) throws Exception
	{
		try (InputStream in = new ByteArrayInputStream(array); ZipInputStream zis = new ZipInputStream(in))
		{
			ZipEntry nextEntry = zis.getNextEntry();
			if (nextEntry == null)
			{
				throw new Exception("Not entry found");
			}
			String name = nextEntry.getName();
			if (name.equals(XML_DOCUMENT))
			{
				byte[] buf = readAll(zis);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(new ByteArrayInputStream(buf));
				convertAttributesToUserData(doc);
				return doc;
			}
			else
			{
				throw new Exception("Now entry with name " + XML_DOCUMENT);
			}
		}
	}

	private static byte[] convertToByteArray(Document tree) throws Exception
	{
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream())
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Source source = new DOMSource(tree);
			StreamResult result = new StreamResult(bos);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.transform(source, result);
			return bos.toByteArray();
		}
	}

	private static void convertAttributesToUserData(Node element)
	{
		boolean isElement = element.getNodeType() == Node.ELEMENT_NODE;
		if (isElement)
		{
			Element e = (Element) element;
			String rect = e.getAttribute(IRemoteApplication.rectangleName);
			if (!Str.IsNullOrEmpty(rect))
			{
				e.removeAttribute(IRemoteApplication.rectangleName);
				Rectangle rectangle = rectangleFromString(rect);
				e.setUserData(IRemoteApplication.rectangleName, rectangle, null);
			}
			String visible = e.getAttribute(IRemoteApplication.visibleName);
			if (!Str.IsNullOrEmpty(visible))
			{
				e.removeAttribute(IRemoteApplication.visibleName);
				e.setUserData(IRemoteApplication.visibleName, Boolean.valueOf(visible), null);
			}
		}
		IntStream.range(0, element.getChildNodes().getLength()).mapToObj(element.getChildNodes()::item).forEach(Converter::convertAttributesToUserData);
	}

	public static String rectangleToString(Rectangle r)
	{
		return "" + r.x + "," + r.y + "," + r.width + "," + r.height;
	}

	public static Rectangle rectangleFromString(String s)
	{
		String[] split = s.split(",");
		return new Rectangle(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
	}
	//endregion

	//region work with SQL blobs
	public static Blob storableToBlob(Storable object) throws Exception
	{
		if (object == null)
		{
			return null;
		}
		List<String> list = object.getFileList();
		ByteArrayOutputStream outputStream = null;

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos))
		{
			{
				byte[] data = object.getClass().getName().getBytes();
				ZipEntry entry = new ZipEntry(CLASS_NAME);
				entry.setSize(data.length);
				zos.putNextEntry(entry);
				zos.write(data);
				zos.closeEntry();
			}

			for (String filename : list)
			{
				byte[] data = object.getData(filename);
				ZipEntry entry = new ZipEntry(filename);
				entry.setSize(data.length);
				zos.putNextEntry(entry);
				zos.write(data);
				zos.closeEntry();
			}
			outputStream = baos;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return new SerialBlob(outputStream.toByteArray());
	}
	
	public static Storable blobToStorable(Blob blob) throws Exception
	{
		if (blob == null)
		{
			return null;
		}
		
		Storable retValue = null;
		try (InputStream in = blob.getBinaryStream(); 
				ZipInputStream zis = new ZipInputStream(in))
		{
			ZipEntry nextEntry = null;
			while((nextEntry = zis.getNextEntry()) != null)
			{
				String name = nextEntry.getName();
				if (retValue == null)
				{
					if (name.equals(CLASS_NAME))
					{
						byte[] buf = readAll(zis);
						Class<?> clazz = Class.forName(new String(buf));
						retValue = (Storable) clazz.newInstance();
						zis.closeEntry();
						continue;
					}
					else
					{
						throw new Exception("Wrong structure");
					}
				}
				
				retValue.addFile(name, readAll(zis));
				zis.closeEntry();
			}
		}
		catch (Exception e)
		{ 
			e.printStackTrace();
		}
		
		return retValue;
	}

	// TODO 
    public static byte[] blobToByteArray(Blob blob) throws Exception
    {
        if (blob == null)
        {
            return null;
        }
        
        return readAll(blob.getBinaryStream());
    }
	
	//endregion

	private static byte[] readAll(InputStream is) throws IOException
	{
        byte[] res = new byte[is.available()];
        is.read(res);
        return res;
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
		Object[] array = (Object[]) object;

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
		byte[] array = (byte[]) object;

		T[] ret = (T[]) Array.newInstance(clazz, array.length);
		for (int i = 0; i < array.length; i++)
		{
			ret[i] = (T) convertToType(array[i], clazz);
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
		Object[] array = (Object[]) object;

		T[] ret = (T[]) Array.newInstance(clazz, array.length);
		for (int i = 0; i < array.length; i++)
		{
			ret[i] = (T) array[i];
		}


		return ret;
	}

	public static Map<String, String> toStringMap(Map<String, Object> map)
	{
		Map<String, String> ret = new HashMap<String, String>(map.size());
		for (Entry<String, Object> entry : map.entrySet())
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
			return (T) object;
		}

		if (type.isAssignableFrom(Character.class))
		{
			return (T) new Character(String.valueOf(object).charAt(0));
		}
		else if (type.isAssignableFrom(String.class))
		{
			return (T) String.valueOf(object);
		}
		else if (type.isAssignableFrom(Date.class))
		{
			return (T) parseDate(String.valueOf(object));
		}
		else if (type.isAssignableFrom(Boolean.class))
		{
			if (object instanceof String)
			{
				return (T) new Boolean(Boolean.parseBoolean(String.valueOf(object)));
			}
			else if (object instanceof Boolean)
			{
				return (T) (Boolean) object;
			}
		}
		else if (type.isAssignableFrom(Integer.class))
		{
			if (object instanceof String)
			{
				return (T) new Integer(Integer.parseInt(String.valueOf(object)));
			}
			else if (object instanceof Number)
			{
				return (T) new Integer(((Number) object).intValue());
			}
		}
        else if (type.isAssignableFrom(Long.class))
        {
            if (object instanceof String)
            {
                return (T) new Long(Long.parseLong(String.valueOf(object)));
            }
            else if (object instanceof Number)
            {
                return (T) new Long(((Number) object).longValue());
            }
        }
		else if (type.isAssignableFrom(Double.class))
		{
			if (object instanceof String)
			{
				return (T) new Double(Double.parseDouble(String.valueOf(object)));
			}
			else if (object instanceof Number)
			{
				return (T) new Double(((Number) object).doubleValue());
			}
		}
		else if (type.isAssignableFrom(BigInteger.class))
		{
			if (object instanceof String)
			{
				return (T) new BigInteger(String.valueOf(object).trim());
			}
			else if (object instanceof Number)
			{
				return (T) new BigInteger(String.valueOf(((Number) object).intValue()));
			}
		}
		else if (type.isAssignableFrom(BigDecimal.class))
		{
			if (object instanceof String)
			{
				return (T) new BigDecimal(String.valueOf(object).trim());
			}
			else if (object instanceof Number)
			{
				return (T) new BigDecimal(String.valueOf(((Number) object).intValue()));
			}
		}

		throw new Exception("Cannot convert " + object + " of type " + object.getClass() + " to type " + type);
	}

	public static Date parseDate(String date) throws ParseException
	{
		Date ret = null;

		for (DateFormat formatter : additionFormats)
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
	 *
	 * @param headers columns names.
	 * @return for these columns
	 * A B A C B A
	 * returned
	 * A~0 B~0 A~1 C B~1 A~2
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
