package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogWizardController implements Initializable, ContainingParent
{
	public Parent parent;
	public BorderPane borderPane;
	public SplitPane horSplitPane;
	public SplitPane verSplitPane;
	public ScrollPane scrollPaneImage;
	public AnchorPane anchorPaneImage;
	public Group group;
	public TableView tableView;
	public AnchorPane anchorPaneTree;
	public TreeView treeView;

	private DialogWizard model;
	private IWindow window;
	private Alert dialog;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	void init(DialogWizard model, IWindow window)
	{
		this.model = model;
		this.window = window;
		initDialog();
	}

	void show()
	{
		this.dialog.show();
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
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		this.dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		this.dialog.setTitle("Dialog wizard " + (this.window == null ? "" : " for " + this.window.getName()));
		this.dialog.setWidth(1000.0);
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