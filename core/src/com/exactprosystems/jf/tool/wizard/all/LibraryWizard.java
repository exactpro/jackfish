package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
        name            = "Library wizard",
        pictureName         = "GherkinWizard.jpg",
        category            = WizardCategory.MATRIX,
        shortDescription    = "This wizard helps refactor libraries and  matrix.",
        experimental 		= true,
        strongCriteries     = true,
        criteries           = {MatrixFx.class, SubCase.class},
        detailedDescription = ""
)


public class LibraryWizard extends AbstractWizard {

    private MatrixFx currentMatrix;
    private SubCase currentSubCase;
    private String oldSubId;
    private String newSubId;
    private String newNameSpaceName;
    private String oldNameSpaceName;
    private String newFileName;

    @Override
    public boolean beforeRun() {
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.setMaxHeight(300);
        Label label1 = new Label("Name of SubCase:");
        Label label2 = new Label("Name of NameSpace:");
        Label label3 = new Label("Name of a new library file:");

        label1.setTooltip(new Tooltip("Change the field to give a new name for the SubCase"));
        label2.setTooltip(new Tooltip("Select the NameSpace where SubCase will be moved"));
        label3.setTooltip(new Tooltip("Use if you need to create NameSpace in a new file"));

        TextField nameOfSub = new TextField(currentSubCase.getId());
        TextField nameOfNameSpace = new TextField(oldNameSpaceName);
        TextField nameOfaNewFile = new TextField();

        GridPane pane = new GridPane();
        pane.setVgap(15);
        pane.setHgap(5);

        pane.add(label1, 0, 0);
        pane.add(label2, 0, 1);
        pane.add(label3, 0, 2);
        pane.add(nameOfSub, 1, 0);
        pane.add(nameOfNameSpace, 1, 1);
        pane.add(nameOfaNewFile, 1, 2);

        borderPane.setCenter(pane);

        nameOfSub.textProperty().addListener((observable, oldValue, newValue) -> this.newSubId = newValue);
        nameOfNameSpace.textProperty().addListener((observable, oldValue, newValue) -> this.newNameSpaceName = newValue);
        nameOfaNewFile.textProperty().addListener((observable, oldValue, newValue) -> this.newFileName = newValue);


    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        return null;
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentSubCase = super.get(SubCase.class, parameters);
        this.oldSubId = currentSubCase.getId();
        this.oldNameSpaceName = currentSubCase.getParent().getClass() == NameSpace.class ? currentSubCase.getParent().getId() : "";

    }


    private void changeSubId(String id) {
       currentSubCase.setId(new MutableValue<>(id));
    }

    private void moveToNameSpace() {

    }
}
