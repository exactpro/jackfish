////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

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

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " <" + getNameProperty() + ">";
	}

    //region AbstractDocument
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
			unmarshaller.setEventHandler(event -> {
				System.out.println("Error in dictionary : " + event);
				return false;
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
    	File file = new File(fileName);

        JAXBContext jaxbContext = JAXBContext.newInstance(GuiDictionaryBean.jaxbContextClasses);

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setListener(new DictionaryMarshallerListener());
        marshaller.marshal(this.bean, file);
		super.save(fileName);
	}

    //endregion

    //region interface IGuiDictionary
    @Override
	public Collection<IWindow> getWindows()
	{
		return new ArrayList<>(this.bean.windows);
	}

	@Override
	public boolean containsWindow(String dialogName)
	{
		return this.bean.windows.stream()
				.map(Window::getName)
				.anyMatch(name -> Str.areEqual(name, dialogName));
	}

	@Override
	public IWindow getWindow(String name)
	{
		return this.bean.windows.stream()
				.filter(window -> window.getName().equals(name))
				.findFirst()
				.orElse(null);
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

	//endregion

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.bean.isChanged();
	}
	
	@Override
	public void saved()
	{
		this.bean.saved();
	}

    //endregion

	//region events methods
	public void fire()
	{
		this.bean.windows.fire();
	}

	public void setOnChangeListener(BiConsumer<Integer, Integer> listener)
	{
		this.bean.windows.setOnChangeListener(listener);
	}

	public void setOnAddListener(BiConsumer<Integer, Window> listener)
	{
		this.bean.windows.setOnAddListener(listener);
	}

	public void setOnAddAllListener(BiConsumer<Integer, Collection<? extends Window>> listener)
	{
		this.bean.windows.setOnAddAllListener(listener);
	}

	public void setOnRemoveListener(BiConsumer<Integer, Window> listener)
	{
		this.bean.windows.setOnRemoveListener(listener);
	}

	public void setOnSetListener(BiConsumer<Integer, Window> listener)
	{
		this.bean.windows.setOnSetListener(listener);
	}

	//endregion

	public void evaluateAll(AbstractEvaluator evaluator)
	{
		this.bean.windows.forEach(window -> window.evaluateAll(evaluator));
	}
	
	public void addWindow(Window window)
    {
        this.bean.windows.add(window);
    }

	public void addWindow(int index, Window window)
	{
		this.bean.windows.add(index, window);
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
