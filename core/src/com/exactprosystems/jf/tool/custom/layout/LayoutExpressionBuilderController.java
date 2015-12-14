package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.Range;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.expfield.NewExpressionField;
import com.exactprosystems.jf.tool.custom.fields.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.layout.LayoutExpressionBuilder.SpecMethod;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class LayoutExpressionBuilderController implements Initializable, ContainingParent
{
	private static final int BORDER_WIDTH = 4;
	private static final int OFFSET = BORDER_WIDTH / 2;
	@FXML
	private ColumnConstraints c0;
	@FXML
	private ColumnConstraints c1;
	@FXML
	private ColumnConstraints c2;
	@FXML
	private ColumnConstraints c3;
	@FXML
	private ColumnConstraints c4;
	@FXML
	private ProgressIndicator progressIndicator;
	@FXML
	private BorderPane mainPane;
	@FXML
	private VBox vBoxControls;
	@FXML
	private CustomFieldWithButton cfFindControl;
	@FXML
	private BorderPane parentPane;
	@FXML
	private ScrollPane spControls;
	@FXML
	private ChoiceBox<SpecMethod> cbParameters;
	@FXML
	private Label labelControlId;
	@FXML
	private ChoiceBox<Range> cbRange;
	@FXML
	private Button btnAddFormula;
	@FXML
	private GridPane gridPane;

	private NewExpressionField expressionFieldFirst;
	private NewExpressionField expressionFieldSecond;
	private ToggleGroup mainToggleGroup;
	private ImageView imageView;
	private Parent parent;
	private LayoutExpressionBuilder model;
	private CustomRectangle initialRectangle;
	private CustomRectangle selectedRectangle;

	private ScrollPane mainScrollPane;
	private ArrayList<ToggleButton> buttons = new ArrayList<>();

	// ==============================================================================================================================
	// interface Initializable
	// ==============================================================================================================================
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		assert vBoxControls != null : "fx:id=\"vBoxControls\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert cfFindControl != null : "fx:id=\"cfFindControl\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert parentPane != null : "fx:id=\"parentPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";

		this.cbRange.getItems().addAll(Range.values());
		this.cbRange.getSelectionModel().selectFirst();
		this.mainToggleGroup = new ToggleGroup();
		createCanvas();
		listeners();
		visibilityListeners();
	}

	private void listeners()
	{
		this.mainToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				this.model.displayControl((IControl) newValue.getUserData(), false);
			}
			else
			{
				Common.tryCatch(this.model::clearCanvas, "Error on clear canvas");
			}
		});
		this.cfFindControl.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty())
			{
				this.vBoxControls.getChildren().setAll(this.buttons);
			}
			else
			{
				this.vBoxControls.getChildren().clear();
				this.buttons.stream().filter(t -> t.getText().toUpperCase().contains(newValue.toUpperCase())).forEach(this.vBoxControls.getChildren()::add);
			}
		});
	}

	private void visibilityListeners()
	{
		this.cbRange.visibleProperty().addListener((observable, oldValue, newValue) -> {
			expressionFieldFirst.setVisible(newValue);
			expressionFieldSecond.setVisible(false);
			c3.setPercentWidth(0);
			c4.setPercentWidth(0);
			if (newValue)
			{
				c3.setPercentWidth(40);
				c4.setPercentWidth(0);
			}
			if (newValue && cbRange.getSelectionModel().getSelectedItem() == Range.BETWEEN)
			{
				expressionFieldSecond.setVisible(true);
				c3.setPercentWidth(20);
				c4.setPercentWidth(20);
			}
		});

		this.cbRange.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (!cbRange.isVisible())
			{
				c3.setPercentWidth(0);
				c4.setPercentWidth(0);
				return;
			}
			if (newValue == Range.BETWEEN && cbRange.isVisible())
			{
				expressionFieldSecond.setVisible(true);
				c3.setPercentWidth(20);
				c4.setPercentWidth(20);
			}
			else
			{
				expressionFieldSecond.setVisible(false);
				c3.setPercentWidth(40);
				c4.setPercentWidth(0);
			}
		});

		this.cbParameters.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			this.cbRange.setVisible(newValue.needRange);
			this.labelControlId.setVisible(newValue.needStr);
			if (newValue.needRange && newValue.needStr)
			{
				c0.setPercentWidth(20);
				c1.setPercentWidth(15);
				c2.setPercentWidth(15);
			}
			else if (newValue.needRange)
			{
				c0.setPercentWidth(35);
				c1.setPercentWidth(0);
				c2.setPercentWidth(15);
			}
			else if (newValue.needStr)
			{
				c0.setPercentWidth(50);
				c1.setPercentWidth(40);
				c2.setPercentWidth(0);
			}
			else
			{
				c0.setPercentWidth(90);
				c1.setPercentWidth(0);
				c2.setPercentWidth(0);
			}
		});
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
	public void init(LayoutExpressionBuilder model, AbstractEvaluator evaluator) throws Exception
	{
		this.model = model;
		this.expressionFieldFirst = new NewExpressionField(evaluator, "first");
		this.expressionFieldFirst.setHelperForExpressionField("First", null);

		this.expressionFieldSecond = new NewExpressionField(evaluator, "second");
		this.expressionFieldSecond.setHelperForExpressionField("Second", null);

		this.gridPane.add(expressionFieldFirst, 3, 0);
		this.gridPane.add(expressionFieldSecond, 4, 0);
		this.expressionFieldSecond.setVisible(false);
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
		}).forEach(button -> {
			this.vBoxControls.getChildren().add(button);
			this.buttons.add(button);
		});

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
		Common.tryCatch(() -> this.model.addFormula(cbParameters.getSelectionModel().getSelectedItem(), labelControlId.getText(), cbRange.getSelectionModel().getSelectedItem(), this.expressionFieldFirst.getText(), this.expressionFieldSecond.getText()), "Error on add formula");
	}

	// ==============================================================================================================================
	// display methods
	// ==============================================================================================================================
	public void displayScreenShot(BufferedImage bufferedImage) throws IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", outputStream);
		Image image = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
		this.imageView.setImage(image);
		this.mainPane.getChildren().remove(this.progressIndicator);
		this.mainPane.setCenter(this.mainScrollPane);
		this.progressIndicator = null;
	}

	public void displayControl(Rectangle rectangle, boolean self)
	{
		CustomRectangle rect = self ? this.initialRectangle : this.selectedRectangle;
		String styleClass = self ? CssVariables.INITIAL_CONTROL : CssVariables.SELECTED_CONTROL;
		rect.addStyleClass(styleClass);
		rect.updateRectangle(rectangle.getX() + OFFSET, rectangle.getY() + OFFSET, rectangle.getWidth() - BORDER_WIDTH, rectangle.getHeight() - BORDER_WIDTH);
		rect.setVisible(true);
	}

	public void displayMethods(SpecMethod[] methods)
	{
		this.cbRange.setVisible(false);
		this.labelControlId.setVisible(false);
		this.cbParameters.getItems().clear();
		this.cbParameters.getItems().addAll(methods);
		this.cbParameters.getSelectionModel().selectFirst();
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
		this.mainScrollPane = new ScrollPane(group);
		mainScrollPane.setContent(group);
		mainScrollPane.setFitToHeight(true);
		mainScrollPane.setFitToWidth(true);
		mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.imageView = new ImageView();
		ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		AnchorPane.setTopAnchor(progressIndicator, (double) 200);
		AnchorPane.setLeftAnchor(progressIndicator, (double) 200);
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
