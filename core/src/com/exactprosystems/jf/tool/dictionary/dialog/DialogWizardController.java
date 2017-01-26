package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.TreeViewWithRectangles;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
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

	public void cancel(ActionEvent actionEvent)
	{

	}

	public void accept(ActionEvent actionEvent)
	{

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
		TableColumn<ElementWizardBean, Integer> columnNumber = new TableColumn<>("#");
		columnNumber.setPrefWidth(35);
		columnNumber.setMaxWidth(35);
		columnNumber.setMinWidth(35);

		TableColumn<ElementWizardBean, String> columnId = new TableColumn<>("Id");

		TableColumn<ElementWizardBean, ControlKind> columnKind = new TableColumn<>("Kind");
		columnKind.setCellFactory(e -> new TableCell<ElementWizardBean, ControlKind>(){
			@Override
			protected void updateItem(ControlKind item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(item.name());
				}
				else
				{
					setText(null);
				}
			}
		});
		columnKind.setPrefWidth(135);
		columnKind.setMaxWidth(135);
		columnKind.setMinWidth(135);

		int value = 75;

		TableColumn<ElementWizardBean, Boolean> columnIsXpath = new TableColumn<>("Xpath");
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
		columnOption.setPrefWidth(value);
		columnOption.setMaxWidth(value);
		columnOption.setMinWidth(value);

		columnId.prefWidthProperty().bind(this.tableView.widthProperty().subtract(35 + 135 + value * 4));

		this.tableView.getColumns().addAll(columnNumber, columnId, columnKind, columnIsXpath, columnIsNew, columnCount, columnOption);
	}
}