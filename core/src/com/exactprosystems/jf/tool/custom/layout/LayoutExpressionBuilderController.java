package com.exactprosystems.jf.tool.custom.layout;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.fields.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.fields.NewExpressionField;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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
	public static final int BORDER_WIDTH =	4;

	public BorderPane						mainPane;

	public VBox								vBoxControls;
	public CustomFieldWithButton			cfFindControl;
	public HBox								hBoxCheckBoxes;
	public BorderPane						bottomPane;
	public BorderPane						parentPane;
	public ScrollPane						spControls;
	private NewExpressionField				expressionField;
	private ToggleGroup						mainToggleGroup;

	private Canvas							canvas;
	private ImageView						imageView;
	private GraphicsContext					graphicsContext;

	private Parent							parent;
	private LayoutExpressionBuilder			model;

	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		assert vBoxControls != null : "fx:id=\"vBoxControls\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert bottomPane != null : "fx:id=\"bottomPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert cfFindControl != null : "fx:id=\"cfFindControl\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert hBoxCheckBoxes != null : "fx:id=\"hBoxCheckBoxes\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
		assert parentPane != null : "fx:id=\"parentPane\" was not injected: check your FXML file 'LayoutExpressionBuilder.fxml'.";
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
		this.canvas = new Canvas();
		this.graphicsContext = this.canvas.getGraphicsContext2D();
		this.graphicsContext.setLineWidth(BORDER_WIDTH);
		this.graphicsContext.setLineDashes(10, 10, 10);
		group.getChildren().add(this.imageView);
		group.getChildren().add(this.canvas);
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(LayoutExpressionBuilder model, AbstractEvaluator evaluator, BufferedImage bufferedImage) throws Exception
	{
		this.model = model;
		this.expressionField = new NewExpressionField(evaluator, "expression Field");
		this.bottomPane.setBottom(this.expressionField);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", outputStream);
		Image image = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
		this.imageView.setImage(image);
		this.canvas.setHeight(image.getHeight());
		this.canvas.setWidth(image.getWidth());
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

	public void displayInitialControl(Rectangle rectangle)
	{
		this.graphicsContext.setStroke(new Color(1,0,0,1));
		this.graphicsContext.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
	}

	public void displayControl(Rectangle rectangle)
	{
		this.graphicsContext.setStroke(new Color(0,1,0,1));
		this.graphicsContext.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
	}

	public void clearCanvas()
	{
		this.graphicsContext.clearRect(0, 0, this.imageView.getImage().getWidth(), this.imageView.getImage().getHeight());
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
}
