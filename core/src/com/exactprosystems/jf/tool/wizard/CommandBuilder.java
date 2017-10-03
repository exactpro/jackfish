////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import org.apache.log4j.Logger;

import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class CommandBuilder
{
    private static final Logger logger = Logger.getLogger(CommandBuilder.class);

    private List<WizardCommand> commands = new ArrayList<>();
    
    private CommandBuilder()
    {}
    
    public static CommandBuilder start()
    {
        return new CommandBuilder();
    }
    
    public List<WizardCommand> build()
    {
        return this.commands;
    }

    public CommandBuilder refreshConfig(Configuration config)
    {
        this.commands.add(context -> Common.tryCatch(config::refresh, R.WIZARD_ERROR_ON_CONFIG_REFRESH.get()));
        return this;
    }

    public CommandBuilder print(String message)
    {
        this.commands.add(context -> 
        {
        	context.getOut().println(message);
        });
        return this;
    }

    public CommandBuilder addMatrixItem(Matrix matrix, MatrixItem where, MatrixItem what, int index)
    {
        this.commands.add(context -> 
        {
            matrix.insert(where, index, what);
            where.init(matrix, matrix);
        });
        return this;
    }
    
    public CommandBuilder removeMatrixItem(Matrix matrix, MatrixItem what)
    {
        this.commands.add(context -> 
        {
            matrix.remove(what);
        });
        return this;
    }

    public CommandBuilder handleMatrixItem(MatrixItem item, Consumer<MatrixItem> consumer)
    {
        this.commands.add(context -> 
        {
            consumer.accept(item);
        });
        return this;
    }

    public CommandBuilder findAndHandleMatrixItem(Matrix matrix, int number, Consumer<MatrixItem> consumer)
    {
        this.commands.add(context -> 
        {
            matrix.getRoot().find(i -> i.getNumber() == number).ifPresent(i -> consumer.accept(i));
        });
        return this;
    }

	public CommandBuilder clipboard(String string)
	{
        this.commands.add(context -> 
        {
    		Sys.copyToClipboard(string);
        });
		return this;
	}

    public CommandBuilder saveTable(Table table, String fileName, char delimiter)
    {
        this.commands.add(context -> table.save(fileName, delimiter, false, false));
        return this;
    }

	public CommandBuilder replaceControl(Section section, IControl oldControl, IControl newControl)
	{
		this.commands.add(context -> 
		{ 
		    section.replaceControl(oldControl, newControl);
		});
		return this;
	}

	public CommandBuilder displayControl(DictionaryFx dictionaryFx, Window window, Section section, IControl control)
	{
		this.commands.add(context -> Common.tryCatch(() -> dictionaryFx.displayElement(window, section.getSectionKind(), control), R.WIZARD_ERROR_ON_SHOW_ELEMENT.get()));
		return this;
	}

	public CommandBuilder replaceWindow(DictionaryFx dictionaryFx, Window oldWindow, Window newWindow)
	{
		this.commands.add(context ->
		{
			//TODO think about why index is -1 after second open wizard
			int index = dictionaryFx.indexOf(oldWindow);
			dictionaryFx.removeWindow(oldWindow);
			dictionaryFx.addWindow(index, newWindow);
		});
		return this;
	}

	public CommandBuilder displayWindow(DictionaryFx dictionaryFx, Window window)
	{
		this.commands.add(context -> Common.tryCatch(() -> {
			dictionaryFx.displayDialog(window, dictionaryFx.getWindows());
			dictionaryFx.displayElement(window, IWindow.SectionKind.Run, window.getFirstControl(IWindow.SectionKind.Run));
		}, R.WIZARD_ERROR_ON_DISPLAY_WINDOW.get()));
		return this;
	}
	
    public CommandBuilder createDocument(DocumentKind kind, String name)
    {
        this.commands.add(context ->
        {
            Common.tryCatch(() ->
            {
                Document doc = context.getFactory().createDocument(kind, name);
            }, MessageFormat.format(R.WIZARD_ERROR_ON_CREATE_DOCUMENT_2.get(), name, kind));
        });
        return this;
    }

    public CommandBuilder loadDocument(Document doc)
    {
        this.commands.add(context ->
        {
            Common.tryCatch(() ->
            {
                try (Reader reader = CommonHelper.readerFromFileName(doc.getNameProperty().get()))
                {
                    doc.load(reader);
                }
            }, MessageFormat.format(R.WIZARD_ERROR_ON_LOAD_DOC_1.get(), doc.getNameProperty().get()));
        });
        return this;
    }

	public CommandBuilder saveDocument(Document doc)
	{
		this.commands.add(context ->
		{
			Common.tryCatch(() ->
			{
			    doc.save(doc.getNameProperty().get());
			    doc.close();
			}, MessageFormat.format(R.WIZARD_ERROR_ON_CREATE_DOC_1.get(), doc.getNameProperty().get()));
		});
		return this;
	}

	public boolean isEmpty()
	{
		return this.commands.isEmpty();
	}

	public CommandBuilder removeParameters(MatrixItem item, int[] indexes)
	{
		this.commands.add(context ->
		{
			Integer[] array = new Integer[indexes.length];
			IntStream.range(0, indexes.length)
					.forEach(i -> array[i] = indexes[i]);
			Arrays.sort(array, Comparator.reverseOrder());
			Arrays.stream(array).mapToInt(i -> i).forEach(item.getParameters()::remove);
		});
		return this;
	}

	public CommandBuilder addParameter(MatrixItem item, Parameter parameter, int index)
	{
		this.commands.add(context ->
		{
			if (index == -1)
			{
				item.getParameters().add(parameter);
			}
			else
			{
				item.getParameters().add(index, parameter);
			}
		});
		return this;
	}

    public static MatrixItem create(Matrix matrix, String itemName, String actionName)
    {
		try
        {
			MatrixItem res = Parser.createItem(itemName, actionName);
			res.init(matrix, matrix);
			return res;
		}
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public CommandBuilder storeGlobal(String name, Object value, Configuration config) {

        this.commands.add(context -> config.storeGlobal(name, value));

        return this;
    }
}
