package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.Range;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.fields.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.fields.NewExpressionField;
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
import java.util.Optional;
import java.util.ResourceBundle;

//TODO replace all border width and fillStroke to styleClass
public class LayoutExpressionBuilderController implements Initializable, ContainingParent
{
	public static final int BORDER_WIDTH = 4;
	public static final int OFFSET = BORDER_WIDTH / 2;

	public BorderPane mainPane;

	public VBox vBoxControls;
	public CustomFieldWithButton cfFindControl;
	public HBox hBoxCheckBoxes;
	public BorderPane parentPane;
	public ScrollPane spControls;
	public ChoiceBox<String> cbParameters;
	public Label labelControlId;
	public ChoiceBox<Range> cbRange;
	public Button btnAddFormula;
	public GridPane gridPane;
	private NewExpressionField expressionFieldFirst;
	private NewExpressionField expressionFieldSecond;
	private ToggleGroup mainToggleGroup;

	private ImageView imageView;
	private Parent parent;
	private LayoutExpressionBuilder model;

	private CustomRectangle initialRectangle;
	private CustomRectangle selectedRectangle;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		assert vBoxControls != null : "fx:id=\"vBoxControls\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert cfFindControl != null : "fx:id=\"cfFindControl\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert hBoxCheckBoxes != null : "fx:id=\"hBoxCheckBoxes\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert parentPane != null : "fx:id=\"parentPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		this.cbParameters.getItems().addAll(
				"visible", "count", "contains",
				"left", "right", "top", "bottom",
				"inLeft","inRight","inTop","inBottom",
				"onLeft","onRight","onTop","onBottom",
				"lAlign","rAlign","tAlign","bAlign",
				"hCenter","vCenter"
		);
		this.cbParameters.getSelectionModel().select("visible");
		this.cbParameters.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			//TODO add logic
		});
		this.cbRange.getItems().addAll(Range.values());
		this.cbRange.getSelectionModel().select(Range.EQUAL);
		this.cbRange.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			GridPane.setColumnSpan(this.expressionFieldFirst, 2);
			this.expressionFieldSecond.setVisible(false);
			if (newValue == Range.BETWEEN)
			{
				this.expressionFieldSecond.setVisible(true);
				GridPane.setColumnSpan(this.expressionFieldFirst, 1);
			}
		});
		this.mainToggleGroup = new ToggleGroup();
		this.mainToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				this.model.displayControl(((IControl) newValue.getUserData()));
			}
			else
			{
				Common.tryCatch(this.model::clearCanvas, "Error on clear canvas");
			}
		});
		this.cfFindControl.textProperty().addListener((observable, oldValue, newValue) -> {
			//TODO implements this logic via DialogsHelper.showFindListView()
		});
		createCanvas();
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

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(LayoutExpressionBuilder model, AbstractEvaluator evaluator, BufferedImage bufferedImage) throws Exception
	{
		this.model = model;
		this.expressionFieldFirst = new NewExpressionField(evaluator, "first");
		this.expressionFieldSecond= new NewExpressionField(evaluator, "second");
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

	public void addFormula(ActionEvent actionEvent)
	{
		Common.tryCatch(()-> this.model.addFormula(cbParameters.getSelectionModel().getSelectedItem(), labelControlId.getText(), cbRange.getSelectionModel().getSelectedItem(), "first","second"),"Error on add formula");
	}

	public void displayInitialControl(Rectangle rectangle)
	{
		this.initialRectangle.setColor(new Color(1, 0, 0, 1));
		this.initialRectangle.updateRectangle(rectangle.getX() + OFFSET, rectangle.getY() + OFFSET, rectangle.getWidth() - BORDER_WIDTH, rectangle.getHeight() - BORDER_WIDTH);
		this.initialRectangle.setVisible(true);
	}

	public void displayControl(Rectangle rectangle)
	{
		this.selectedRectangle.setColor(new Color(0, 1, 0, 1));
		this.selectedRectangle.updateRectangle(rectangle.getX() + OFFSET, rectangle.getY() + OFFSET, rectangle.getWidth() - BORDER_WIDTH, rectangle.getHeight() - BORDER_WIDTH);
		this.selectedRectangle.setVisible(true);
	}

	public void clearCanvas()
	{
		this.selectedRectangle.setVisible(false);
	}

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

	public void displayControlId(String controlId)
	{
		this.labelControlId.setText(controlId);
	}
}
