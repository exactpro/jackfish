package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
		name = "LayoutWizard",
		pictureName = "AutomateConverterWizard.png",
		category = WizardCategory.MATRIX,
		shortDescription = "Short description",
		strongCriteries = true,
		experimental = true,
		criteries = {MatrixFx.class, MatrixItem.class},
		detailedDescription = "Detailed description"
)
public class LayoutWizard extends AbstractWizard
{
	private Matrix matrix;
	private MatrixItem item;

	private ComboBox<AppConnection> cbConnections;
	private ComboBox<IWindow>       cbDialogs;

	private ImageViewWithScale imageViewWithScale;
	private ListView<IControl> lvControls;

	private Button btnCheckTable;

	private Button btnScan;

	@Override
	public void init(IContext context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);
		this.matrix = super.get(MatrixFx.class, parameters);
		this.item = super.get(MatrixItem.class, parameters);
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		borderPane.setMinHeight(800.0);
		borderPane.setPrefHeight(800.0);

		borderPane.setMinWidth(800.0);
		borderPane.setPrefWidth(800.0);

		this.cbConnections = new ComboBox<>();
		this.cbDialogs = new ComboBox<>();
		this.imageViewWithScale = new ImageViewWithScale();
		this.lvControls = new ListView<>();
		this.btnScan = new Button("Scan");
		this.btnCheckTable = new Button("Check table");

		VBox main = new VBox();

		//region image and lv
		{
			GridPane gpTop = new GridPane();
			gpTop.getStyleClass().addAll(CssVariables.HGAP_MID, CssVariables.VGAP_MID);

			ColumnConstraints c0 = new ColumnConstraints();
			c0.setPercentWidth(70.0);
			ColumnConstraints c1 = new ColumnConstraints();
			c1.setPercentWidth(30.0);
			gpTop.getColumnConstraints().addAll(c0, c1);

			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(32.0);
			r0.setMaxHeight(32.0);
			r0.setPrefHeight(32.0);
			r0.setPercentHeight(-1);

			RowConstraints r1 = new RowConstraints();
			r1.setVgrow(Priority.ALWAYS);

			gpTop.getRowConstraints().addAll(r0, r1);

			HBox connectionBox = new HBox();
			connectionBox.getChildren().addAll(new Label("Connection : "), this.cbConnections);
			this.cbConnections.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(this.cbConnections, Priority.ALWAYS);
			connectionBox.setAlignment(Pos.CENTER_LEFT);

			HBox dialogBox = new HBox();
			dialogBox.getChildren().addAll(new Label("Dialog : "), this.cbDialogs);
			this.cbDialogs.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(this.cbDialogs, Priority.ALWAYS);
			dialogBox.setAlignment(Pos.CENTER_RIGHT);

			gpTop.add(connectionBox, 0, 0);
			gpTop.add(dialogBox, 1, 0);

			main.getChildren().add(gpTop);
			VBox.setVgrow(gpTop, Priority.ALWAYS);
		}
		//endregion

		main.getChildren().add(Common.createSpacer(Common.SpacerEnum.VerticalMid));

		//region scan and checkboxes
		{
			HBox box = new HBox();
			HBox cbBoxes = new HBox();
			//TODO fill cbBoxes

			box.getChildren().addAll(cbBoxes, this.btnScan);
			HBox.setHgrow(cbBoxes, Priority.ALWAYS);
			box.setAlignment(Pos.CENTER_LEFT);
			main.getChildren().add(box);
		}
		//endregion

		main.getChildren().add(Common.createSpacer(Common.SpacerEnum.VerticalMid));

		//region table
		{
			GridPane gp = new GridPane();
			gp.getStyleClass().addAll(CssVariables.HGAP_MID, CssVariables.VGAP_MID);

			ColumnConstraints c0 = new ColumnConstraints();
			c0.setPercentWidth(70.0);
			ColumnConstraints c1 = new ColumnConstraints();
			c1.setPercentWidth(30.0);
			gp.getColumnConstraints().addAll(c0, c1);

			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(32.0);
			r0.setMaxHeight(32.0);
			r0.setPrefHeight(32.0);
			r0.setPercentHeight(-1);

			RowConstraints r1 = new RowConstraints();
			r1.setVgrow(Priority.ALWAYS);

			gp.getRowConstraints().addAll(r1, r0);

			VBox.setVgrow(gp, Priority.ALWAYS);
			main.getChildren().add(gp);
			gp.setGridLinesVisible(true);
			gp.add(this.btnCheckTable, 1,1);

		}
		//endregion

		borderPane.setCenter(main);

		listeners();
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		return ArrayList::new;
	}

	@Override
	public boolean beforeRun()
	{
		return true;
	}

	private void listeners()
	{

	}
}
