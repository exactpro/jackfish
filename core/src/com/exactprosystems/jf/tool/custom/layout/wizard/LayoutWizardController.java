////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout.wizard;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.combobox.CheckedComboBox;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetView;
import com.exactprosystems.jf.tool.custom.layout.CustomArrow;
import com.exactprosystems.jf.tool.custom.layout.CustomGrid;
import com.exactprosystems.jf.tool.custom.layout.CustomRectangle;
import com.exactprosystems.jf.tool.custom.scale.IScaleListener;
import com.exactprosystems.jf.tool.custom.scale.ScalePane;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class LayoutWizardController implements Initializable, ContainingParent, IScaleListener
{
	public static final int BORDER_WIDTH = 4;
	public static final int OFFSET = BORDER_WIDTH / 2;

	public Parent parent;
	public BorderPane paneTable;
	public ComboBox<IWindow> cbDialog;
	public CheckedComboBox<IControl> cbElement;
	public BorderPane paneImage;

	private LayoutWizard model;
	private Alert dialog;
	private DataProvider<String> provider;

	private Group group;
	private ScrollPane mainScrollPane;
	private ImageView imageView;

	private CustomGrid customGrid;
	private CustomRectangle selfRectangle;
	private CustomRectangle otherRectangle;
	private CustomArrow customArrow;

	private ProgressIndicator progressIndicator;
	private Text progressText;

	private ChangeListener<IWindow> windowChangeListener = (observable, oldValue, newValue) -> Common.tryCatch(() -> this.model.changeDialog(newValue), "Error on change dialog");
	private SpreadsheetView view;

	public void init(LayoutWizard wizard, DataProvider<String> provider)
	{
		this.model = wizard;

		this.provider = provider;
		this.view = new SpreadsheetView(this.provider);
		this.paneTable.setCenter(this.view);

		initDialog();
		createCanvas();

		this.paneImage.setCenter(this.mainScrollPane);
	}

	//region work with image
	public void clearImage()
	{
		this.paneImage.setCenter(null);
		this.paneImage.setBottom(null);
	}

	public void beforeLoadImage(String dialogName)
	{
		this.paneImage.setCenter(this.progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		this.paneImage.setBottom(this.progressText = new Text(String.format("Loading image for dialog '%s' ...", dialogName)));
		BorderPane.setAlignment(this.progressText, Pos.CENTER);
	}

	public void displayScreenShot(BufferedImage bufferedImage) throws IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", outputStream);
		Image image = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
		this.imageView.setImage(image);
		this.paneImage.getChildren().removeAll(this.progressIndicator, this.progressText);
		this.paneImage.setCenter(this.mainScrollPane);
		BorderPane.setMargin(this.mainScrollPane, new Insets(0, 0, 5, 0));
		this.customGrid.setSize((int) image.getWidth(), (int) image.getHeight());
	}

	public void resizeImage(double width, double height)
	{
		this.imageView.setFitHeight(height);
		this.imageView.setFitWidth(width);
		this.customGrid.setSize((int) width, (int) height);
	}

	//region IScaleListener
	@Override
	public void changeScale(double scale)
	{
		this.model.changeScale(scale);
	}
	//endregion

	//endregion

	public void displayWindow(IWindow window)
	{
		this.view.renameColumn(0, window.getName());
	}

	public void displayDialogs(Collection<IWindow> dialogs)
	{
		this.cbDialog.getItems().addAll(dialogs);
	}

	public void displayControls(Collection<IControl> controls)
	{
		this.cbElement.getItems().setAll(controls);
	}

	public void show()
	{
		this.dialog.show();
	}

	public void hide()
	{
		this.dialog.hide();
	}

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.cbDialog.setCellFactory(p -> new ListCell<IWindow>()
		{
			@Override
			protected void updateItem(IWindow item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(item.getName());
				}
				else
				{
					setText(null);
				}
			}
		});
		this.cbDialog.getSelectionModel().selectedItemProperty().addListener(this.windowChangeListener);

		ScalePane scalePane = new ScalePane(this);
		scalePane.setMinHeight(30.0);
		scalePane.setPrefHeight(30.0);
		scalePane.setMaxHeight(30.0);
		this.paneImage.setTop(scalePane);

		this.cbElement.setOnHidden(e -> this.model.selectItems(this.cbElement.getChecked()));
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	//region Action events
	public void zoomMinus(ActionEvent actionEvent)
	{

	}

	public void zoomPlus(ActionEvent actionEvent)
	{

	}

	public void btnClose(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::close, "Error on close");
	}

	public void btnAccept(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::accept, "Error on close");
	}
	//endregion

	//region private methods
	private void initDialog()
	{
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		this.dialog.setHeight(1000);
		this.dialog.setWidth(1000);
		this.dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		this.dialog.setTitle("Layout wizard");
		this.dialog.getDialogPane().setHeader(new Label());
		this.dialog.getDialogPane().setContent(parent);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		ButtonType buttonCreate = new ButtonType("", ButtonBar.ButtonData.OTHER);
		this.dialog.getButtonTypes().setAll(buttonCreate);
		Button button = (Button) this.dialog.getDialogPane().lookupButton(buttonCreate);
		button.setPrefHeight(0.0);
		button.setMaxHeight(0.0);
		button.setMinHeight(0.0);
		button.setVisible(false);
	}

	private void createCanvas()
	{
		this.group = new Group();
		this.mainScrollPane = new ScrollPane(this.group);
		this.mainScrollPane.setContent(this.group);
		this.mainScrollPane.setFitToHeight(true);
		this.mainScrollPane.setFitToWidth(true);
		this.mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.imageView = new ImageView();
		ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		AnchorPane.setTopAnchor(progressIndicator, (double) 200);
		AnchorPane.setLeftAnchor(progressIndicator, (double) 200);

		this.selfRectangle = new CustomRectangle();
		this.otherRectangle = new CustomRectangle();
		this.customArrow = new CustomArrow();
		this.selfRectangle.setWidthLine(BORDER_WIDTH);
		this.otherRectangle.setWidthLine(BORDER_WIDTH);

		this.group.getChildren().add(this.imageView);
		this.selfRectangle.setVisible(false);
		this.selfRectangle.setGroup(this.group);
		this.customArrow.setGroup(this.group);
		this.otherRectangle.setVisible(false);
		this.otherRectangle.setGroup(this.group);

		this.customGrid = new CustomGrid();
		this.customGrid.hide();
		this.customGrid.setGroup(this.group);
	}
	//endregion
}