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


package com.exactprosystems.jf.common;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.IFactory;
import com.exactprosystems.jf.api.common.IPool;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommonHelper
{
    private final static String UTF8 = "UTF-8"; 

    private CommonHelper()
	{}
    
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

	/**
	 * Use this method for load factory for any IPool
	 * @param clazz from which will taking class loader
	 * @param factoryClass instance this class will return
	 * @param jarName path to jar, which will loaded
	 * @param exceptionSupplier exception, if something will wrong
	 * @param logger for notice the main exception
	 * @return instance of loaded IFactory
	 *
	 * @see IFactory
	 * @see IPool
	 */
	public static <R extends IFactory, X extends Throwable> R loadFactory(Class<? extends IPool> clazz, Class<R> factoryClass, String jarName, Supplier<X> exceptionSupplier, Logger logger) throws X
	{
		if (!new File(jarName).exists() || !new File(jarName).isFile())
		{
			throw exceptionSupplier.get();
		}
		try
		{
			ClassLoader parent = clazz.getClassLoader();
			URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:" + jarName)}, parent);

			ServiceLoader<R> loader = ServiceLoader.load(factoryClass, classLoader);
			Iterator<R> iterator = loader.iterator();
			R factory = null;
			if (iterator.hasNext())
			{
				factory = iterator.next();
			}
			if (factory == null)
			{
				throw exceptionSupplier.get();
			}
			return factory;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw exceptionSupplier.get();
		}
	}
}
