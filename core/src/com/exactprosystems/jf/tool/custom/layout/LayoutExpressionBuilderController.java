////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.PieceKind;
import com.exactprosystems.jf.api.app.Range;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.controls.toggle.CustomToggleButton;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.scale.ScalePaneNew;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class LayoutExpressionBuilderController implements Initializable, ContainingParent
{
	public static final int BORDER_WIDTH = 4;
	public static final int OFFSET = BORDER_WIDTH / 2;
	@FXML
	private CheckBox cbUseId;
	@FXML
	private HBox formulaPane;
	@FXML
	private CheckBox cbUseGrid;
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
	private ScrollPane spControls;
	@FXML
	private ChoiceBox<PieceKind> cbParameters;
	@FXML
	private Label labelControlId;
	@FXML
	private ChoiceBox<Range> cbRange;
	@FXML
	private Button btnAddFormula;
	@FXML
	private GridPane gridPane;

	private Group group;
	private ArrayList<javafx.scene.text.Text> ids = new ArrayList<>();

	private ExpressionField expressionFieldFirst;
	private ExpressionField expressionFieldSecond;
	private ToggleGroup controlsToggleGroup;
	private ToggleGroup formulaToggleGroup;
	private ImageView imageView;
	private Parent parent;
	private LayoutExpressionBuilder model;

	private CustomGrid customGrid;
	private ScalableArrow selfRectangle;
	private ScalableArrow otherRectangle;
	private CustomArrow customArrow;

	private ScrollPane mainScrollPane;
	private ArrayList<ToggleButton> buttons = new ArrayList<>();

	private Map<LayoutExpressionBuilder.IdWithCoordinates, Text> mapIdText = new HashMap<>();

	// ==============================================================================================================================
	// interface Initializable
	// ==============================================================================================================================
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		assert vBoxControls != null : "fx:id=\"vBoxControls\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert cfFindControl != null : "fx:id=\"cfFindControl\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		this.cbUseGrid.setDisable(true);
		this.cbUseId.setDisable(true);

		this.cbRange.setVisible(false);
		this.labelControlId.setVisible(false);
		this.cbParameters.getItems().addAll(PieceKind.values());
		this.cbRange.getItems().addAll(Range.values());
		this.cbParameters.getSelectionModel().selectFirst();
		this.cbRange.getSelectionModel().selectFirst();
		BorderPane.setMargin(this.gridPane, new Insets(5, 0, 0, 0));
		this.controlsToggleGroup = new ToggleGroup();
		this.formulaToggleGroup = new ToggleGroup();
		createCanvas();
		listeners();
		visibilityListeners();
	}

	// ==============================================================================================================================
	// interface ContainingParent
	// ==============================================================================================================================
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void changeScale(double scale)
	{
		this.model.changeScale(scale);
	}

	// ==============================================================================================================================
	// public methods
	// ==============================================================================================================================
	public void init(LayoutExpressionBuilder model, AbstractEvaluator evaluator) throws Exception
	{
		this.model = model;
		this.expressionFieldFirst = new ExpressionField(evaluator, "first");
		this.expressionFieldFirst.setHelperForExpressionField("First", null);

		this.expressionFieldSecond = new ExpressionField(evaluator, "second");
		this.expressionFieldSecond.setHelperForExpressionField("Second", null);

		this.gridPane.add(expressionFieldFirst, 3, 0);
		this.gridPane.add(expressionFieldSecond, 4, 0);
		this.expressionFieldSecond.setVisible(false);
		this.expressionFieldFirst.setVisible(false);
	}

	public void saveIds(Collection<LayoutExpressionBuilder.IdWithCoordinates> values)
	{
		values.stream().forEach(id -> {
			Text text = new Text(id.x, id.y, id.id);
			text.setVisible(false);
			text.getStyleClass().add(CssVariables.CONTROL_ID);
			this.group.getChildren().add(text);
			this.mapIdText.put(id, text);
		});
	}

	public ButtonData show(String title, boolean fullScreen, List<IControl> list, String parameterName)
	{
		Alert dialog = createAlert(title);
		list.stream().filter(iControl -> !Str.areEqual(parameterName, iControl.getID())).forEach(control -> {
			ToggleButton button = new ToggleButton(control.toString());
			button.setToggleGroup(this.controlsToggleGroup);
			button.prefWidthProperty().bind(this.vBoxControls.widthProperty().subtract(20));
			button.setUserData(control);
			button.setDisable(true);
			button.setAlignment(Pos.BASELINE_LEFT);
			button.setTooltip(new Tooltip(control.locator().toString()));
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
			return optional.get().getButtonData();
		}
		return null;
	}

	// ==============================================================================================================================
	// event handlers
	// ==============================================================================================================================
	public void addFormula(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> {
			if (this.formulaToggleGroup.getSelectedToggle() != null)
			{
				this.model.updateFormula(((int) this.formulaToggleGroup.getSelectedToggle().getUserData()), this.cbParameters.getSelectionModel().getSelectedItem(), this.labelControlId.getText(), this.cbRange.getSelectionModel().getSelectedItem(), this.expressionFieldFirst.getText(), this.expressionFieldSecond.getText());
				this.formulaToggleGroup.getToggles().forEach(t -> t.setSelected(false));
			}
			else
			{
				this.model.addFormula(this.cbParameters.getSelectionModel().getSelectedItem(), this.labelControlId.getText(), this.cbRange.getSelectionModel().getSelectedItem(), this.expressionFieldFirst.getText(), this.expressionFieldSecond.getText());
			}
		}, "Error on add formula");
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
		AnchorPane anchorPane = new AnchorPane();
		AnchorPane.setBottomAnchor(this.mainScrollPane, 0.0);
		AnchorPane.setLeftAnchor(this.mainScrollPane, 0.0);
		AnchorPane.setTopAnchor(this.mainScrollPane, 0.0);
		AnchorPane.setRightAnchor(this.mainScrollPane, 0.0);
		anchorPane.getChildren().add(this.mainScrollPane);
		ScalePaneNew scalePane = new ScalePaneNew();
		scalePane.setOnScaleChanged(this::changeScale);
		scalePane.setSpacing(0.0);
		anchorPane.getChildren().add(scalePane);
		this.mainPane.setCenter(anchorPane);
		BorderPane.setMargin(this.mainScrollPane, new Insets(0, 0, 5, 0));
		this.customGrid.setSize((int) image.getWidth(), (int) image.getHeight());
		this.progressIndicator = null;
		this.buttons.stream().forEach(b -> b.setDisable(false));
		this.cbUseGrid.setDisable(false);
		this.cbUseId.setDisable(false);
	}

	public void displayControl(Rectangle rectangle, boolean self)
	{
		ScalableArrow rect = self ? this.selfRectangle : this.otherRectangle;
		String styleClass = self ? CssVariables.SELF_CONTROL : CssVariables.OTHER_CONTROL;
		rect.addStyleClass(styleClass);
		rect.updateRectangle(rectangle.getX() + OFFSET, rectangle.getY() + OFFSET, rectangle.getWidth() - BORDER_WIDTH, rectangle.getHeight() - BORDER_WIDTH);
		rect.setVisible(true);
	}

	public void displayControlId(String controlId)
	{
		this.labelControlId.setText(controlId);
	}

	public void displayDistance(int distance)
	{
		this.expressionFieldFirst.setText(String.valueOf(distance));
		this.expressionFieldSecond.setText(String.valueOf(distance));
	}

	public void clearDistance()
	{
		this.expressionFieldFirst.setText("");
		this.expressionFieldSecond.setText("");
	}

	public void clearCanvas()
	{
		this.otherRectangle.setVisible(false);
	}

	public void displayArrow(int start, int end, int where, CustomArrow.ArrowDirection position)
	{
		this.customArrow.setPoints(start, end);
		this.customArrow.setDirection(position);
		this.customArrow.show(where);
	}

	public void displayFormula(int index, List<FormulaPart> formula)
	{
		if (formula.size() > 0 && this.formulaPane.getChildren().size() == 0)
		{
			this.formulaPane.setMaxHeight(80);
			this.formulaPane.setMinHeight(80);
			this.formulaPane.setPrefHeight(80);
		}
		this.formulaPane.getChildren().clear();
		for (int i = 0; i < formula.size(); i++)
		{
			this.formulaPane.getChildren().add(createGrid(i, formula.get(i)));
		}

		if (index > 0 && index < formula.size())
		{
			// TODO use index for current element

		}
	}

	public void displayOutLine(int selfPoint, int otherPoint, CustomArrow.ArrowDirection direction, int where, boolean isNeedCrossLine)
	{
		this.selfRectangle.displayOutLine(selfPoint, direction, where, isNeedCrossLine);
		this.otherRectangle.displayOutLine(otherPoint, direction, where, isNeedCrossLine);
	}

	public void displayPart(PieceKind kind, String name, Range range, String first, String second)
	{
		this.cbParameters.getSelectionModel().select(kind);
		this.labelControlId.setText(name);
		this.cbRange.getSelectionModel().select(range);
		this.expressionFieldFirst.setText(first);
		this.expressionFieldSecond.setText(second);
	}

	public void clearArrow()
	{
		this.customArrow.hide();
		this.otherRectangle.clearOutline();
		this.selfRectangle.clearOutline();
	}

	public void clearControls()
	{
		this.controlsToggleGroup.getToggles().forEach(t -> t.setSelected(false));
	}

	public void selectControl(String name)
	{
		this.controlsToggleGroup.getToggles()
				.stream()
				.filter(t -> Str.areEqual(((IControl) t.getUserData()).getID(), name))
				.findFirst()
				.ifPresent(t -> t.setSelected(true));
	}

	public void displayIds()
	{
		this.mapIdText.values().forEach(text -> text.setVisible(true));
	}

	public void hideIds()
	{
		this.mapIdText.values().forEach(text -> text.setVisible(false));
	}

	public void resizeImage(double width, double height)
	{
		this.imageView.setFitHeight(height);
		this.imageView.setFitWidth(width);
		this.customGrid.setSize((int) width, (int) height);
	}

	public void resizeIds(double zoom)
	{
		this.mapIdText.forEach((idWithCoordinates, text1) -> {
			text1.setX(idWithCoordinates.x * zoom);
			text1.setY(idWithCoordinates.y * zoom + textOffsetY(zoom));
		});
	}

	// ==============================================================================================================================
	// private methods
	// ==============================================================================================================================
	private int textOffsetY(double zoom)
	{
		return zoom >= 1 ? 12 : (int) (zoom * 12);
	}

	private Alert createAlert(String title)
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) alert.getDialogPane().getScene().getWindow()));
		Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
		okButton.setDefaultButton(false);

		alert.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		alert.setTitle(title);
		alert.setResizable(true);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.getDialogPane().setPrefHeight(800);
		alert.getDialogPane().setPrefWidth(800);
		return alert;
	}

	private void createCanvas()
	{
		this.group = new Group();
		this.mainScrollPane = new ScrollPane(this.group);
		mainScrollPane.setContent(this.group);
		mainScrollPane.setFitToHeight(true);
		mainScrollPane.setFitToWidth(true);
		mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.imageView = new ImageView();
		ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		AnchorPane.setTopAnchor(progressIndicator, (double) 200);
		AnchorPane.setLeftAnchor(progressIndicator, (double) 200);

		this.selfRectangle = new ScalableArrow();
		this.otherRectangle = new ScalableArrow();
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

	private Node createGrid(int index, FormulaPart part)
	{
		CustomToggleButton node = new CustomToggleButton();
		node.setUserData(index);
		StringBuilder sb = new StringBuilder(part.getKind().toString());
		if (part.getKind().useName())
		{
			sb.append("\n").append(part.getName());
		}
		if (part.getKind().useRange())
		{
			sb.append("\n").append(part.getRange().toString(part.getFirst(), part.getSecond()));
		}

		double asDouble = Arrays.stream(sb.toString().split("\n")).mapToDouble(String::length).max().getAsDouble();
		double width = asDouble * 10 + 20;
		node.setMaxWidth(width);
		node.setMinWidth(width);
		node.setPrefWidth(width);

		Hyperlink link = new Hyperlink("x");
		link.getStyleClass().addAll(CssVariables.CUSTOM_FIELD_CUSTOM_BUTTON);
		node.setRight(link);
		node.setText(sb.toString());
		link.setOnAction(event -> this.model.removePart(index));
		node.setTooltip(new Tooltip(part.toString()));
		node.setTextAlignment(TextAlignment.LEFT);
		node.getStyleClass().add(CssVariables.FORMULA_TOGGLE_BUTTON);
		node.setToggleGroup(this.formulaToggleGroup);
		return node;
	}

	private void listeners()
	{
		this.cbUseId.selectedProperty().addListener((observable, oldValue, newValue) -> {
			this.model.displayIds(newValue);
		});
		this.controlsToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				this.model.displayControl((IControl) newValue.getUserData(), false);
				this.model.displayDistance((IControl) newValue.getUserData(), this.cbParameters.getSelectionModel().getSelectedItem());
			}
			else
			{
				Common.tryCatch(this.model::clearCanvas, "Error on clear canvas");
				this.clearDistance();
				this.clearArrow();
			}
		});

		this.formulaToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				this.cfFindControl.setText("");
				this.model.displayPart(((int) newValue.getUserData()));
				this.btnAddFormula.setText("✔");
			}
			else
			{
				this.btnAddFormula.setText("+");
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
		this.cbUseGrid.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue)
			{
				this.customGrid.show();
			}
			else
			{
				this.customGrid.hide();
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
			this.cbRange.setVisible(newValue.useRange());
			this.labelControlId.setVisible(newValue.useName());
			if (newValue.useRange() && newValue.useName())
			{
				c0.setPercentWidth(20);
				c1.setPercentWidth(15);
				c2.setPercentWidth(15);
			}
			else if (newValue.useRange())
			{
				c0.setPercentWidth(35);
				c1.setPercentWidth(0);
				c2.setPercentWidth(15);
			}
			else if (newValue.useName())
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
			Toggle selectedToggle = this.controlsToggleGroup.getSelectedToggle();
			this.model.displayDistance(selectedToggle == null ? null : ((IControl) selectedToggle.getUserData()), newValue);
		});
	}
}
