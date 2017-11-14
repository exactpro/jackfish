package com.exactprosystems.jf;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable
{
    @FXML private Button notEnabledButton;
    @FXML private Button notVisibleButton;
    @FXML private Button clearButton;
    @FXML private Button plusButton;
    @FXML private Label countLabel;
    @FXML private Button showButton;
    @FXML private Button hideButton;
    @FXML private Button colorButton;
    @FXML private Label sliderLabel;
    @FXML private Label pushLabel;
    @FXML private Label pressLabel;
    @FXML private Label selectLabel;
    @FXML private Label moveLabel;
    @FXML private Label checkedLabel;
    @FXML private Slider slider;
    @FXML private MenuBar menu;
    @FXML private Button protocolClear;
    @FXML private TextArea protocol;
    @FXML private TreeView<String> treeView;
    @FXML private CheckBox checkBox;
    @FXML private RadioButton green;
    @FXML private RadioButton yellow;
    @FXML private RadioButton orange;
    @FXML private RadioButton blue;
    @FXML private ComboBox<String> comboBox;
    @FXML private Spinner spinner;
    @FXML private SplitPane splitter;
    @FXML private TabPane tabPanel;
    @FXML private ScrollBar scrollBar;
    @FXML private ProgressBar progressBar;
    @FXML private Pane panel;
    @FXML private TextField textEdit;
    @FXML private ToggleButton toggleButton;
    @FXML private Button any;
    @FXML private Label centralLabel;
    @FXML private ImageView image;
    @FXML private TableView<MockTable.TableData> table;
    @FXML private Button button;

    private static final String CLICK = "_click";
    private static final String DOUBLE_CLICK = "_double_click";
    private static final String MOVE = "_move";
    private static final String UP = "_up_";
    private static final String DOWN = "_down_";
    private static final String PRESS = "_press_";
    private static final String PUSH = "_push";
    private static final String NEW_LINE = "\n";
    private MainModel mainModel;
	public VBox vBox;

	@Override
    public void initialize(URL location, ResourceBundle resources)
    {
        mainModel = new MainModel();
        comboBox.getItems().addAll(mainModel.getData());
        table.getColumns().addAll(mainModel.getTable().getHeaders());
        table.setItems(mainModel.getTable().getTableData());
        treeView.setRoot(mainModel.getTree().getRoot());
        menu.getMenus().addAll(mainModel.getMenu().getMenus());
        slider.valueProperty().addListener((observable, oldValue, newValue) -> sliderLabel.setText("Slider_" + String.valueOf(newValue.intValue())));
        textEdit.textProperty().addListener((observable, oldValue, newValue) -> centralLabel.setText("TextEdit_" + newValue));
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkedLabel.setText("CheckBox_" + (newValue ? "checked" : "unchecked")));
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> centralLabel.setText("ComboBox_" + newValue));

		TableView<A> tb = new TableView<>();
		tb.setMinHeight(100);

		tb.getItems().addAll(new A("1", "2"), new A("3", "4"), new A("5", "6"));
		TableColumn<A, String> c1 = new TableColumn<>("A");
		c1.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().a));

		TableColumn<A, String> c2 = new TableColumn<>("B");
		c2.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().b));
		c2.setCellFactory(e -> new TableCell<A, String>(){
			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					BorderPane pane = new BorderPane();
					TextField tf = new TextField("my Item :" + item);
					pane.setLeft(tf);
					pane.setRight(new Button("Click"));
					setGraphic(pane);
					setAlignment(Pos.CENTER_RIGHT);
				}
			}
		});
		tb.getColumns().addAll(c1, c2);
		vBox.getChildren().add(tb);
	}

    class A
	{
		public String a;
		public String b;

		public A(String a, String b)
		{
			this.a = a;
			this.b = b;
		}

		public A()
		{
		}

		public String getA()
		{
			return a;
		}

		public void setA(String a)
		{
			this.a = a;
		}

		public String getB()
		{
			return b;
		}

		public void setB(String b)
		{
			this.b = b;
		}
	}

    public void clickHandler(MouseEvent mouseEvent)
    {
        centralLabel.setText(getFormattedName(mouseEvent.getSource()) + (mouseEvent.getClickCount() == 1 ? CLICK : DOUBLE_CLICK));
    }

    public void moveHandler(MouseEvent mouseEvent)
    {
        moveLabel.setText(getFormattedName(mouseEvent.getSource()) + MOVE);
    }

    public void moveHandlerRadio()
    {
        moveLabel.setText("RadioButton" + MOVE);
    }

    public void pushHandler(ActionEvent actionEvent)
    {
        pushLabel.setText(getFormattedName(actionEvent.getSource()) + PUSH);
    }

    public void doProtocolClear()
    {
        this.protocol.clear();
    }

    public void doShowButton()
    {
        hideButton.setVisible(true);
    }

    public void doHideButton()
    {
        hideButton.setVisible(false);
    }

    public void doClearCount()
    {
        mainModel.clearCounter();
        countLabel.setText(String.valueOf(mainModel.getCounter()));
    }

    public void doPlusCount()
    {
        mainModel.plusCounter();
        countLabel.setText(String.valueOf(mainModel.getCounter()));
    }

    public void releasedHandler(KeyEvent keyEvent)
    {
        protocol.appendText(getFormattedName(keyEvent.getSource()) + UP + keyEvent.getCode().impl_getCode() + NEW_LINE);
    }

    public void pressHandler(KeyEvent keyEvent)
    {
        protocol.appendText(getFormattedName(keyEvent.getSource()) + DOWN + keyEvent.getCode().impl_getCode() + NEW_LINE);
    }

    public void typedHandler(KeyEvent keyEvent)
    {
        protocol.appendText(getFormattedName(keyEvent.getSource()) + PRESS + (int)keyEvent.getCharacter().toCharArray()[0] + NEW_LINE);
    }

    private String getFormattedName(Object object)
    {
        Node node = (Node) object;
        return node.getId().substring(0,1).toUpperCase() + node.getId().substring(1);
    }
}
