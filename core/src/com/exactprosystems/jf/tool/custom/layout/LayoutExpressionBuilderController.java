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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.expfield.NewExpressionField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class LayoutExpressionBuilderController implements Initializable, ContainingParent
{
	private static final int BORDER_WIDTH = 4;
	private static final int OFFSET = BORDER_WIDTH / 2;
	@FXML
	private HBox formulaPane;
	@FXML
	private CheckBox cbUseBorder;
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
	private TextField cfFindControl;
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

	private NewExpressionField expressionFieldFirst;
	private NewExpressionField expressionFieldSecond;
	private ToggleGroup mainToggleGroup;
	private ImageView imageView;
	private Parent parent;
	private LayoutExpressionBuilder model;

	private CustomGrid customGrid;
	private CustomRectangle selfRectangle;
	private CustomRectangle otherRectangle;
	private CustomArrow customArrow;

	private ScrollPane mainScrollPane;
	private boolean useBorder = true;
	private ArrayList<ToggleButton> buttons = new ArrayList<>();

	// ==============================================================================================================================
	// interface Initializable
	// ==============================================================================================================================
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		assert vBoxControls != null : "fx:id=\"vBoxControls\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert cfFindControl != null : "fx:id=\"cfFindControl\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";

		this.cbRange.setVisible(false);
		this.labelControlId.setVisible(false);
		this.cbParameters.getItems().addAll(PieceKind.values());
		this.cbRange.getItems().addAll(Range.values());
		this.cbParameters.getSelectionModel().selectFirst();
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
				this.model.displayDistance((IControl) newValue.getUserData(), this.cbParameters.getSelectionModel().getSelectedItem());
			}
			else
			{
				Common.tryCatch(this.model::clearCanvas, "Error on clear canvas");
				this.clearDistance();
				this.clearArrow();
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
		this.cbUseBorder.selectedProperty().addListener((observable, oldValue, newValue) -> {
			useBorder = newValue;
			if (this.otherRectangle.isInit())
			{
				this.otherRectangle.setVisible(newValue);
			}
			this.selfRectangle.setVisible(newValue);
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
			Toggle selectedToggle = this.mainToggleGroup.getSelectedToggle();
			this.model.displayDistance(selectedToggle == null ? null : ((IControl) selectedToggle.getUserData()), newValue);
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
		this.expressionFieldFirst.setVisible(false);
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
		Common.tryCatch(() -> this.model.addFormula(this.cbParameters.getSelectionModel().getSelectedItem(), this.labelControlId.getText(), this.cbRange.getSelectionModel().getSelectedItem(), this.expressionFieldFirst.getText(), this.expressionFieldSecond.getText()), "Error on add formula");
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
		this.customGrid.setSize((int) image.getWidth(), (int) image.getHeight());
		this.progressIndicator = null;
	}

	public void displayControl(Rectangle rectangle, boolean self)
	{
		CustomRectangle rect = self ? this.selfRectangle : this.otherRectangle;
		String styleClass = self ? CssVariables.SELF_CONTROL : CssVariables.OTHER_CONTROL;
		rect.addStyleClass(styleClass);
		rect.updateRectangle(rectangle.getX() + OFFSET, rectangle.getY() + OFFSET, rectangle.getWidth() - BORDER_WIDTH, rectangle.getHeight() - BORDER_WIDTH);
		rect.setInit(true);
		if (useBorder)
		{
			rect.setVisible(true);
		}
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
		this.otherRectangle.setInit(false);
	}

	public void displayArrow(int start, int end, int where, CustomArrow.ArrowDirection position)
	{
		this.customArrow.setPoints(start, end);
		this.customArrow.setDirection(position);
		this.customArrow.show(where);
	}

	public void displayFormula(List<FormulaPart> parse)
	{
		if (parse.size() > 0 && formulaPane.getChildren().size() == 0)
		{
			formulaPane.setMaxHeight(80);
			formulaPane.setMinHeight(80);
			formulaPane.setPrefHeight(80);
		}
		parse.stream().map(this::createGrid).forEach(formulaPane.getChildren()::add);
	}

	public void clearArrow()
	{
		this.customArrow.hide();
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
		alert.getDialogPane().setPrefHeight(800);
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

		this.selfRectangle = new CustomRectangle();
		this.otherRectangle = new CustomRectangle();
		this.customArrow = new CustomArrow();
		this.selfRectangle.setWidthLine(BORDER_WIDTH);
		this.otherRectangle.setWidthLine(BORDER_WIDTH);

		group.getChildren().add(this.imageView);
		this.selfRectangle.setVisible(false);
		this.selfRectangle.setGroup(group);
		this.customArrow.setGroup(group);
		this.otherRectangle.setVisible(false);
		this.otherRectangle.setGroup(group);

		this.customGrid = new CustomGrid();
		this.customGrid.hide();
		this.customGrid.setGroup(group);
	}

	private Node createGrid(FormulaPart part)
	{
		TextArea textArea = new TextArea();
		textArea.setUserData(part);
		int width = 120;
		textArea.setMaxWidth(width);
		textArea.setMinWidth(width);
		textArea.setPrefWidth(width);
		StringBuilder sb = new StringBuilder(part.getKind().toString());
		if (part.getKind().useName())
		{
			sb.append("\n").append(part.getName());
		}
		if (part.getKind().useRange())
		{
			sb.append("\n").append(part.getRange().toString(part.getFirst(), part.getSecond()));
		}
		textArea.setEditable(false);
		textArea.setText(sb.toString());
		return textArea;
	}
}
