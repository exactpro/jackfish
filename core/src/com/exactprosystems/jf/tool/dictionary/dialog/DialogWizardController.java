package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.TreeViewWithRectangles;
import javafx.collections.FXCollections;
import javafx.event.*;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DialogWizardController implements Initializable, ContainingParent
{
	public Parent parent;
	public BorderPane borderPane;
	public SplitPane horSplitPane;
	public SplitPane verSplitPane;
	public TableView<ElementWizardBean> tableView;
	public TextField tfDialogName;
	public Label lblSelfId;

	private DialogWizard model;
	private Alert dialog;

	private String windowName;

	private ImageViewWithScale imageViewWithScale;
	private TreeViewWithRectangles treeViewWithRectangles;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.imageViewWithScale = new ImageViewWithScale();
		this.verSplitPane.getItems().add(0, this.imageViewWithScale.getContent());

		this.treeViewWithRectangles = new TreeViewWithRectangles();
		this.horSplitPane.getItems().add(this.treeViewWithRectangles.getContent());

		this.imageViewWithScale.setClickConsumer(this.treeViewWithRectangles::selectItem);
		this.treeViewWithRectangles.setTreeViewConsumer(xpathItem -> {
			if (xpathItem != null)
			{
				this.imageViewWithScale.displayRectangle(xpathItem.getRectangle());
			}
		});
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	void init(DialogWizard model, String windowName)
	{
		this.windowName = windowName;
		this.model = model;
		this.tfDialogName.setText(windowName);
		initDialog();
		initTable();
		this.tfDialogName.textProperty().addListener((observable, oldValue, newValue) -> this.model.changeDialogName(newValue));
	}

	void show()
	{
		this.dialog.setOnShowing(e -> this.model.displayImageAndTree());
		this.dialog.show();
	}

	void close()
	{
		this.dialog.close();
	}

	void displaySelf(IControl self)
	{
		this.lblSelfId.setText(self.getID());
	}

	void displayTree(Document document, int xOffset, int yOffset)
	{
		if (document != null)
		{
			this.treeViewWithRectangles.displayDocument(document, xOffset, yOffset);
			BufferedImage image = this.imageViewWithScale.getImage();
			this.imageViewWithScale.setListRectangles(this.treeViewWithRectangles.buildMap(image.getWidth(), image.getHeight(), new Dimension(image.getWidth() / 16, image.getHeight() / 16)));
		}
	}

	void displayImage(BufferedImage image)
	{
		if (image != null)
		{
			this.imageViewWithScale.displayImage(image);
		}
	}

	void displayImageFailing(String message)
	{
		Text node = new Text();
		node.setText("Exception :\n" + message);
		node.setFill(Color.RED);
		this.imageViewWithScale.replaceWaitingPane(node);
	}

	void displayDocumentFailing(String message)
	{
		Text node = new Text();
		node.setText("Exception :\n" + message);
		node.setFill(Color.RED);
		this.treeViewWithRectangles.replaceWaitingPane(node);
	}

	void displayElements(List<ElementWizardBean> list)
	{
		this.tableView.getItems().setAll(list);
	}

	public void cancel(ActionEvent actionEvent)
	{
		this.model.close(false);
	}

	public void accept(ActionEvent actionEvent)
	{
		this.model.close(true);
	}

	private void initDialog()
	{
		this.dialog = new Alert(Alert.AlertType.CONFIRMATION);
		this.dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		this.dialog.setTitle("Dialog wizard " + this.windowName);
		this.dialog.setWidth(1500.0);
		this.dialog.setHeight(1000.0);
		Label header = new Label();
		header.setMinHeight(0.0);
		header.setPrefHeight(0.0);
		header.setMaxHeight(0.0);
		this.dialog.getDialogPane().setHeader(header);
		this.dialog.getDialogPane().setContent(this.parent);
		ButtonType buttonCreate = new ButtonType("", ButtonBar.ButtonData.OTHER);
		this.dialog.getButtonTypes().setAll(buttonCreate);
		Button button = (Button) this.dialog.getDialogPane().lookupButton(buttonCreate);
		button.setPrefHeight(0.0);
		button.setMaxHeight(0.0);
		button.setMinHeight(0.0);
		button.setVisible(false);
	}

	private void initTable()
	{
		this.tableView.setEditable(true);
		TableColumn<ElementWizardBean, Integer> columnNumber = new TableColumn<>("#");
		columnNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
		columnNumber.setCellFactory(e -> new TableCell<ElementWizardBean, Integer>(){
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(String.valueOf(item));
				}
				else
				{
					setText(null);
				}
			}
		});
		columnNumber.setPrefWidth(35);
		columnNumber.setMaxWidth(35);
		columnNumber.setMinWidth(35);

		TableColumn<ElementWizardBean, String> columnId = new TableColumn<>("Id");
		columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		columnId.setCellFactory(e -> new TableCell<ElementWizardBean, String>(){
			private TextField tf;

			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					if (this.tf == null)
					{
						this.tf = new TextField(item);
						this.tf.focusedProperty().addListener((observable, oldValue, newValue) -> {
							if (newValue)
							{
								getTableView().edit(getIndex(), columnId);
								startEdit();
							}
							else
							{
								commitEdit(this.tf.getText());
							}
						});
					}
					this.setGraphic(this.tf);
				}
				else
				{
					setGraphic(null);
				}
			}

			@Override
			public void startEdit()
			{
				super.startEdit();
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			}

			@Override
			public void commitEdit(String newValue)
			{
				final TableView<ElementWizardBean> table = getTableView();
				if (table != null) {
					TableColumn.CellEditEvent editEvent = new TableColumn.CellEditEvent(
							table,
							table.getEditingCell(),
							TableColumn.editCommitEvent(),
							newValue
					);

					javafx.event.Event.fireEvent(getTableColumn(), editEvent);
					setEditable(false);
					updateItem(newValue, false);
				}
			}
		});
		columnId.setOnEditCommit(e -> {
			ElementWizardBean elementWizardBean = e.getRowValue();
			if (elementWizardBean != null)
			{
				Common.tryCatch(() -> this.model.updateId(elementWizardBean.getNumber(), e.getNewValue()), "Error on update id");
			}
		});

		TableColumn<ElementWizardBean, ControlKind> columnKind = new TableColumn<>("Kind");
		columnKind.setCellValueFactory(new PropertyValueFactory<>("controlKind"));
		columnKind.setOnEditCommit(e -> {
			ElementWizardBean rowValue = e.getRowValue();
			if (rowValue != null)
			{
				Common.tryCatch(() -> this.model.updateControlKind(rowValue.getNumber(),e.getNewValue()), "Error on update control kind");
			}
		});
		columnKind.setCellFactory(e -> new TableCell<ElementWizardBean, ControlKind>(){
			ComboBox<ControlKind> comboBox = new ComboBox<>(FXCollections.observableArrayList(ControlKind.values()));

			@Override
			protected void updateItem(ControlKind item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					this.comboBox.getSelectionModel().select(item);
					this.comboBox.setOnShowing(e -> {
						getTableView().edit(getIndex(), columnKind);
						startEdit();
					});
					this.comboBox.setOnHidden(e -> cancelEdit());
					this.comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> commitEdit(newValue));
					setGraphic(this.comboBox);
				}
				else
				{
					setGraphic(null);
				}
			}

			@Override
			public void commitEdit(ControlKind newValue)
			{
				final TableView<ElementWizardBean> table = getTableView();
				if (table != null) {
					TableColumn.CellEditEvent editEvent = new TableColumn.CellEditEvent(
							table,
							table.getEditingCell(),
							TableColumn.editCommitEvent(),
							newValue
					);

					javafx.event.Event.fireEvent(getTableColumn(), editEvent);
					setEditable(false);
					updateItem(newValue, false);
				}
			}
		});
		columnKind.setPrefWidth(135);
		columnKind.setMaxWidth(135);
		columnKind.setMinWidth(135);

		int value = 75;

		TableColumn<ElementWizardBean, Boolean> columnIsXpath = new TableColumn<>("Xpath");
		columnIsXpath.setCellValueFactory(new PropertyValueFactory<>("xpath"));
		columnIsXpath.setCellFactory(e -> new TableCell<ElementWizardBean, Boolean>(){
			@Override
			protected void updateItem(Boolean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(item.toString());
				}
				else
				{
					setText(null);
				}
			}
		});
		columnIsXpath.setPrefWidth(value);
		columnIsXpath.setMaxWidth(value);
		columnIsXpath.setMinWidth(value);

		TableColumn<ElementWizardBean, Boolean> columnIsNew = new TableColumn<>("New");
		columnIsNew.setCellValueFactory(new PropertyValueFactory<>("isNew"));
		columnIsNew.setCellFactory(e -> new TableCell<ElementWizardBean, Boolean>(){
			@Override
			protected void updateItem(Boolean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(item.toString());
				}
				else
				{
					setText(null);
				}
			}
		});
		columnIsNew.setPrefWidth(value);
		columnIsNew.setMaxWidth(value);
		columnIsNew.setMinWidth(value);

		TableColumn<ElementWizardBean, Integer> columnCount = new TableColumn<>("Count");
		columnCount.setCellValueFactory(new PropertyValueFactory<>("count"));
		columnCount.setCellFactory(e -> new TableCell<ElementWizardBean, Integer>(){
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(item.toString());
				}
				else
				{
					setText(null);
				}
			}
		});

		columnCount.setPrefWidth(value);
		columnCount.setMaxWidth(value);
		columnCount.setMinWidth(value);

		TableColumn<ElementWizardBean, ElementWizardBean> columnOption = new TableColumn<>("Option");
		columnOption.setCellValueFactory(new PropertyValueFactory<>("option"));
		columnOption.setPrefWidth(value);
		columnOption.setMaxWidth(value);
		columnOption.setMinWidth(value);
		columnOption.setCellFactory(e -> new TableCell<ElementWizardBean, ElementWizardBean>(){
			@Override
			protected void updateItem(ElementWizardBean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					HBox box = new HBox();
					box.setAlignment(Pos.CENTER);
					Button btnEdit = new Button("E");
					Button btnRemove = new Button("R");
					btnEdit.setOnAction(e -> {
						System.out.println("Edit control : " + item);
					});
					btnRemove.setOnAction(e -> {
						System.out.println("Remove control : " + item);
					});
					box.getChildren().addAll(btnEdit, btnRemove);
					setGraphic(box);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		columnId.prefWidthProperty().bind(this.tableView.widthProperty().subtract(35 + 135 + value * 4 + 2));

		this.tableView.getColumns().addAll(columnNumber, columnId, columnKind, columnIsXpath, columnIsNew, columnCount, columnOption);
	}
}