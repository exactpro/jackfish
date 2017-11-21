////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable
{
    @FXML private ListView<String> listView;
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
	private TableView<A> tb;
	public BorderPane mainPanel;

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
        listView.getItems().addAll(mainModel.getData());

		this.tb = new TableView<>();
		tb.setMinHeight(150);

		tb.getItems().addAll(new A("1", "2"), new A("3", "4"), new A("5", "6"));
		tb.setRowFactory(param -> new TableRow<A>(){
			@Override
			protected void updateItem(A item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					if (Objects.equals(item.a, "1"))
					{
						this.setStyle("-fx-background-color :red");
					}
					else if (item.a.equals("3"))
					{
						this.setStyle("-fx-background-color : green");
					}
					else if (item.a.equals("5"))
					{
						this.setStyle("-fx-background-color : blue");
					}
				}
			}
		});
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
					Button click = new Button("Click");
					click.setOnAction(e ->
					{
						printColors();
					});
					pane.setRight(click);
					setGraphic(pane);
					setAlignment(Pos.CENTER_RIGHT);
				}
			}
		});
		tb.getColumns().addAll(c1, c2);
		vBox.getChildren().add(tb);

		Button btnOpenDialog = new Button("Open dialog");
		btnOpenDialog.setOnAction(e ->
		{
			Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
			dialog.setContentText("Simple content");
			dialog.setHeaderText("Simple header");
			dialog.showAndWait();
		});

		vBox.getChildren().add(btnOpenDialog);

		Group dragGroup = new Group();

		final Text source = new Text(50, 100, "DRAG ME");
		source.setId("source");
		source.setScaleX(2.0);
		source.setScaleY(2.0);

		final Text target = new Text(250, 100, "DROP HERE");
		target.setId("#target");
		target.setScaleX(2.0);
		target.setScaleY(2.0);

		source.setOnDragDetected(event -> {
			System.err.println("set on drag detected : " + event);
			Dragboard db = source.startDragAndDrop(TransferMode.MOVE);
			source.startFullDrag();

			ClipboardContent content = new ClipboardContent();
			content.putString(source.getText());
			db.setContent(content);

			event.consume();
		});

		target.setOnDragOver(event -> {
			System.err.println("set on drag over : " + event);
			if (event.getGestureSource() != target &&
					event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}

			event.consume();
		});

		target.setOnDragEntered(event -> {
			System.err.println("set on drag entered : " + event);
			if (event.getGestureSource() != target &&
					event.getDragboard().hasString()) {
				target.setFill(Color.GREEN);
			}

			event.consume();
		});

		target.setOnDragExited(event -> {
			System.err.println("set on drag exited : " + event);
			target.setFill(Color.BLACK);

			event.consume();
		});

		target.setOnDragDropped(event -> {
			System.err.println("set on drag dropped : " + event);
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasString()) {
				target.setText(db.getString());
				success = true;
			}
			event.setDropCompleted(success);

			event.consume();
		});

		source.setOnDragDone(event -> {
			System.err.println("set on drag done : " + event);
			if (event.getTransferMode() == TransferMode.MOVE) {
				source.setText("");
			}

			event.consume();
		});

		dragGroup.getChildren().add(source);
		dragGroup.getChildren().add(target);
		Button resetDrag = new Button("Reset");
		resetDrag.setOnAction(e -> {
			source.setText("DRAG ME");
			target.setText("DROP HERE");
		});
		BorderPane bpDrag = new BorderPane();
		bpDrag.setCenter(dragGroup);
		bpDrag.setRight(resetDrag);
		vBox.getChildren().add(bpDrag);
	}

	private void printColors()
	{
		for (int i = 0; i < tb.getItems().size(); i++)
		{
			Node cell = (Node) tb.queryAccessibleAttribute(AccessibleAttribute.CELL_AT_ROW_COLUMN, i, 0);
			if (cell instanceof TableCell<?,?>)
			{
				TableRow tableRow = ((TableCell) cell).getTableRow();
				System.out.println(tableRow.getBackground().getFills().stream().map(bf -> bf.getFill()).collect(Collectors.toList()));
			}
		}
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
		this.printColors();
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
