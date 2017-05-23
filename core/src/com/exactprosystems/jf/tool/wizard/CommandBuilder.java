package com.exactprosystems.jf.tool.wizard;

import java.util.ArrayList;
import java.util.List;

import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

public class CommandBuilder
{
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
    
    public CommandBuilder addMatrixItem(Matrix matrix, MatrixItem item, int index)
    {
        this.commands.add(context -> 
        {
            System.err.println("addItem(Matrix = " + matrix + " item = " + item + "  index = " + index + ")") ;
        });
        return this;
    }
    
    public CommandBuilder removeMatrixItem(Matrix matrix, MatrixItem item, int index)
    {
        this.commands.add(context -> 
        {
            System.err.println("removeItem(Matrix = " + matrix + " item = " + item + "  index = " + index + ")") ;
        });
        return this;
    }


}
