package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
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
import com.exactprosystems.jf.tool.wizard.WizardMatcher;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.WizardHelper;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private MatrixFx                 currentMatrix;
    private DictionaryFx             dictionary;
    private ComboBox<ConnectionBean> storedConnections;
    private ComboBox<IWindow>        dialogs;
    private IWindow                  currentDialog;
    private MatrixItem               currentItem;
    private ListView<ResultBean>     resultListView;
    private AppConnection            appConnection;
    private ImageViewWithScale       imageViewWithScale;
    private Document                 document;
    private ListView<ControlItem>    controls;
    private IRemoteApplication       service;
    private WizardMatcher            wizardMatcher;
    private Text                     text;
    private GridPane                 grid;


    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentItem = get(MatrixItem.class, parameters);
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
        grid = new GridPane();
        this.imageViewWithScale = new ImageViewWithScale();
        this.controls = new ListView<>();
        this.controls.setOnKeyPressed(event ->
        {
            if (event.getCode() == KeyCode.SPACE)
            {
                this.controls.getSelectionModel().getSelectedItem().toggle();
            }
        });
        this.resultListView = new ListView<>(FXCollections.observableArrayList());
        this.resultListView.tooltipProperty().set(new Tooltip("Use drag and drop to reorder elements"));
        this.dialogs = new ComboBox<>();
        this.dialogs.setPrefWidth(300);
        this.storedConnections = new ComboBox<>();
        this.storedConnections.setPrefWidth(300);

        this.storedConnections.setOnShowing(event -> tryCatch(this::displayStores, "Error on update titles"));
        this.storedConnections.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            this.connectToApplicationFromStore(newValue);
            if (this.appConnection != null)
            {
            this.dictionary = (DictionaryFx) this.appConnection.getDictionary();
            this.dialogs.getItems().clear();
            this.dialogs.getItems().setAll(dictionary.getWindows());
            }
        });

        this.resultListView.setCellFactory(param -> new MyCell());

        Button scan = new Button("Scan");
        scan.setOnAction(event ->
        {
            this.resultListView.getItems().clear();
            this.controls.getItems().forEach(controlItem ->
            {
                if (controlItem.isOn())
                {
                    fillNamesAndValues(controlItem.getControl());
                }
            });
        });

        this.dialogs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            this.currentDialog = newValue;
            this.dialogs.valueProperty().set(newValue);
            this.dialogs.getSelectionModel().select(newValue);

            onDialogSelected();
        });
        this.controls.setCellFactory(CheckBoxListCell.forListView(ControlItem::onProperty));

        ColumnConstraints col1 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.CENTER, true);
        ColumnConstraints col2 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.CENTER, true);
        ColumnConstraints col3 = new ColumnConstraints(300, 300, 300, Priority.SOMETIMES, HPos.CENTER, true);

        GridPane.setFillWidth(storedConnections, true);
        GridPane.setFillWidth(dialogs, true);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        text = new Text("Please select stored connection and dialog");

        grid.add(new Label("Select stored connection: "), 0, 0);
        grid.add(this.storedConnections, 1, 0);
        grid.add(new Label("Select dialog: "), 2, 0);
        grid.add(this.dialogs, 3, 0);
        grid.add(this.controls, 3, 1);
        grid.add(scan, 3, 2);
        grid.add(this.resultListView, 3, 3);
        grid.add(text, 0, 1, 3, 3);

        borderPane.setCenter(grid);
    }


    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return () ->
        {
            CommandBuilder builder = CommandBuilder.start();
            return builder.addMatrixItem(this.currentMatrix, this.currentItem, createItem(resultListView.getItems()), 0).build();
        };
    }

    @Override
    public boolean beforeRun()
    {
        return true;
    }

    private void displayStores() throws Exception
    {
        ObservableList<ConnectionBean> items = this.storedConnections.getItems();
        Map<String, Object> storeMap = this.currentMatrix.getFactory().getConfiguration().getStoreMap();
        Collection<ConnectionBean> connections = storeMap.entrySet().stream().filter(entry ->
                entry.getValue() instanceof AppConnection).map(entry -> new ConnectionBean(entry.getKey(), (AppConnection) entry.getValue())).collect(Collectors.toList());
        items.setAll(connections);

        List<ConnectionBean> connectionsFromPool = new ArrayList<>();
        for (ConnectionBean connectionBean : items)
        {
            connectionsFromPool = this.currentMatrix.getFactory().getConfiguration().getApplicationPool()
                    .getConnections().stream().filter(connection -> !connection.equals(connectionBean.getConnection()))
                    .map(connection -> new ConnectionBean(connection.getId(), connection)).collect(Collectors.toList());

        }
        items.addAll(connectionsFromPool);
    }

    private void connectToApplicationFromStore(ConnectionBean bean)
    {
        if (bean != null)
        {
            this.appConnection = this.storedConnections.getItems().stream().filter(connectionBean -> connectionBean.equals(bean))
                    .map(ConnectionBean::getConnection).findFirst().orElse(null);
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

        String operation = getDefaultAction(control, value);
        this.resultListView.getItems().add(new ResultBean(name, operation));
    }


    private MatrixItem createItem(List<ResultBean> beans)
    {
        MatrixItem matrixItem = CommandBuilder.create(currentMatrix, Tokens.Action.get(), DialogFill.class.getSimpleName());
        if (matrixItem != null)
        {
            beans.forEach(bean -> matrixItem.getParameters().add(bean.getControlName(), bean.getOperation(), TypeMandatory.Extra));
            matrixItem.createId();
        }

        return matrixItem;
    }

    private ChangeListener<Boolean> getListenerForControlItems(ControlItem item)
    {
        return (observable, wasSelected, isSelected) ->
        {

            Rectangle rectangle = item.getRectangle(wizardMatcher);
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
        if (this.currentDialog == null)
        {
            return;
        }
        IControl selfControl = Common.tryCatch(() -> this.currentDialog.getSelfControl(), "Error on get self", null);

        Predicate<IControl> predicate = (IControl control) ->
        {
            Addition addition = control.getAddition();

            if (addition != null && addition.equals(Addition.Many))
            {
                return false;
            }
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
        this.grid.getChildren().remove(text);
        this.grid.getChildren().remove(this.imageViewWithScale);
        this.grid.add(this.imageViewWithScale, 0, 1, 3, 3);

        WizardHelper.gainImageAndDocument(this.appConnection, selfControl, (image, doc) ->
        {
            this.imageViewWithScale.displayImage(image);
            this.document = doc;

            List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
            this.imageViewWithScale.setListForSearch(list);

            PluginInfo info = this.appConnection.getApplication().getFactory().getInfo();
            this.wizardMatcher = new WizardMatcher(info);

            this.imageViewWithScale.setOnRectangleClick(rectangle -> this.controls.getItems().forEach(controlItem ->
            {

                Rectangle itemRectangle = controlItem.getRectangle(wizardMatcher);
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

        public Rectangle getRectangle(WizardMatcher wizardMatcher)
        {
            return Common.tryCatch(() ->
            {
                List<Node> all = wizardMatcher.findAll(document, this.control.locator());
                return ((Rectangle) all.get(0).getUserData(IRemoteApplication.rectangleName));
            }, "Error on get rectangle", null);
        }
    }

    private String getDefaultAction(IControl control, String value)
    {
        String apostr = "'";
        String endOfTheAction = "()";
        StringBuilder res = new StringBuilder();
        String dO = Do.class.getSimpleName() + ".";
        String setNum = "(" + value + ")";
        String setStr = "(" + apostr + value + apostr + ")";
        switch (control.getBindedClass())
        {
            case Label:
            case Button:
                res.append(dO).append(control.getBindedClass().defaultOperation().toString()).append(endOfTheAction);
                break;
            case CheckBox:
                res.append(value);
                break;
            case Spinner:
            case ToggleButton:
            case RadioButton:
                res.append(dO).append(control.getBindedClass().defaultOperation().toString()).append(setNum);
                break;
            case TextBox:
            case ComboBox:
            case TabPanel:
                res.append(dO).append(control.getBindedClass().defaultOperation().toString()).append(setStr);
        }

        return res.toString();
    }

    private class MyCell extends ListCell<ResultBean>
    {

        public MyCell()
        {

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            setOnDragDetected(event ->
            {
                if (getItem() == null)
                {
                    return;
                }
                ObservableList<ResultBean> items = getListView().getItems();

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(items.indexOf(getItem())));

                String s = getItem().toString();
                dragboard.setDragView(new Text(s).snapshot(null, null));

                dragboard.setContent(content);

                event.consume();
            });

            setOnDragOver(event ->
            {
                if (event.getGestureSource() != this &&
                        event.getDragboard().hasString())
                {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            setOnDragEntered(event ->
            {
                if (event.getGestureSource() != this &&
                        event.getDragboard().hasString())
                {
                    setOpacity(0.3);
                }
            });

            setOnDragExited(event ->
            {
                if (event.getGestureSource() != this &&
                        event.getDragboard().hasString())
                {
                    setOpacity(1);
                }
            });

            setOnDragDropped(event ->
            {
                if (getItem() == null)
                {
                    return;
                }

                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString())
                {
                    ObservableList<ResultBean> items = getListView().getItems();
                    int from = Integer.parseInt(db.getString());
                    int where = items.indexOf(getItem());

                    ResultBean what = items.get(from);

                    items.remove(what);
                    items.add(where,what);

                    List<ResultBean> itemscopy = new ArrayList<>(getListView().getItems());
                    getListView().getItems().setAll(itemscopy);

                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        protected void updateItem(ResultBean item, boolean empty)
        {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
                setGraphic(null);
            }
            else
            {
                setGraphic(new Text(item.toString()));
            }
        }

    }

    class ResultBean
    {
        String controlName;
        String operation;

        private ResultBean(String controlName, String operation)
        {
            this.controlName = controlName;
            this.operation = operation;
        }

        public String getControlName()
        {
            return controlName;
        }

        public String getOperation()
        {
            return operation;
        }

        @Override
        public String toString()
        {
            return controlName + " : " + operation;
        }
    }

    class ConnectionBean
    {
        private String name;
        private AppConnection connection;

        private ConnectionBean(String name, AppConnection connection)
        {
            this.name = name;
            this.connection = connection;
        }

        public String getName()
        {
            return name;
        }

        public AppConnection getConnection()
        {
            return connection;
        }

        @Override
        public String toString()
        {
            return getName();
        }
    }
}