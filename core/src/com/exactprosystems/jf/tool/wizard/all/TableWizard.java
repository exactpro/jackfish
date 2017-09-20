package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.tables.TableLoadFromFile;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetView;
import com.exactprosystems.jf.tool.custom.grideditor.TableDataProvider;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

@WizardAttribute(
        name 				= "TableWizard ",
        pictureName 		= "TableWizard.png",
        category 			= WizardCategory.MATRIX,
        shortDescription 	= "This wizard makes it easier to work with tables.",
        detailedDescription = "You can change the table directly in the matrix, without having to need other tools.",
        experimental 		= false,
        strongCriteries 	= false,
        criteries 			= { ActionItem.class, MatrixFx.class }
)
public class TableWizard extends AbstractWizard
{
    private ActionItem actionItem;
    private Table      table;
    private char       delimiter;
    private String     fileName;

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
            this.table = new Table(this.fileName, this.delimiter, this.context.getEvaluator());
        }
        catch (Exception e)
        {
            DialogsHelper.showError("Error while table creating. Please check file name or file extension.");
            return false;
        }
        return true;
    }

    private boolean checkParameters(Parameters parameters)
    {
        if (parameters.getByName(TableLoadFromFile.fileName).getExpression().isEmpty())
        {
            DialogsHelper.showError("No file name specified.");
            return false;
        }

        try
        {
            this.fileName = super.context.getEvaluator().evaluate(parameters.getByName(TableLoadFromFile.fileName).getExpression()).toString();
        }
        catch (Exception e)
        {
            DialogsHelper.showError("Error in filename.");
            return false;
        }

        if (!this.fileName.contains("csv"))
        {
            DialogsHelper.showError("The file '" + this.fileName + "' is not a CSV.");
            return false;
        }

        if (!new File(this.fileName).canWrite())
        {
            DialogsHelper.showError("The file '" + this.fileName + "' is read-only and can't be changed.");
            return false;
        }

        if (parameters.getByName(TableLoadFromFile.delimiterName) == null)
        {
            this.delimiter = DefaultValuePool.Semicolon.toString().charAt(0);
        }
        else
        {
            try
            {
                this.delimiter = super.context.getEvaluator().evaluate(parameters.getByName(TableLoadFromFile.delimiterName).getExpression()).toString().charAt(0);
            }
            catch (Exception e)
            {
                DialogsHelper.showError("Delimiter is incorrect or empty.");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
        DataProvider<String> provider = new TableDataProvider(this.table, (undo, redo) -> this.actionItem.getMatrix().addCommand(undo, redo));
        SpreadsheetView spreadsheetView = new SpreadsheetView(provider);
        provider.displayFunction(spreadsheetView::display);
        borderPane.setCenter(spreadsheetView);
        borderPane.setPrefSize(800.0, 600.0);
        borderPane.setMinSize(800.0, 600.0);
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return () -> CommandBuilder
                .start()
                .saveTable(this.table, this.fileName, this.delimiter)
                .build();
    }
}
