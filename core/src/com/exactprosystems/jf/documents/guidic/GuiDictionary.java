////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.xml.schema.Xsd;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.DocumentKind;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


@DocumentInfo(
        kind = DocumentKind.GUI_DICTIONARY,
        newName = "NewDictionary",
        extentioin = "xml",
        description = "Gui dictionary"
)
public class GuiDictionary extends AbstractDocument implements IGuiDictionary
{
    private GuiDictionaryBean bean;
    
	public GuiDictionary()
	{
		this(null, null);
	}

	public GuiDictionary(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
		this.bean = new GuiDictionaryBean();
	}

    //------------------------------------------------------------------------------------------------------------------
    // Object
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " <" + getName() + ">";
	}

    //------------------------------------------------------------------------------------------------------------------
    // interface Document
    //------------------------------------------------------------------------------------------------------------------
    @Override
	public void load(Reader reader) throws Exception
	{
		try
		{
			super.load(reader);

			JAXBContext jaxbContext = JAXBContext.newInstance(GuiDictionaryBean.jaxbContextClasses);

			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Source schemaFile = new StreamSource(Xsd.class.getResourceAsStream("GuiDictionary.xsd"));
			Schema schema = schemaFactory.newSchema(schemaFile);

			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			unmarshaller.setSchema(schema);
			unmarshaller.setEventHandler(new ValidationEventHandler()
			{
				@Override
				public boolean handleEvent(ValidationEvent event)
				{
					System.out.println("Error in dictionary : " + event);
					return false;
				}
			});

			unmarshaller.setListener(new DictionaryUnmarshallerListener());

			this.bean = (GuiDictionaryBean) unmarshaller.unmarshal(reader);
		}
		catch (UnmarshalException e)
		{
			throw new Exception(e.getCause().getMessage(), e.getCause());
		}
	}

    @Override
    public boolean canClose() throws Exception
    {
    	return true;
    }

    @Override
    public void save(String fileName) throws Exception
    {
    	super.save(fileName);

    	File file = new File(fileName);

        JAXBContext jaxbContext = JAXBContext.newInstance(GuiDictionaryBean.jaxbContextClasses);

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setListener(new DictionaryMarshallerListener());
        marshaller.marshal(this, file);
    }

    //------------------------------------------------------------------------------------------------------------------
    // interface IGuiDictionary
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Collection<IWindow> getWindows()
    {
    	List<IWindow> result = new ArrayList<IWindow>();
    	for (IWindow window : this.bean.windows)
    	{
    		result.add(window);
    	}
    	return result;
    }

	@Override
	public boolean containsWindow(String dialogName)
	{
		for (Window window : this.bean.windows)
		{
			if (Str.areEqual(dialogName, window.getName()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IWindow getWindow(String name)
	{
    	Iterator<Window> iterator = this.bean.windows.iterator();
    	while (iterator.hasNext())
    	{
    		Window window = iterator.next();
    		if (window.getName().equals(name))
    		{
    			return window;
    		}
    	}
    	return null;
	}

	@Override
	public IWindow getFirstWindow()
	{
		if (this.bean.windows == null || this.bean.windows.isEmpty())
		{
			return null;
		}
		return this.bean.windows.get(0);
	}
	//------------------------------------------------------------------------------------------------------------------
	// interface Mutable
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		for (Window window : this.bean.windows)
		{
			if (window.isChanged())
			{
				return true;
			}
		}
		return this.bean.windows.isChanged();
	}
	
	@Override
	public void saved()
	{
		for (Window window : this.bean.windows)
		{
			window.saved();
		}
		this.bean.windows.saved();
	}

    //------------------------------------------------------------------------------------------------------------------
	public void evaluateAll(AbstractEvaluator evaluator)
	{
		for (Window window : this.bean.windows)
		{
			window.evaluateAll(evaluator);
		}
	}
	
	public void addWindow(Window window)
    {
        this.bean.windows.add(window);
    }

	public void addWindow(int index, Window window)
	{
		this.bean.windows.add(index, window);
	}

    public void removeWindowByName(String name)
    {
    	Iterator<Window> iterator = this.bean.windows.iterator();
    	while (iterator.hasNext())
    	{
    		Window window = iterator.next();
    		if (window.getName().equals(name))
    		{
    			iterator.remove();
    			return;
    		}
    	}
    }

	public int indexOf(IWindow window)
	{
		Iterator<Window> iterator = this.bean.windows.iterator();
		int res = 0;
		while (iterator.hasNext())
		{
			Window next = iterator.next();
			if (next.equals(window))
			{
				return res;
			}
			res++;
		}
		return -1;
	}

	public void removeWindow(IWindow window)
	{
		Iterator<Window> iterator = this.bean.windows.iterator();
		while (iterator.hasNext())
		{
			Window next = iterator.next();
			if (next.equals(window))
			{
				iterator.remove();
				return;
			}
		}
	}
}
