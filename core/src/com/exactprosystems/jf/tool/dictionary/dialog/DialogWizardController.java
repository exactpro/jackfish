package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.TreeViewWithRectangles;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

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

	void displayElement(ElementWizardBean bean)
	{
		int index = this.tableView.getItems().indexOf(bean);
		if (index == -1)
		{
			this.tableView.getItems().add(bean);
		}
		else
		{
			this.tableView.getItems().set(index, bean);
		}
	}

	AbstractControl editElement(AbstractControl abstractControl)
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		alert.getDialogPane().setHeader(new Label());
		alert.setTitle("Change element");
		GridPane gridPane = new GridPane();
		gridPane.setPrefWidth(400);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		alert.getDialogPane().setContent(gridPane);
		gridPane.getStyleClass().addAll(CssVariables.HGAP_MIN, CssVariables.VGAP_MIN);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(30);
		c0.setHalignment(HPos.RIGHT);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setFillWidth(true);
		c1.setPercentWidth(70);
		c1.setHalignment(HPos.LEFT);
		gridPane.getColumnConstraints().addAll(c0, c1);

		int index = 1;
		addXpathToPane(gridPane, abstractControl.getXpath(), abstractControl.useAbsoluteXpath(),
				  newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.xpathName, newId), "Error on set parameter")
				, newB -> Common.tryCatch(() -> abstractControl.set(AbstractControl.absoluteXpathName, newB), "Error on set parameter")
		);
//		addToPane(gridPane, "Xpath : ",		abstractControl.getXpath(),		newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.xpathName, newId), "Error on set parameter"), index++);
		addToPane(gridPane, "UID : ",		abstractControl.getUID(), 		newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.uidName, newId), "Error on set parameter"), index++);
		addToPane(gridPane, "Class : ",		abstractControl.getClazz(),		newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.clazzName, newId), "Error on set parameter"), index++);
		addToPane(gridPane, "Name : ",	 	abstractControl.getName(), 		newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.nameName, newId), "Error on set parameter"), index++);
		addToPane(gridPane, "Title : ",	 	abstractControl.getTitle(),		newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.titleName, newId), "Error on set parameter"), index++);
		addToPane(gridPane, "Action : ", 	abstractControl.getAction(),	newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.actionName, newId), "Error on set parameter"), index++);
		addToPane(gridPane, "Text : ",	 	abstractControl.getText(),		newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.textName, newId), "Error on set parameter"), index++);
		addToPane(gridPane, "Tooltip : ", 	abstractControl.getTooltip(), 	newId -> Common.tryCatch(() -> abstractControl.set(AbstractControl.tooltipName, newId), "Error on set parameter"), index++);

		Optional<ButtonType> buttonType = alert.showAndWait();
		if (buttonType.isPresent())
		{
			ButtonType type = buttonType.get();
			if (type == ButtonType.OK)
			{
				return abstractControl;
			}
		}
		return null;
	}

	private void addToPane(GridPane pane, String id, String value, Consumer<String> consumer, int index)
	{
		Label lbl = new Label(id);
		CustomFieldWithButton tf = new CustomFieldWithButton(value);
		tf.setMaxWidth(Double.MAX_VALUE);
		tf.textProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
		GridPane.setFillWidth(tf, true);
		pane.add(lbl, 0, index);
		pane.add(tf, 1, index);
	}

	private void addXpathToPane(GridPane pane, String value, boolean isAbsolute, Consumer<String> consumer, Consumer<Boolean> absoluteConsumer)
	{
		Label lbl = new Label("Xpath");
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER);

		CheckBox cbIsAbsolute = new CheckBox("");
		cbIsAbsolute.setSelected(isAbsolute);
		cbIsAbsolute.selectedProperty().addListener((observable, oldValue, newValue) -> absoluteConsumer.accept(newValue));
		cbIsAbsolute.setAlignment(Pos.CENTER);

		CustomFieldWithButton tf = new CustomFieldWithButton(value);
		tf.textProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
		HBox.setHgrow(tf, Priority.ALWAYS);
		box.getChildren().addAll(cbIsAbsolute, tf);

		pane.add(lbl, 0, 0);
		pane.add(box, 1, 0);
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
		columnId.setEditable(true);
		columnId.setCellFactory(e -> new TableCell<ElementWizardBean, String>(){
			private TextField tf;

			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
				else
				{
					setGraphic(null);
					setText(null);
				}
			}

			@Override
			public void startEdit()
			{
				super.startEdit();
				createTextField();
				this.tf.setText(getString());
				setGraphic(this.tf);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				Platform.runLater(this.tf::requestFocus);
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				setText(getString());
				setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			private void createTextField()
			{
				if (this.tf == null)
				{
					this.tf = new TextField(getString());
					this.tf.focusedProperty().addListener((observable, oldValue, newValue) -> {
						if (!newValue && tf != null)
						{
							commitEdit(tf.getText());
						}
					});
					this.tf.setOnKeyPressed(t -> {
						if (t.getCode() == KeyCode.ENTER)
						{
							commitEdit(tf.getText());
						}
						else if (t.getCode() == KeyCode.ESCAPE)
						{
							cancelEdit();
						}
						else if (t.getCode() == KeyCode.TAB)
						{
							commitEdit(tf.getText());
						}
					});
				}
			}

			private String getString()
			{
				return String.valueOf(getItem() == null ? "" : getItem());
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
			ChoiceBox<ControlKind> comboBox;

			@Override
			protected void updateItem(ControlKind item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
				else
				{
					setGraphic(null);
					setText(null);
				}
			}

			@Override
			public void startEdit()
			{
				super.startEdit();
				createCB();
				setGraphic(this.comboBox);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				this.comboBox.show();
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				setText(getString());
				setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			private void createCB()
			{
				if (this.comboBox == null)
				{
					this.comboBox = new ChoiceBox<>(FXCollections.observableArrayList(ControlKind.values()));
					//TODO think about it
					this.comboBox.setStyle("-fx-background-color : gold");
					this.comboBox.getSelectionModel().select(getItem());
					this.comboBox.setOnAction(e -> commitEdit(this.comboBox.getSelectionModel().getSelectedItem()));
					this.comboBox.showingProperty().addListener((observable, oldValue, newValue) -> {
						if (!newValue)
						{
							cancelEdit();
						}
					});
				}
			}

			private String getString()
			{
				return String.valueOf(getItem() == null ? "" : getItem().name());
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
					btnEdit.setOnAction(e -> Common.tryCatch(() -> model.changeElement(item), "Error on change element"));
					btnRemove.setOnAction(e -> model.removeElement(item));
					box.getChildren().addAll(btnEdit, Common.createSpacer(Common.SpacerEnum.HorizontalMid),btnRemove);
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