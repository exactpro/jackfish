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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.combobox.CheckedComboBox;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.layout.CustomArrow;
import com.exactprosystems.jf.tool.custom.layout.CustomGrid;
import com.exactprosystems.jf.tool.custom.layout.CustomRectangle;
import com.exactprosystems.jf.tool.custom.scale.IScaleListener;
import com.exactprosystems.jf.tool.custom.scale.ScalePane;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LayoutWizardController implements Initializable, ContainingParent, IScaleListener
{
	private static final Function<IControl, String> converter = IControl::getID;
	public static final int BORDER_WIDTH = 4;
	public static final int OFFSET = BORDER_WIDTH / 2;

	public Parent parent;
	public ComboBox<IWindow> cbDialog;
	public CheckedComboBox<IControl> cbElement;
	public BorderPane paneImage;
	public BorderPane paneFormula;
	public VBox vBox;
	public HBox hBox;
	public CheckBox cbVisibility;
	public CheckBox cbCount;
	public CheckBox cbSize;
	public CheckBox cbNear;
	public CheckBox cbCross;
	public CheckBox cbCenter;

	private CheckBox cbGrid;

	private ToggleGroup tgVertical;
	private ToggleGroup tgHorizontal;

	private ExpressionField formulaField;

	private LayoutWizard model;
	private Alert dialog;

	private Group group;
	private ScrollPane mainScrollPane;
	private ImageView imageView;

	private CustomRectangle vRectangle;
	private CustomRectangle hRectangle;
	private CustomGrid customGrid;
	private CustomArrow customArrow;

	private ChangeListener<IWindow> windowChangeListener = (observable, oldValue, newValue) -> Common.tryCatch(() -> this.model.changeDialog(newValue), "Error on change dialog");

	public void init(LayoutWizard wizard, AbstractEvaluator evaluator)
	{
		this.model = wizard;

		initDialog();
		createCanvas();
		createFormula(evaluator);

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
		this.paneImage.setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		Text text = new Text(String.format("Loading image for dialog '%s' ...", dialogName));
		this.paneImage.setBottom(text);
		BorderPane.setAlignment(text, Pos.CENTER);
	}

	public void loadImageFailed()
	{
		clearImage();
		Text value = new Text("Interactive mode is not available, because dialog not found");
		value.setStroke(Color.RED);
		BorderPane.setAlignment(value, Pos.CENTER);
		this.paneImage.setCenter(value);
	}

	public void displayScreenShot(BufferedImage bufferedImage) throws IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", outputStream);
		Image image = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
		this.imageView.setImage(image);
		this.paneImage.setBottom(null);
		this.paneImage.setCenter(this.mainScrollPane);
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

	public void displayWindow(IWindow window, List<IControl> checkedControls)
	{
		this.cbDialog.getSelectionModel().select(window);
		this.cbElement.setChecked(checkedControls);
	}

	public void displayDialogs(Collection<IWindow> dialogs)
	{
		this.cbDialog.getItems().addAll(dialogs);
	}

	public void displayControls(Collection<IControl> controls)
	{
		this.cbElement.getItems().setAll(controls);
	}

	public void displaySelectedControls(List<IControl> controls)
	{
		List<IControl> lv = new ArrayList<>(controls);
		List<IControl> lh = new ArrayList<>(controls);
		this.vBox.getChildren().setAll(lv.stream().map(ic ->
		{
			ToggleButton button = new ToggleButton(converter.apply(ic));
			button.setUserData(ic);
//			button.setToggleGroup(tgVertical);
			button.setMaxWidth(Double.MAX_VALUE);
			return button;
		}).collect(Collectors.toList()));
		this.hBox.getChildren().setAll(lh.stream().map(ic ->
		{
			ToggleButton button = new ToggleButton(converter.apply(ic));
			button.setUserData(ic);
//			button.setToggleGroup(tgHorizontal);
			return button;
		}).collect(Collectors.toList()));
	}

	public void displayRect(Rectangle rectangle, boolean isHorizontal)
	{
		CustomRectangle r = isHorizontal ? hRectangle : vRectangle;
		r.updateRectangle(rectangle.getX() + OFFSET, rectangle.getY() + OFFSET, rectangle.getWidth() - BORDER_WIDTH, rectangle.getHeight() - BORDER_WIDTH);
		r.setInit(true);
		r.setVisible(true);
	}

	public void hideRect(boolean isHorizontal)
	{
		CustomRectangle r = isHorizontal ? hRectangle : vRectangle;
		r.setVisible(false);
	}

	public void changeScaleRect(Rectangle rectangle, boolean isHorizontal)
	{
		CustomRectangle r = isHorizontal ? hRectangle : vRectangle;
		r.updateRectangle(rectangle.getX() + OFFSET, rectangle.getY() + OFFSET, rectangle.getWidth() - BORDER_WIDTH, rectangle.getHeight() - BORDER_WIDTH);
	}

	public void show()
	{
		this.dialog.showAndWait();
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

		scalePane.getChildren().add(new Separator(Orientation.VERTICAL));
		this.cbGrid = new CheckBox("Show grid");
		this.cbGrid.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue)
			{
				this.customGrid.show();
			}
			else
			{
				this.customGrid.hide();
			}
		});
		scalePane.getChildren().add(this.cbGrid);

		this.paneImage.setTop(scalePane);

		this.cbElement.setOnHidden(e -> this.model.selectItems(this.cbElement.getChecked()));
		this.cbElement.setStringConverter(converter);

		this.tgHorizontal = new ToggleGroup();
		this.tgVertical = new ToggleGroup();

		this.tgHorizontal.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				this.model.displayHRect(((IControl) newValue.getUserData()));
			}
			else
			{
				this.model.hideHRect();
			}
			if (newValue != null && this.tgVertical.getSelectedToggle() != null)
			{
				this.model.changeItem(((IControl) newValue.getUserData()), ((IControl) this.tgVertical.getSelectedToggle().getUserData()));
			}
		});

		this.tgVertical.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				this.model.displayVRect(((IControl) newValue.getUserData()));
			}
			else
			{
				this.model.hideVRect();
			}
			if (newValue != null && this.tgHorizontal.getSelectedToggle() != null)
			{
				this.model.changeItem(((IControl) this.tgHorizontal.getSelectedToggle().getUserData()), ((IControl) newValue.getUserData()));
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
		this.dialog.getDialogPane().getStylesheets().add(Common.currentTheme().getPath());
		this.dialog.getDialogPane().setPrefHeight(1000);
		this.dialog.getDialogPane().setPrefWidth(1000);
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

		this.vRectangle = new CustomRectangle();
		this.vRectangle.addStyleClass(CssVariables.SELF_CONTROL);
		this.hRectangle = new CustomRectangle();
		this.hRectangle.addStyleClass(CssVariables.OTHER_CONTROL);
		this.customArrow = new CustomArrow();
		this.vRectangle.setWidthLine(BORDER_WIDTH);
		this.hRectangle.setWidthLine(BORDER_WIDTH);

		this.group.getChildren().add(this.imageView);
		this.vRectangle.setVisible(false);
		this.vRectangle.setGroup(this.group);
		this.customArrow.setGroup(this.group);
		this.hRectangle.setVisible(false);
		this.hRectangle.setGroup(this.group);

		this.customGrid = new CustomGrid();
		this.customGrid.hide();
		this.customGrid.setGroup(this.group);
	}

	private void createFormula(AbstractEvaluator evaluator)
	{
		this.formulaField = new ExpressionField(evaluator);
		this.formulaField.setHelperForExpressionField("Layout formula", null);
		this.paneFormula.setCenter(this.formulaField);
	}
	//endregion
}