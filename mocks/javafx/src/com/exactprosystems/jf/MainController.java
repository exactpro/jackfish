/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable
{
	@FXML private GridPane gridPane;
	@FXML private ListView<String> List;
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
    @FXML private Slider Slider;
    @FXML private MenuBar menu;
    @FXML private Button ProtocolClear;
    @FXML private TextArea Protocol;
    @FXML private TreeView<String> Tree;
    @FXML private CheckBox CheckBox;
    @FXML private RadioButton RadioButton;
    @FXML private RadioButton Yellow;
    @FXML private RadioButton Orange;
    @FXML private RadioButton Blue;
    @FXML private ComboBox<String> ComboBox;
    @FXML private Spinner Spinner;
    @FXML private SplitPane Splitter;
    @FXML private TabPane TabPanel;
    @FXML private ScrollBar ScrollBar;
    @FXML private ProgressBar ProgressBar;
    @FXML private Pane Panel;
    @FXML private TextField TextBox;
    @FXML private ToggleButton ToggleButton;
    @FXML private Button Any;
    @FXML private Label CentralLabel;
    @FXML private ImageView Image;
    @FXML private TableView<MockTable.TableData> Table;
    @FXML private Button Button;

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
        Tree.setOnScrollTo(event -> scrollPrint(Tree.getId()));
        List.setOnScrollTo(event -> scrollPrint(List.getId()));
        ComboBox.getItems().addAll(mainModel.getData());
        if(ComboBox.isEditable())
		{
			TextField boxEditor = ComboBox.getEditor();
			boxEditor.setOnKeyPressed(event -> pressHandlerCustom(event, ComboBox.getId()));
			boxEditor.setOnKeyTyped(event -> typeHandlerCustom(event, ComboBox.getId()));
			boxEditor.setOnKeyReleased(event -> releaseHandlerCustom(event, ComboBox.getId()));
		}
		else
		{
			ComboBox.setOnKeyReleased(this::releasedHandler);
			ComboBox.setOnKeyPressed(this::pressHandler);
			ComboBox.setOnKeyTyped(this::typedHandler);
		}
        Table.getColumns().addAll(mainModel.getTable().getHeaders());
        Table.setItems(mainModel.getTable().getTableData());
        Tree.setRoot(mainModel.getTree().getRoot());
        menu.getMenus().addAll(mainModel.getMenu().getMenus());
        Slider.valueProperty().addListener((observable, oldValue, newValue) -> sliderLabel.setText("Slider_" + String.valueOf(newValue.intValue())));
        TextBox.textProperty().addListener((observable, oldValue, newValue) -> CentralLabel.setText("TextBox_" + newValue));
        CheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> checkedLabel.setText("CheckBox_" + (newValue ? "checked" : "unchecked")));
        ComboBox.valueProperty().addListener((observable, oldValue, newValue) -> CentralLabel.setText("ComboBox_" + newValue));
        List.getItems().addAll(mainModel.getData());

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
        CentralLabel.setText(getFormattedName(mouseEvent.getSource()) + (mouseEvent.getClickCount() == 1 ? CLICK : DOUBLE_CLICK));
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
		this.Protocol.clear();
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
        Protocol.appendText(getFormattedName(keyEvent.getSource()) + UP + keyEvent.getCode().impl_getCode() + NEW_LINE);
    }

	public void scrollPrint(String text)
	{
		Protocol.appendText(text + "_scroll" + NEW_LINE);
	}

	public void moveHandlerCustom(MouseEvent mouseEvent, String text)
	{
		moveLabel.setText(text + MOVE);
	}

	public void clickHandlerCustom(MouseEvent mouseEvent, String text)
	{
		CentralLabel.setText(text + (mouseEvent.getClickCount() == 1 ? CLICK : DOUBLE_CLICK));
	}

	private void releaseHandlerCustom(KeyEvent keyEvent, String text)
	{
		Protocol.appendText(text + UP + keyEvent.getCode().impl_getCode() + NEW_LINE);
	}

    private void pressHandlerCustom(KeyEvent keyEvent, String text)
    {
        Protocol.appendText(text + DOWN + keyEvent.getCode().impl_getCode() + NEW_LINE);
    }

    private void typeHandlerCustom(KeyEvent keyEvent, String text)
    {
        Protocol.appendText(text + PRESS + (int)keyEvent.getCharacter().toCharArray()[0] + NEW_LINE);
    }

    public void pressHandler(KeyEvent keyEvent)
    {
        Protocol.appendText(getFormattedName(keyEvent.getSource()) + DOWN + keyEvent.getCode().impl_getCode() + NEW_LINE);
    }

    public void typedHandler(KeyEvent keyEvent)
    {
        Protocol.appendText(getFormattedName(keyEvent.getSource()) + PRESS + (int)keyEvent.getCharacter().toCharArray()[0] + NEW_LINE);
    }

    private String getFormattedName(Object object)
    {
        Node node = (Node) object;
		return "CentralLabel".equalsIgnoreCase(node.getId()) ? "Label" : node.getId();
    }

    public void init()
	{
		((ComboBoxListViewSkin) ComboBox.getSkin()).getListView().setOnScrollTo(event -> scrollPrint(ComboBox.getId()));
		Table.setEditable(true);
		showDialog();
		ScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> sliderLabel.setText("ScrollBar_" + newValue.intValue()));
	}

	private void showDialog()
	{
		ButtonType button = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
		Dialog dialog = new Dialog();
		String titleDialog = "Dialog";
		dialog.setTitle(titleDialog);
		dialog.initModality(Modality.NONE);
		dialog.setResizable(true);
		dialog.setX(1050);
		dialog.setY(300);
		dialog.getDialogPane().setPrefSize(150, 200);
		dialog.getDialogPane().getButtonTypes().add(button);
		dialog.getDialogPane().lookupButton(button);

		Stage stage = (Stage)dialog.getDialogPane().getScene().getWindow();
		stage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> clickHandlerCustom(event, titleDialog));
		stage.addEventHandler(MouseEvent.MOUSE_MOVED, event -> moveHandlerCustom(event, titleDialog));
		stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> pressHandlerCustom(event, titleDialog));
		stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> releaseHandlerCustom(event, titleDialog));
		stage.addEventHandler(KeyEvent.KEY_TYPED, event -> typeHandlerCustom(event, titleDialog));

		dialog.show();
	}
}
