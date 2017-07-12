package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import org.apache.log4j.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
            System.err.println("## ++ " + matrix + " " + what + " " + where + " " + index);
            matrix.insert(where, index, what);
            where.init(matrix, matrix);
        });
        return this;
    }
    
    public CommandBuilder removeMatrixItem(Matrix matrix, MatrixItem what)
    {
        this.commands.add(context -> 
        {
            System.err.println("## -- " + matrix + " " + what + " ##=" + matrix.hashCode());
            matrix.getRoot().bypass(System.err::println);
            matrix.remove(what);
            matrix.getRoot().bypass(System.err::println);
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

	public CommandBuilder replaceControl(Section section, IControl oldControl, IControl newControl)
	{
		this.commands.add(context -> 
		{ 
		    section.replaceControl(oldControl, newControl);
		});
		return this;
	}
	
    public CommandBuilder createDocument(DocumentKind kind, String name)
    {
        this.commands.add(context -> 
        {
            Common.tryCatch(() -> 
            { 
                Document doc = context.getFactory().createDocument(kind, name);
            }, "Error on create " + name + " of kind " + kind);
        });
        return this;
    }

    public CommandBuilder loadDocument(Document doc)
    {
        this.commands.add(context -> 
        {
            Common.tryCatch(() -> 
            { 
                try (Reader reader = CommonHelper.readerFromFileName(doc.getName()))
                {
                    doc.load(reader);
                }
            }, "Error on load " + doc.getName());
        });
        return this;
    }

	public CommandBuilder saveDocument(Document doc)
	{
		this.commands.add(context -> 
		{
			Common.tryCatch(() -> 
			{ 
	            System.err.println("## save " + " ##=" + doc.hashCode());
			    doc.save(doc.getName());
			    doc.close(context.getFactory().getSettings());
			}, "Error on save " + doc.getName());
		});
		return this;
	}

	public boolean isEmpty()
	{
		return this.commands.isEmpty();
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


}
