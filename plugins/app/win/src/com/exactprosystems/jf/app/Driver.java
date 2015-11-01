////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.Keyboard;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.MouseAction;
import com.exactprosystems.jf.api.app.OperationResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Driver
{

	public Driver(Logger logger, OutputStream output, InputStream input)
	{
		this.logger = logger;
		this.output = new BufferedWriter(new OutputStreamWriter(output));
		this.input = new BufferedReader(new InputStreamReader(input));
	}
	
	public static Method byName(Class<?> clazz, String name)
	{
		for (Method method : clazz.getDeclaredMethods())
		{
			if (method.getName().equals(name))
			{
				return method;
			}
		}
		return null;
	}

	
	public void driverSendKey(Keyboard key) throws Exception
    {
        sendKey(key);
    }
	
    public void driverMouseClick(UIProxy proxy, MouseAction action, int x, int y) throws Exception
    {
		mouseClick(proxyId(proxy), action, x, y);
    }

	
    public UIProxy driverFindFirst(UIProxy proxy, WindowTreeScope scope, WindowProperty property, Object value) throws Exception
    {
    	Number[] res = findFirst(proxyId(proxy), scope.name(), (long)property.getId(), value);
		if (res != null)
		{
			return new UIProxy(res);
		}
		return null;
    }
    
    public List<UIProxy> driverFindAllForLocator(ControlKind controlKind, UIProxy proxy, Locator locator) throws Exception
    {
    	List<Number[]> found = findAllForLocator(controlKind, proxyId(proxy), locator);
    	return toUIProxyList(found);
    }

    public UIProxy driverFindFirstForLocator(ControlKind controlKind, UIProxy proxy, Locator locator) throws Exception
    {
    	Number[] res = findFirstForLocator(controlKind, proxyId(proxy), locator);
		if (res != null)
		{
			return new UIProxy(res);
		}
		return null;
    }

	public List<UIProxy> driverFindAll(UIProxy proxy, WindowTreeScope scope, WindowProperty property, Object value) throws Exception
    {
    	List<Number[]> found = findAll(proxyId(proxy), scope.name(), (long)property.getId(), value);
    	return toUIProxyList(found);
    }
    
	public String driverDoPatternCall(UIProxy proxy, WindowPattern pattern, String method, Object ... args) throws Exception
	{
		return doPatternCall(proxyId(proxy), pattern.getId(), method, args);
	}

    public String driverGetProperty(UIProxy proxy, WindowProperty property) throws Exception
    {
    	return getProperty(proxyId(proxy), property.getId());
    }
    
	public List<WindowPattern> driverGetPatterns(UIProxy proxy) throws Exception
	{
		List<WindowPattern> res = new ArrayList<WindowPattern>();
		
    	List<Number> found = getPatterns(proxy.getId());
    	if (found != null)
    	{
    		for (Number element : found)
    		{
    			res.add(WindowPattern.byId(element.intValue()));
    		}
    	}

    	return res;
	}
    
	public List<WindowProperty> driverGetProperties(UIProxy proxy) throws Exception
	{
		List<WindowProperty> res = new ArrayList<WindowProperty>();
		
    	List<Number> found = getProperties(proxy.getId());
    	if (found != null)
    	{
    		for (Number element : found)
    		{
    			res.add(WindowProperty.byId(element.intValue()));
    		}
    	}

    	return res;
	}
	
	public DataTable driverGetDataTable(UIProxy component, Locator additional, KindInformation kind) throws Exception
	{
		return getDataTable(component.getId(), additional, kind);
	}
    
	public <T> T translate(Class<T> type, Method method, Object... par) throws Exception
	{
		try
		{
			Request requestJson = new Request();
			requestJson.setNameMethod(method.getName());
			Map<String, Object> map = new HashMap<>();

			Annotation[][] parameterAnnotations = method.getParameterAnnotations();
			for (int i = 0; i < parameterAnnotations.length; i++)
			{
				Annotation[] annotations = parameterAnnotations[i];
				for (Annotation annotation : annotations)
				{
					String name = ((Name) annotation).name();
                    if (par[i] instanceof Locator)
                    {
                        Field operation = par[i].getClass().getDeclaredField("operation");
                        operation.setAccessible(true);
                        operation.set(par[i], null);
                    }
					map.put(name, par[i]);
				}
			}
			requestJson.setParameters(map);

			return getResult(type, requestJson);
		}
		catch (JsonSyntaxException e)
		{
			logger.error("JSON Error:");
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		catch (Exception ex)
		{
			logger.error("ERROR translate(" + type + ", " + method + ", " + Arrays.toString(par) + ")");
			throw ex;
		}
	}

    
    
    
	private void sendKey(@Name(name = "key")Keyboard key) throws Exception
    {
		translate(Object.class, sendKey, key);
    }
 
    private void mouseClick(@Name(name = "id")long[] id, @Name(name = "action")MouseAction action, @Name(name = "x")int x, @Name(name = "y")int y) throws Exception
    {
		translate(Object.class, mouseClick, id, action, x, y);
    }
	
    private Number[] findFirst(@Name(name = "id")long[] id, @Name(name = "scope")String scope, @Name(name = "propertyId")long propertyId, @Name(name = "value")Object value) throws Exception
    {
		return translate(Number[].class, findFirst, id, scope, propertyId, value);
    }
	
	private List<Number[]> findAllForLocator(@Name(name = "controlKind")ControlKind controlKind, @Name(name = "id")long[] id, @Name(name = "locator")Locator locator) throws Exception
	{
		return translate(ArrayList.class, findAllForLocator, controlKind, id, locator);
	}
    
	private Number[] findFirstForLocator(@Name(name = "controlKind")ControlKind controlKind, @Name(name = "id")long[] id, @Name(name = "locator")Locator locator) throws Exception
	{
		return translate(Number[].class, findFirstForLocator, controlKind, id, locator);
	}
    
    private List<Number[]> findAll(@Name(name = "id")long[] id, @Name(name = "scope")String scope, @Name(name = "propertyId")long propertyId, @Name(name = "value")Object value) throws Exception
    {
		return translate(ArrayList.class, findAll, id, scope, propertyId, value);
    }

	private String doPatternCall(@Name(name = "id")long[] id, @Name(name = "patternId")long patternId, @Name(name = "method")String method, @Name(name = "args")Object ... args) throws Exception
	{
		return translate(String.class, doPatternCall, id, patternId, method, args);
	}
	
	private String getProperty(@Name(name = "id")long[] id, @Name(name = "propertyId")long propertyId) throws Exception
    {
    	return 	translate(String.class, getProperty, id, propertyId);
    }
	
	private List<Number> getPatterns(@Name(name = "id")long[] id) throws Exception
	{
		return translate(ArrayList.class, getPatterns, id);
	}
	
	private List<Number> getProperties(@Name(name = "id")long[] id) throws Exception
	{
		return translate(ArrayList.class, getProperties, id);
	}

	private DataTable getDataTable(@Name(name = "id")long[] id, @Name(name = "additional")Locator additional, @Name(name = "kind")KindInformation kind) throws Exception
	{
		return translate(DataTable.class, getDataTable, id, additional, kind);
	}
	
	
	private <T> T getResult(Class<T> type, Request request) throws Exception
	{
		Gson requestJson = new GsonBuilder().create();
		String responseJson = sendReceive(requestJson.toJson(request, Request.class));
		if (responseJson == null)
		{
			return null;
		}

		Type fooType = null;
		GsonBuilder gsonBuilder = new GsonBuilder();

		if (type == Locator.class)
		{
			fooType = new TypeToken<Response<Locator>>()
			{
			}.getType();
		}
		else if (type == DataTable.class)
		{
			fooType = new TypeToken<Response<DataTable>>()
			{
			}.getType();
		}
		else if (type == ImageWrapper.class)
		{
			fooType = new TypeToken<Response<ImageWrapper>>()
			{
			}.getType();
		}
		else if (type == OperationResult.class)
		{
			fooType = new TypeToken<Response<OperationResult>>()
			{
			}.getType();
		}
		else if (type == String.class)
		{
			fooType = new TypeToken<Response<String>>()
			{
			}.getType();
		}
		else if (type == Integer.class)
		{
			fooType = new TypeToken<Response<Integer>>()
			{
			}.getType();
		}
		else if (type == Number[].class)
		{
			fooType = new TypeToken<Response<Number[]>>()
			{
			}.getType();
		}
		else
		{
			fooType = new TypeToken<Response<Object>>()
			{
			}.getType();
		}

		Gson json = gsonBuilder.create();

		Response<T> response = json.fromJson(responseJson, fooType);
		if (response.getException() != null)
		{
			throw new Exception(response.getException());
		}

		return response.getReturnValue();
	}

	private String sendReceive(String str) throws IOException
	{
		logger.trace(">> " + str);
		this.output.write(str);
		output.newLine();
		output.flush();
		String res = input.readLine();
		logger.trace("<< " + res);
		return res;
	}

    private long[] proxyId(UIProxy proxy)
	{
    	if (proxy != null)
    	{
    		return proxy.getId();
    	}
		return null;
	}
    
    private List<UIProxy> toUIProxyList(List<Number[]> found)
    {
    	List<UIProxy> res = new ArrayList<UIProxy>();

		if (found != null)
    	{
    		for (Object element : found)
    		{
    			if (element instanceof Number[])
    			{
    				Number[] id  = (Number[])element;
    				res.add(new UIProxy(id));
    			}
    			else if (element instanceof List)
    			{
    				List id  = (List)element;
    				res.add(new UIProxy(id));
    			}
    		}
    	}
    	
    	return res;
    }
    
    
    private static Method	sendKey				= byName(Driver.class, "sendKey");
    private static Method	mouseClick			= byName(Driver.class, "mouseClick");
	private static Method	findAllForLocator	= byName(Driver.class, "findAllForLocator");
	private static Method	findFirstForLocator	= byName(Driver.class, "findFirstForLocator");
	private static Method	findAll				= byName(Driver.class, "findAll");
	private static Method	findFirst			= byName(Driver.class, "findFirst");
	private static Method	doPatternCall		= byName(Driver.class, "doPatternCall");
	private static Method	getProperty			= byName(Driver.class, "getProperty");
	private static Method	getPatterns			= byName(Driver.class, "getPatterns");
	private static Method	getProperties		= byName(Driver.class, "getProperties");
	private static Method	getDataTable		= byName(Driver.class, "getDataTable");
	
	

    private BufferedWriter	output;
	private BufferedReader	input;

	private Logger			logger	= null;

}
