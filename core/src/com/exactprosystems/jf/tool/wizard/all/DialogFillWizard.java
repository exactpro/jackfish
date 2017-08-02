package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.exactprosystems.jf.tool.Common.tryCatch;


@WizardAttribute(
        name 				= "DialogFill wizard",
        pictureName 		= "DialogFillWizard.jpg",
        category 			= WizardCategory.MATRIX,
        shortDescription 	= "This wizard creates DialogFills.",
        detailedDescription = "This wizard creates DialogFills.",
        experimental 		= true,
        strongCriteries 	= true,
        criteries 			= {MatrixItem.class, MatrixFx.class}
)
public class DialogFillWizard extends AbstractWizard{
    private MatrixFx currentMatrix;
    private AppConnection appConnection;
    private DictionaryFx dictionary;
    private Collection<IWindow> windows;

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);
        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.dictionary = (DictionaryFx) this.currentMatrix.getDefaultApp().getDictionary();



    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        DialogsHelper.showDialogFillDialog(this.dictionary);

    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        return null;
    }

    @Override
    public boolean beforeRun() {

        return true;
    }
}
