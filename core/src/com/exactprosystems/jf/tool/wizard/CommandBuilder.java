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

    public CommandBuilder loadDocument(DocumentKind kind, String name)
    {
        this.commands.add(context -> 
        {
            Common.tryCatch(() -> 
            { 
                try (Reader reader = CommonHelper.readerFromFileName(name))
                {
                    Document doc = context.getFactory().createDocument(kind, name);
                    doc.load(reader);
                }
            }, "Error on load " + name);
        });
        return this;
    }

	public CommandBuilder saveDocument(Document doc)
	{
		this.commands.add(context -> 
		{
			Common.tryCatch(() -> 
			{ 
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
