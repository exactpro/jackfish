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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.w3c.dom.Document;

import java.awt.*;
import java.util.*;

import java.util.List;
import java.util.function.Predicate;
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
public class DialogFillWizard extends AbstractWizard {
    private MatrixFx              currentMatrix;
    private DictionaryFx          dictionary;
    private ComboBox<String>      storedConnections;
    private ComboBox<String>      dialogs;
    private String                currentAdapterStore;
    private Collection<IWindow>   windows;
    private IWindow               currentDialog;
    private MatrixItem            currentItem;
    private ListView<String>      resultListView;
    private AppConnection         appConnection;
    private ImageViewWithScale    imageViewWithScale;
    private Document              document;
    private ListView<ControlItem> controls;
    private Map<String, String>   controlNamesAndValues;
    private IRemoteApplication    service;
    private int                   dialogXOffset;
    private int                   dialogYOffset;


    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);
        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentItem = get(MatrixItem.class, parameters);
        this.controlNamesAndValues = new HashMap<>();

    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        this.imageViewWithScale = new ImageViewWithScale();
        this.controls = new ListView<>();
        ObservableList<String> objects = FXCollections.observableArrayList();
        this.resultListView = new ListView<>(objects);
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

        this.resultListView.setCellFactory(param -> new MyCell());

        Button scan = new Button("Scan");
        scan.setOnAction(event -> {
            clear();
            this.controls.getItems().forEach(controlItem -> {
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
        this.controls.setCellFactory(CheckBoxListCell.forListView(ControlItem::onProperty));

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
        grid.add(this.controls, 3, 1);
        grid.add(scan, 3, 2);
        grid.add(this.resultListView, 3, 3);
        grid.add(this.imageViewWithScale, 0, 1, 3, 3);
        borderPane.setCenter(grid);
    }

    private void clear() {
        this.resultListView.getItems().clear();
        this.controlNamesAndValues.clear();
    }

    private void setCurrentAdapterStore(String newValue) {
        this.currentAdapterStore = newValue;
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        return () -> {
            CommandBuilder builder = CommandBuilder.start();
            Map<String, String> map = new LinkedHashMap<>();
            for (String o : this.resultListView.getItems())
            {
                if (map.put(o.split(" : ")[0], o.split(" : ")[1]) != null)
                {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            return builder.addMatrixItem(this.currentMatrix, this.currentItem, createItem(map),0).build();

        };
    }

    @Override
    public boolean beforeRun() {
        return true;
    }

    private Rectangle getItemRectangle(Locator owner, Locator element) {

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

    private void connectToApplicationFromStore(String idAppStore) {
        if (idAppStore != null && !idAppStore.isEmpty())
        {
            this.appConnection = (AppConnection) this.currentMatrix.getFactory().getConfiguration().getStoreMap().get(idAppStore);
            this.service = this.appConnection.getApplication().service();
        }
    }

    private void fillNamesAndValues(IControl control) {
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

    private MatrixItem createItem(Map<String, String> map) {
        MatrixItem matrixItem = CommandBuilder.create(currentMatrix, Tokens.Action.get(), DialogFill.class.getSimpleName());
        Parameters params = new Parameters();
        map.forEach((name, value) -> params.add(name, value, TypeMandatory.Extra));

        Common.tryCatch(() -> matrixItem.init(this.currentMatrix, new ArrayList<>(), new HashMap<>(), params), "Error on parameters create");
        return matrixItem;
    }

    private ChangeListener<Boolean> getListenerForControlItems(ControlItem item) {
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

    private void onDialogSelected() {
        IControl selfControl = Common.tryCatch(() -> this.currentDialog.getSelfControl(), "Error on get self", null);

        Rectangle selfRectangle = Common.tryCatch(() -> this.service.getRectangle(selfControl.locator(), selfControl.locator()), "Error on get self Rectangle", null);

        this.dialogXOffset = selfRectangle.x;
        this.dialogYOffset = selfRectangle.y;

        Predicate<IControl> predicate = (IControl control) -> {
            switch (control.getBindedClass())
            {
                case TextBox:
                case Button:
                case CheckBox:
                case RadioButton:
                case Label:
                case TabPanel:
                case Spinner:
                    return true;
                default:
                    return false;
            }
        };

        List<ControlItem> collect = currentDialog.getControls(IWindow.SectionKind.Run).stream().filter(predicate)
                .map(iControl -> new ControlItem(iControl, false)).collect(Collectors.toList());

        ObservableList<ControlItem> objects = FXCollections.observableArrayList(collect);
        this.controls.getItems().clear();
        this.controls.getItems().addAll(objects);
        this.controls.getItems().forEach(controlItem -> controlItem.onProperty().addListener(getListenerForControlItems(controlItem)));

        WizardHelper.gainImageAndDocument(this.appConnection, selfControl, (image, doc) ->
        {
            this.imageViewWithScale.displayImage(image);
            this.document = doc;
            List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
            this.imageViewWithScale.setListForSearch(list);
            this.imageViewWithScale.setOnRectangleClick(rectangle -> this.controls.getItems().forEach(controlItem -> {

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

    private class ControlItem {
        private IControl control;

        private final BooleanProperty on = new SimpleBooleanProperty();

        public IControl getControl() {
            return this.control;
        }

        private ControlItem(IControl control, boolean on) {
            this.control = control;
            setOn(on);
        }

        public final String getName() {
            return this.control.getID();
        }

        public final BooleanProperty onProperty() {
            return this.on;
        }

        public final boolean isOn() {
            return this.onProperty().get();
        }

        public final void setOn(final boolean on) {
            this.onProperty().set(on);
        }

        @Override
        public String toString() {
            return getName();
        }

        public void toggle() {
            this.setOn(!isOn());
        }

        public Rectangle getRectangle() {
            Rectangle res = null;
            if (this.control.getBindedClass().isAllowed(OperationKind.IS_VISIBLE))
            {
                Locator ownerLocator = Common.tryCatch(() -> {
                    IControl ownerControl = currentDialog.getOwnerControl(this.control);
                    return ownerControl == null ? null : ownerControl.locator();
                }, "Error on get owner", null);

                res = getItemRectangle(ownerLocator, this.control.locator());
                if (res != null)
                {
                    res.setRect(res.x - dialogXOffset, res.y - dialogYOffset, res.width, res.height);
                }
            }

            return res;
        }
    }

    private String getDefaultAction(IControl control, String value) {
        String apostr = "'";
        String endOfTheAction = "()";
        String res = "";
        //cause do is a key word
        String dO = "Do.";
        String setNum = "(" + value + ")";
        String setStr = "(" + apostr + value + apostr + ")";
        switch (control.getBindedClass())
        {
            case Label:
            case Button:
                res = dO + control.getBindedClass().defaultOperation().toString() + endOfTheAction;
                break;
            case CheckBox:
                res = value;
                break;
            case Spinner:
            case ToggleButton:
            case RadioButton:
                res = dO + control.getBindedClass().defaultOperation().toString() + setNum;
                break;
            case TextBox:
            case ComboBox:
            case TabPanel:
                res = dO + control.getBindedClass().defaultOperation().toString() + setStr;
        }

        return res;
    }

    private class MyCell extends ListCell<String> {

        public MyCell() {

//            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//            setAlignment(Pos.CENTER);

            setOnDragDetected(event -> {
                if (getItem() == null) {
                    return;
                }

                ObservableList<String> items = getListView().getItems();

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem());

                String s = getListView().getItems().get(items.indexOf(getItem()));
                dragboard.setDragView(new Label(s).snapshot(null,null));

                dragboard.setContent(content);

                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this &&
                        event.getDragboard().hasString())
                {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

//            setOnDragEntered(event -> {
//                if (event.getGestureSource() != this &&
//                        event.getDragboard().hasString())
//                {
//                    setOpacity(0.3);
//                }
//            });
//
//            setOnDragExited(event -> {
//                if (event.getGestureSource() != this &&
//                        event.getDragboard().hasString())
//                {
//                    setOpacity(1);
//                }
//            });

            setOnDragDropped(event -> {
                if (getItem() == null)
                {
                    return;
                }

                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString())
                {

                    ObservableList<String> items = getListView().getItems();
                    int draggedIdx = items.indexOf(db.getString());
                    int thisIdx = items.indexOf(getItem());

                    items.set(draggedIdx, getItem());
                    items.set(thisIdx, db.getString());

                    List<String> itemscopy = new ArrayList<>(getListView().getItems());
                    getListView().getItems().setAll(itemscopy);

                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
                setGraphic(null);
            }
            else
            {
                setText(item);
            }
        }

    }
}
