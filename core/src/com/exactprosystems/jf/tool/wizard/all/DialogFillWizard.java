package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.gui.DialogFill;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.IContext;

import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.WizardHelper;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.w3c.dom.Document;

import java.awt.*;
import java.util.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;


@WizardAttribute(
        name                = "DialogFill wizard",
        pictureName         = "DialogFillWizard.jpg",
        category            = WizardCategory.MATRIX,
        shortDescription    = "This wizard creates DialogFills.",
        detailedDescription = "This wizard creates DialogFills.",
        experimental        = true,
        strongCriteries     = true,
        criteries           = {MatrixItem.class, MatrixFx.class}
)
public class DialogFillWizard extends AbstractWizard
{
    private MatrixFx currentMatrix;
    private DictionaryFx dictionary;
    private ComboBox<String> storedConnections;
    private ComboBox<String> dialogs;
    private String currentAdapterStore;
    private Collection<IWindow> windows;
    private IWindow currentDialog;
    private MatrixItem currentItem;
    private ListView<String> resultListView;
    private AppConnection appConnection;
    private ImageViewWithScale imageViewWithScale;
    private Document document;
    private ListView<ControlItem> textBoxes;
    private Map<String, String> controlNamesAndValues;
    private IRemoteApplication service;


    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentItem = get(MatrixItem.class, parameters);
        this.controlNamesAndValues = new HashMap<>();

    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {

        this.imageViewWithScale = new ImageViewWithScale();
        this.textBoxes = new ListView<>();
        this.resultListView = new ListView<>();
        this.dialogs = new ComboBox<>();
        this.dialogs.setPrefWidth(300);
        GridPane grid = new GridPane();
        this.storedConnections = new ComboBox<>();
        this.storedConnections.setPrefWidth(300);

        this.storedConnections.setOnShowing(event -> tryCatch(this::displayStores, "Error on update titles"));
        this.storedConnections.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
            {
                this.setCurrentAdapterStore(newValue);
                this.connectToApplicationFromStore(this.currentAdapterStore);
                this.dictionary = (DictionaryFx) this.appConnection.getDictionary();
                this.dialogs.getItems().clear();
                this.windows = dictionary.getWindows();
                this.dialogs.getItems().addAll(windows.stream().map(Object::toString).collect(Collectors.toList()));
            }
        });

        Button scan = new Button("Scan");
        scan.setOnAction(event -> {
            clear();
            this.textBoxes.getItems().forEach(controlItem -> {
                if (controlItem.isOn())
                {
                    fillNamesAndValues(controlItem.getControl());
                }
            });
            this.controlNamesAndValues.forEach((s, s2) -> this.resultListView.getItems().add(s + " : " + s2));
        });

        this.dialogs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.currentDialog = this.dictionary.getWindows().stream().filter(iWindow -> iWindow.getName().equals(newValue)).findFirst().get();
            this.dialogs.valueProperty().set(newValue);
            this.dialogs.getSelectionModel().select(newValue);

            onDialogSelected();
        });
        this.textBoxes.setCellFactory(CheckBoxListCell.forListView(ControlItem::onProperty));

        ColumnConstraints col1 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.LEFT, true);
        ColumnConstraints col2 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.CENTER, true);
        ColumnConstraints col3 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.CENTER, true);

        GridPane.setFillWidth(storedConnections, true);
        GridPane.setFillWidth(dialogs, true);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        grid.add(new Label("Select stored connection: "), 0, 0);
        grid.add(storedConnections, 1, 0);
        grid.add(new Label("Select dialog: "), 2, 0);
        grid.add(dialogs, 3, 0);
        grid.add(this.textBoxes, 3, 1);
        grid.add(scan, 3, 2);
        grid.add(this.resultListView, 3, 3);
        grid.add(this.imageViewWithScale, 0, 1, 3, 3);
        borderPane.setCenter(grid);
    }

    private void clear()
    {
        this.resultListView.getItems().clear();
        this.controlNamesAndValues.clear();
    }

    private void setCurrentAdapterStore(String newValue)
    {
        this.currentAdapterStore = newValue;
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return () -> {
            CommandBuilder builder = CommandBuilder.start();
            return builder.addMatrixItem(this.currentMatrix, this.currentItem, createItem(this.controlNamesAndValues), 0).build();
        };
    }

    @Override
    public boolean beforeRun()
    {

        return true;
    }

    private Rectangle getItemRectangle(Locator owner, Locator element)
    {

        return Common.tryCatch(() -> this.service.getRectangle(owner, element), "Error on get rectangle", null);
    }

    private void displayStores() throws Exception {
        Map<String, Object> storeMap = this.currentMatrix.getFactory().getConfiguration().getStoreMap();
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
        this.currentAdapterStore = storedConnections.getSelectionModel().getSelectedItem();

        this.displayStoreActionControl(stories, this.currentAdapterStore);
    }

    private void displayStoreActionControl(Collection<String> stories, String lastSelectedStore)
    {
        Platform.runLater(() ->
        {
            if (stories != null)
            {
                storedConnections.getItems().setAll(stories);
            }
            storedConnections.getSelectionModel().select(lastSelectedStore);
        });
    }

    private void connectToApplicationFromStore(String idAppStore)
    {
        if (idAppStore != null && !idAppStore.isEmpty())
        {
            this.appConnection = (AppConnection) this.currentMatrix.getFactory().getConfiguration().getStoreMap().get(idAppStore);
            this.service = this.appConnection.getApplication().service();
        }
    }

    private void fillNamesAndValues(IControl control)
    {
        String name = control.getID();
        String value = "";
        if (control.getBindedClass().isAllowed(OperationKind.GET_VALUE))
        {
            value = Common.tryCatch(() -> String.valueOf(control.operate(this.service, this.currentDialog, Do.getValue())
                    .getValue()), "Error on get values from controls.", "");
        }

        String action = getDefaultAction(control, value);
        this.controlNamesAndValues.put(name, action);

    }

    private MatrixItem createItem(Map<String, String> map)
    {
        MatrixItem matrixItem = CommandBuilder.create(currentMatrix, Tokens.Action.get(), DialogFill.class.getSimpleName());
        Parameters params = new Parameters();
        map.forEach((name, value) -> params.add(name, value, TypeMandatory.Extra));

        Common.tryCatch(() -> matrixItem.init(this.currentMatrix, new ArrayList<>(), new HashMap<>(), params), "Error on parameters create");
        return matrixItem;
    }

    private ChangeListener<Boolean> getListenerForControlItems(ControlItem item)
    {
        return (observable, wasSelected, isSelected) -> {
            Rectangle rectangle = item.getRectangle();
            if (rectangle == null)
            {
                return;
            }
            if (isSelected)
            {
                this.imageViewWithScale.showRectangle(rectangle, MarkerStyle.MARK, "", true);
            }
            else
            {
                this.imageViewWithScale.hideRectangle(rectangle, MarkerStyle.MARK);
            }
        };
    }

    private void onDialogSelected()
    {
        List<ControlItem> collect = currentDialog.getControls(IWindow.SectionKind.Run).stream()
                .map(iControl -> new ControlItem(iControl, false)).collect(Collectors.toList());

        ObservableList<ControlItem> objects = FXCollections.observableArrayList(collect);
        this.textBoxes.getItems().clear();
        this.textBoxes.getItems().addAll(objects);
        this.textBoxes.getItems().forEach(controlItem -> controlItem.onProperty().addListener(getListenerForControlItems(controlItem)));

        IControl selfControl = Common.tryCatch(() -> this.currentDialog.getSelfControl(), "Error on get self", null);
        WizardHelper.gainImageAndDocument(this.appConnection, selfControl, (image, doc) ->
        {
            this.imageViewWithScale.displayImage(image);
            this.document = doc;
            List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
            this.imageViewWithScale.setListForSearch(list);
            this.imageViewWithScale.setOnRectangleClick(rectangle -> this.textBoxes.getItems().forEach(controlItem -> {

                Rectangle itemRectangle = controlItem.getRectangle();
                if (rectangle.equals(itemRectangle))
                {
                    controlItem.toggle();
                    if (controlItem.isOn())
                    {
                        this.imageViewWithScale.showRectangle(rectangle, MarkerStyle.MARK, "", true);
                    }
                    else
                    {
                        this.imageViewWithScale.hideRectangle(rectangle, MarkerStyle.MARK);
                    }
                }
            }));
        }, ex ->
        {
            String message = ex.getMessage();
            if (ex.getCause() instanceof JFRemoteException)
            {
                message = ((JFRemoteException) ex.getCause()).getErrorKind().toString();
            }
            DialogsHelper.showError(message);
        });
    }

    private class ControlItem
    {
        private IControl control;

        private final BooleanProperty on = new SimpleBooleanProperty();

        public IControl getControl()
        {
            return this.control;
        }

        private ControlItem(IControl control, boolean on)
        {
            this.control = control;
            setOn(on);

        }

        public final String getName()
        {

            return this.control.getID();
        }

        public final BooleanProperty onProperty()
        {
            return this.on;
        }

        public final boolean isOn()
        {
            return this.onProperty().get();
        }

        public final void setOn(final boolean on)
        {
            this.onProperty().set(on);
        }

        @Override
        public String toString()
        {
            return getName();
        }

        public void toggle()
        {
            this.setOn(!isOn());
        }

        public Rectangle getRectangle()
        {
            Rectangle res = null;
            if (this.control.getBindedClass().isAllowed(OperationKind.IS_VISIBLE))
            {
                Locator ownerLocator = Common.tryCatch(() -> {
                    IControl ownerControl = currentDialog.getOwnerControl(this.control);
                    return ownerControl == null ? null : ownerControl.locator();
                }, "Error on get owner", null);

                res =  getItemRectangle(ownerLocator, this.control.locator());
            }

            return res;
        }
    }

    private String getDefaultAction(IControl control, String value)
    {
        String apostr = "'";
        String endOfTheAction = "()";
        String res = "";
        String doo = "Do.";
        String setNum = "(" + value + ")";
        String setStr = "(" + apostr + value + apostr + ")";
        switch (control.getBindedClass())
        {
            case Any:
            case Dialog:
            case Frame:
            case Image:
            case Label:
            case ListView:
            case MenuItem:
            case Menu:
            case Panel:
            case ScrollBar:
            case Table:
            case Button:
            case Tooltip:
            case Row:
                res = doo + control.getBindedClass().defaultOperation().toString() + endOfTheAction;
                break;
            case CheckBox:
                res = value;
                break;
            case Slider:
            case Spinner:
            case Splitter:
            case ToggleButton:
            case RadioButton:
                res = doo + control.getBindedClass().defaultOperation().toString() + setNum;
                break;
            case TextBox:
            case ComboBox:
            case TabPanel:
                res = doo + control.getBindedClass().defaultOperation().toString() + setStr;
        }

        return res;
    }
}
