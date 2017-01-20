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
	public TableView tableView;
	public TextField tfDialogName;
	public ComboBox<ControlKind> cbControl;
	public TextField tfOwnerId;

	private DialogWizard model;
	private Alert dialog;

	private String windowName;

	private ImageViewWithScale imageViewWithScale;
	private TreeViewWithRectangles treeViewWithRectangles;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.cbControl.getItems().addAll(ControlKind.values());

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
		this.tfDialogName.textProperty().addListener((observable, oldValue, newValue) -> this.model.changeDialogName(newValue));
	}

	void show()
	{
		this.dialog.setOnShowing(e -> this.model.displayImageAndTree());
		this.dialog.show();
	}

	void displaySelf(IControl self)
	{
		if (self != null)
		{
			this.tfOwnerId.setText(self.getID());
			this.cbControl.getSelectionModel().select(self.getBindedClass());
		}
	}

	void displayApplicationNotRun()
	{
		Text textApplicationNotRunning = new Text("Application not starting.\nStart application before call this wizard");
		this.imageViewWithScale.replaceWaitingPane(textApplicationNotRunning);
		this.treeViewWithRectangles.replaceWaitingPane(new Label());
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

	public void editOwner(ActionEvent actionEvent)
	{

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
}