package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.gui.DialogFill;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;


@WizardAttribute(
        name = "DialogFill wizard",
        pictureName = "DialogFillWizard.jpg",
        category = WizardCategory.MATRIX,
        shortDescription = "This wizard creates DialogFills.",
        detailedDescription = "This wizard creates DialogFills.",
        experimental = true,
        strongCriteries = true,
        criteries = {MatrixItem.class, MatrixFx.class}
)
public class DialogFillWizard extends AbstractWizard {
    private MatrixFx currentMatrix;
    private DictionaryFx dictionary;
    private ComboBox<String> storedConnections;
    private ComboBox<String> dialogs;
    private Collection<IControl> textBoxes;
    private String currentAdapterStore;
    private ApplicationConnector appConnector;
    private Collection<IWindow> windows;
    private IWindow currentDialog;
    private Map<IControl, String> values;


    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);
        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.dictionary = (DictionaryFx) this.currentMatrix.getDefaultApp().getDictionary();
        this.appConnector = new ApplicationConnector(((Context) context).getFactory());
        this.windows = dictionary.getWindows();
//        this.currentItem =


    }

    @Override
    protected void initDialog(BorderPane borderPane) {


        this.storedConnections = new ComboBox<>();
        this.dialogs = new ComboBox<>();
        GridPane grid = new GridPane();


        this.storedConnections.setOnShowing(event -> tryCatch(this::displayStores, "Error on update titles"));
        this.storedConnections.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
            {
                this.setCurrentAdapterStore(newValue);
                Common.tryCatch(() -> this.connectToApplicationFromStore(this.currentAdapterStore), "Error on connect");
            }
            this.dialogs.getItems().clear();
            this.windows = dictionary.getWindows();
            this.dialogs.getItems().addAll(windows.stream().map(Object::toString).collect(Collectors.toList()));
        });

        dialogs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.currentDialog = this.dictionary.getWindows().stream().filter(iWindow -> iWindow.getName().equals(newValue)).findFirst().get();
            this.textBoxes = currentDialog.getControls(IWindow.SectionKind.Run).stream().filter(iControl -> iControl.getBindedClass().equals(ControlKind.TextBox)).collect(Collectors.toList());
            this.values = getLocatorsValues(this.textBoxes);
        });


        ColumnConstraints col1 = new ColumnConstraints(200, 150, 300, Priority.SOMETIMES, HPos.LEFT, true);
        ColumnConstraints col2 = new ColumnConstraints(200, 150, 300, Priority.SOMETIMES, HPos.LEFT, true);

        GridPane.setFillWidth(storedConnections,true);
        GridPane.setFillWidth(dialogs,true);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(new Label("Select stored connection: "), 0, 0);
        grid.add(storedConnections, 1, 0);
        grid.add(new Label("Select dialog: "), 0, 1);
        grid.add(dialogs, 1, 1);

        borderPane.setCenter(grid);
    }



    private void setCurrentAdapterStore(String newValue) {
        this.currentAdapterStore = newValue;
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        return () -> {
            CommandBuilder builder = CommandBuilder.start();
            this.values.forEach((key, value) -> {
                MatrixItem matrixItem = createItem(key, "'" + value + "'");
                builder.addMatrixItem(this.currentMatrix, this.currentMatrix.getRoot(), matrixItem, 0);
            });
            return builder.build();
        };
    }

    private MatrixItem createItem(IControl key, String value) {
        MatrixItem matrixItem = CommandBuilder.create(this.currentMatrix, Tokens.Action.get(), DialogFill.class.getSimpleName());
        Parameters params = new Parameters();
        params.add(key.getID(),value, TypeMandatory.Extra);
        Common.tryCatch(() -> matrixItem.init(this.currentMatrix, new ArrayList<>(), new HashMap<>(), params), "Error on parameters create");
        return matrixItem;
    }

    @Override
    public boolean beforeRun() {

        return true;
    }

    private void displayStores() throws Exception {
        Map<String, Object> storeMap = this.dictionary.getFactory().getConfiguration().getStoreMap();
        Collection<String> stories = new ArrayList<>();
        if (!storeMap.isEmpty())
        {
            stories.add("");
            storeMap.forEach((s, o) ->
            {
                if (o instanceof AppConnection)
                {
                    stories.add(s);
                }
            });
        }
        String currentAdapterStore = storedConnections.getSelectionModel().getSelectedItem();

        this.displayStoreActionControl(stories, currentAdapterStore);
    }

    private void displayStoreActionControl(Collection<String> stories, String lastSelectedStore) {
        Platform.runLater(() ->
        {
            if (stories != null)
            {
                storedConnections.getItems().setAll(stories);
            }
            storedConnections.getSelectionModel().select(lastSelectedStore);
        });
    }

    private void connectToApplicationFromStore(String idAppStore) throws Exception {
        if (idAppStore == null || idAppStore.isEmpty())
        {
            this.appConnector.setIdAppEntry(null);
            this.appConnector.setAppConnection(null);
        }
        else
        {
            AppConnection appConnection = (AppConnection) this.dictionary.getFactory().getConfiguration().getStoreMap().get(idAppStore);
            this.appConnector.setIdAppEntry(appConnection.getId());
            this.appConnector.setAppConnection(appConnection);
        }
    }

    private Map<IControl, String> getLocatorsValues(Collection<IControl> controls) {

        IRemoteApplication service = this.appConnector.getAppConnection().getApplication().service();

        Map<IControl, String> map = new HashMap<>();
        for (IControl o : controls)
        {
            map.put(o, Common.tryCatch(() -> String.valueOf(o.operate(service, this.currentDialog, Do.getValue())
                    .getValue()), "Error on get values from controls", ""));
        }
        return map;


    }
}
