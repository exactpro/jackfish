package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.Range;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.fields.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.fields.NewExpressionField;
import com.exactprosystems.jf.tool.custom.layout.LayoutExpressionBuilder.SpecMethod;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

//TODO replace all border width and fillStroke to styleClass
public class LayoutExpressionBuilderController implements Initializable, ContainingParent
{
	public static final int			BORDER_WIDTH	= 4;
	public static final int			OFFSET			= BORDER_WIDTH / 2;

	private static final Color		selfColor		= new Color(1, 0, 0, 1);
	private static final Color		otherColor		= new Color(0, 1, 0, 1);
	
	public BorderPane				mainPane;

	public VBox						vBoxControls;
	public CustomFieldWithButton	cfFindControl;
	public HBox						hBoxCheckBoxes;
	public BorderPane				parentPane;
	public ScrollPane				spControls;
	public ChoiceBox<SpecMethod>		cbParameters;
	public Label					labelControlId;
	public ChoiceBox<Range>			cbRange;
	public Button					btnAddFormula;
	public GridPane					gridPane;
	private NewExpressionField		expressionFieldFirst;
	private NewExpressionField		expressionFieldSecond;
	private ToggleGroup				mainToggleGroup;

	private ImageView				imageView;
	private Parent					parent;
	private LayoutExpressionBuilder	model;

	private CustomRectangle			initialRectangle;
	private CustomRectangle			selectedRectangle;

	// ==============================================================================================================================
	// interface Initializable
	// ==============================================================================================================================
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		assert vBoxControls != null : "fx:id=\"vBoxControls\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert cfFindControl != null : "fx:id=\"cfFindControl\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert hBoxCheckBoxes != null : "fx:id=\"hBoxCheckBoxes\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert parentPane != null : "fx:id=\"parentPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";

		this.cbRange.getItems().addAll(Range.values());
		this.cbRange.getSelectionModel().selectFirst();
		this.cbRange.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			GridPane.setColumnSpan(this.expressionFieldFirst, 2);
			this.expressionFieldSecond.setVisible(false);
			if (newValue == Range.BETWEEN)
			{
				this.expressionFieldSecond.setVisible(true);
				GridPane.setColumnSpan(this.expressionFieldFirst, 1);
			}
		});
		this.mainToggleGroup = new ToggleGroup();
		this.mainToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				this.model.displayControl((IControl) newValue.getUserData(), false);
			}
			else
			{
				Common.tryCatch(this.model::clearCanvas, "Error on clear canvas");
			}
		});
		this.cfFindControl.textProperty().addListener((observable, oldValue, newValue) ->
		{
			// TODO implements this logic via DialogsHelper.showFindListView()
		});
		createCanvas();
	}

	// ==============================================================================================================================
	// interface ContainingParent
	// ==============================================================================================================================
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	// ==============================================================================================================================
	// public methods
	// ==============================================================================================================================
	public void init(LayoutExpressionBuilder model, AbstractEvaluator evaluator, BufferedImage bufferedImage) throws Exception
	{
		this.model = model;
		this.expressionFieldFirst = new NewExpressionField(evaluator, "first");
		this.expressionFieldFirst.setHelperForExpressionField("First", null);
		this.expressionFieldSecond= new NewExpressionField(evaluator, "second");
		this.expressionFieldSecond.setHelperForExpressionField("Second", null);
		this.gridPane.add(expressionFieldFirst, 3, 0);
		this.gridPane.add(expressionFieldSecond, 4, 0);
		this.expressionFieldSecond.setVisible(false);
		GridPane.setColumnSpan(this.expressionFieldFirst, 2);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", outputStream);
		Image image = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
		this.imageView.setImage(image);
	}

	public String show(String title, boolean fullScreen, ArrayList<IControl> list)
	{
		Alert dialog = createAlert(title);
		list.stream().map(control -> {
			ToggleButton button = new ToggleButton(control.toString());
			button.setToggleGroup(this.mainToggleGroup);
			button.prefWidthProperty().bind(this.vBoxControls.widthProperty().subtract(20));
			button.setUserData(control);
			button.setAlignment(Pos.BASELINE_LEFT);
			button.setTooltip(new Tooltip(control.locator().toString()));
			return button;
		}).forEach(this.vBoxControls.getChildren()::add);
		dialog.getDialogPane().setContent(parent);
		if (fullScreen)
		{
			dialog.setOnShown(event -> ((Stage) dialog.getDialogPane().getScene().getWindow()).setFullScreen(true));
		}
		Optional<ButtonType> optional = dialog.showAndWait();
		if (optional.isPresent())
		{
			if (optional.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				//				return this.mainExpression.getText();
			}
		}
		return null;
	}

	// ==============================================================================================================================
	// event handlers
	// ==============================================================================================================================
	public void addFormula(ActionEvent actionEvent)
	{
		Common.tryCatch(()-> this.model.addFormula(cbParameters.getSelectionModel().getSelectedItem(), labelControlId.getText(), cbRange.getSelectionModel().getSelectedItem(), "first","second"),"Error on add formula");
	}

	// ==============================================================================================================================
	// display methods
	// ==============================================================================================================================
	public void displayControl(Rectangle rectangle, boolean self)
	{
		CustomRectangle rect = self ? this.initialRectangle : this.selectedRectangle;
		Color color = self ? selfColor : otherColor;  

		rect.setColor(color);
		rect.updateRectangle(rectangle.getX() + OFFSET, rectangle.getY() + OFFSET, rectangle.getWidth() - BORDER_WIDTH, rectangle.getHeight() - BORDER_WIDTH);
		rect.setVisible(true);
	}

	public void displayMethods(SpecMethod[] methods)
	{
		this.cbParameters.getItems().clear();
		this.cbParameters.getItems().addAll(methods);
		this.cbParameters.getSelectionModel().selectFirst();
		this.cbParameters.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			this.cbRange.setVisible(newValue.needRange);
			this.labelControlId.setVisible(newValue.needStr);
		});
	}
	
	public void displayControlId(String controlId)
	{
		this.labelControlId.setText(controlId);
	}

	public void clearCanvas()
	{
		this.selectedRectangle.setVisible(false);
	}

	// ==============================================================================================================================
	// private methods
	// ==============================================================================================================================

	private Alert createAlert(String title)
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(Common.currentTheme().getPath());
		alert.setTitle(title);
		alert.setResizable(true);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.getDialogPane().setPrefHeight(600);
		alert.getDialogPane().setPrefWidth(800);
		return alert;
	}

	private void createCanvas()
	{
		Group group = new Group();
		ScrollPane scrollPane = new ScrollPane(group);
		scrollPane.setContent(group);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.mainPane.setCenter(scrollPane);
		this.imageView = new ImageView();

		this.initialRectangle = new CustomRectangle();
		this.selectedRectangle = new CustomRectangle();
		this.initialRectangle.setWidthLine(BORDER_WIDTH);
		this.selectedRectangle.setWidthLine(BORDER_WIDTH);
		group.getChildren().add(this.imageView);
		this.initialRectangle.setVisible(false);
		this.initialRectangle.setGroup(group);
		this.selectedRectangle.setVisible(false);
		this.selectedRectangle.setGroup(group);
	}

}
