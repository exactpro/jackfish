package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.tables.TableLoadFromFile;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import javafx.scene.layout.BorderPane;

import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
        name 				= "TableWizard ",
        pictureName 		= "TableWizard.jpg",
        category 			= WizardCategory.MATRIX,
        shortDescription 	= "This wizard work with table",
        detailedDescription = "This wizard work with table",
        experimental 		= true,
        strongCriteries 	= false,
        criteries 			= { ActionItem.class, MatrixFx.class }
)
public class TableWizard extends AbstractWizard
{
    private ActionItem actionItem;
    private Table table;
    private char delimeter;
    private String fileName;

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);
        this.actionItem = super.get(ActionItem.class, parameters);
    }

    @Override
    public boolean beforeRun()
    {
        if (this.actionItem.getActionClass() != TableLoadFromFile.class || !checkParameters(this.actionItem.getParameters()))
        {
            return false;
        }

        try
        {
            this.table = new Table(this.fileName, this.delimeter, this.context.getEvaluator());
        }
        catch (Exception e)
        {
            DialogsHelper.showError("Error while table creating. Please check file name or file extension.");
        }
        return true;
    }

    private boolean checkParameters(Parameters parameters)
    {
        if(parameters.getByName(TableLoadFromFile.fileName).getExpression().isEmpty())
        {
            DialogsHelper.showError("File name for table wasn't choiced");
            return false;
        }

        try
        {
            this.fileName = super.context.getEvaluator().evaluate(parameters.getByName(TableLoadFromFile.fileName).getExpression()).toString();
        }
        catch (Exception e)
        {
            DialogsHelper.showError("Error in filename");
            return false;
        }

        if(parameters.getByName(TableLoadFromFile.delimiterName) == null)
        {
            this.delimeter = DefaultValuePool.Semicolon.toString().charAt(0);
        }
        else
        {
            this.delimeter = parameters.getByName(TableLoadFromFile.delimiterName).toString().charAt(0);
        }

        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
        borderPane.setPrefSize(800.0, 600.0);
        borderPane.setMinSize(800.0, 600.0);
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return null;
    }
}
