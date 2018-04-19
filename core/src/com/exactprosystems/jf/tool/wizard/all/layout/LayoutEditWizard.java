/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.wizard.all.layout;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.app.PieceKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.WizardMatcher;
import com.exactprosystems.jf.tool.wizard.related.ConnectionBean;
import com.exactprosystems.jf.tool.wizard.related.WizardCommonHelper;
import com.exactprosystems.jf.tool.wizard.related.WizardLoader;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

@WizardAttribute(name = R.LAYOUT_EDIT_WIZARD_NAME, pictureName = "LayoutWizard.png", shortDescription = R.LAYOUT_EDIT_WIZARD_SHORT_DESCRIPTION, detailedDescription = R.LAYOUT_EDIT_WIZARD_DETAILED_DESCRIPTION,
				 criteries = {MatrixFx.class, RawTable.class}, experimental = true, strongCriteries = true, category = WizardCategory.MATRIX

)
public class LayoutEditWizard extends AbstractWizard
{
	private Matrix        matrix;
	private RawTable      item;
	private AppConnection appConnection;
	private WizardMatcher wizardMatcher;
	private WizardLoader  wizardLoader;

	private IWindow           currentWindow;
	private String			  currentWindowName;
	private AbstractEvaluator evaluator;
	private Table             table;

	private ComboBox<ConnectionBean> cbConnections;
	private Label                    lblDialog;
	private ImageViewWithScale       imageViewWithScale;
	private TextArea                 errorArea;
	private Button                   btnCheckTable;
	private ProgressIndicator        piCheckTable;
	private GridPane                 checkGrid;
	private Button                   btnScan;
	private BorderPane               bpView;

	private ToggleGroup distanceGroup;
	private ToggleGroup allOrSignificantGroup;
	private HBox        boxWithRadioButtons;
	private RadioButton rbNumber;
	private RadioButton rbLess;
	private RadioButton rbGreat;
	private RadioButton rbAbout;
	private RadioButton rbBetween;

	private RadioButton rbAll;
	private RadioButton rbSignificant;
	private GridPane    main;
	private Text        waitText;

	private ToggleGroup viewGroup = new ToggleGroup();

	private CheckBox cbVisible;
	private CheckBox cbCount;
	private CheckBox cbWidthHeight;
	private CheckBox cbContains;
	private CheckBox cbNear;
	private CheckBox cbIn;
	private CheckBox cbOn;
	private CheckBox cbAligned;
	private CheckBox cbVCentered;
	private CheckBox cbHCentered;

	private HBox boxWithFunctions;

	private Map<PieceKind, Image> map = new HashMap<>();
	List<Rectangle> topRectangles;

	@Override
	public void init(Context context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);

		this.matrix = super.get(MatrixFx.class, parameters);
		this.item = super.get(RawTable.class, parameters);
		this.evaluator = this.matrix.getFactory().createEvaluator();
	}

	@Override
	public boolean beforeRun()
	{
		return true;
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		return null;
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		fillMap();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double height = screenSize.getHeight() - 150;
		borderPane.setMinHeight(height);
		borderPane.setPrefHeight(height);

		borderPane.setMinWidth(Math.min(1000.0, screenSize.width));
		borderPane.setPrefWidth(Math.min(1000.0, screenSize.width));

		this.cbConnections = new ComboBox<>();
		this.lblDialog = new Label(this.currentWindow.getName());
		this.imageViewWithScale = new ImageViewWithScale();
		this.imageViewWithScale.addShowGrid();
		this.btnScan = new Button(R.WIZARD_SCAN.get());
		this.btnCheckTable = new Button(R.WIZARD_CHECK_TABLE.get());
		this.btnCheckTable.setDisable(true);
		//		this.btnCheckTable.setOnAction(e -> this.checkTable());
		this.piCheckTable = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		this.piCheckTable.setMinSize(32.0, 32.0);
		this.piCheckTable.setPrefSize(32.0, 32.0);
		this.piCheckTable.setMaxSize(32.0, 32.0);
		this.piCheckTable.setVisible(false);
		this.waitText = new Text(R.WIZARD_SELECT_CONNECTION_INFO.get());

		this.main = new GridPane();
		this.main.getStyleClass().addAll(CssVariables.HGAP_MID, CssVariables.VGAP_MID);

		this.checkGrid = new GridPane();
		this.checkGrid.setGridLinesVisible(true);
		ScrollPane sp = new ScrollPane();
		sp.setContent(this.checkGrid);
		sp.setFitToWidth(true);
		sp.setFitToHeight(true);
		this.main.add(sp, 0, 4);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(70.0);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(30.0);
		this.main.getColumnConstraints().addAll(c0, c1);

		final double smallRow = 32.0;
		final double errorRow = 96.0;
		Supplier<RowConstraints> createSmallRow = () -> {
			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(smallRow);
			r0.setMaxHeight(smallRow);
			r0.setPrefHeight(smallRow);
			return r0;
		};

		Supplier<RowConstraints> createBigRow = () -> {
			RowConstraints r1 = new RowConstraints();
			r1.setMinHeight((height - smallRow * 3 - 4 * 8 - errorRow) / 2);
			r1.setPrefHeight((height - smallRow * 3 - 4 * 8 - errorRow) / 2);
			r1.setVgrow(Priority.SOMETIMES);
			r1.setPercentHeight(-1);
			return r1;
		};

		//region image and lv
		{
			this.main.getRowConstraints().addAll(createSmallRow.get(), createBigRow.get());

			HBox connectionBox = new HBox();
			connectionBox.getChildren().addAll(new Label("Connection : "), this.cbConnections);
			this.cbConnections.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(this.cbConnections, Priority.ALWAYS);
			connectionBox.setAlignment(Pos.CENTER_LEFT);

			HBox dialogBox = new HBox();
			dialogBox.getChildren().addAll(new Label("Dialog : "), this.lblDialog);
			this.lblDialog.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(this.lblDialog, Priority.ALWAYS);
			dialogBox.setAlignment(Pos.CENTER_RIGHT);

			this.main.add(connectionBox, 0, 0);
			this.main.add(dialogBox, 1, 0);

			this.main.add(this.waitText, 0, 1);
			GridPane.setHalignment(this.waitText, HPos.CENTER);
		}
		//endregion

		//region scan and checkboxes
		{
			this.main.getRowConstraints().addAll(createSmallRow.get());
			this.main.getRowConstraints().addAll(createSmallRow.get());

			this.boxWithRadioButtons = new HBox();
			HBox cbBoxes = new HBox();
			this.distanceGroup = new ToggleGroup();

			this.rbNumber = new RadioButton("Number");
			this.rbNumber.setToggleGroup(this.distanceGroup);
			this.rbNumber.setSelected(true); // default value
			this.rbAbout = new RadioButton("About");
			this.rbAbout.setToggleGroup(this.distanceGroup);
			this.rbLess = new RadioButton("Less");
			this.rbLess.setToggleGroup(this.distanceGroup);
			this.rbGreat = new RadioButton("Great");
			this.rbGreat.setToggleGroup(this.distanceGroup);
			this.rbBetween = new RadioButton("Between");
			this.rbBetween.setToggleGroup(this.distanceGroup);

			cbBoxes.getChildren().addAll(new Label("Select distance : "), Common.createSpacer(Common.SpacerEnum.HorizontalMid), Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.rbNumber,
					Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.rbAbout, Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.rbLess,
					Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.rbGreat, Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.rbBetween);

			this.rbAll = new RadioButton("All");
			this.allOrSignificantGroup = new ToggleGroup();
			this.rbAll.setToggleGroup(this.allOrSignificantGroup);
			this.rbSignificant = new RadioButton("Significant");
			this.rbSignificant.setToggleGroup(this.allOrSignificantGroup);
			this.rbSignificant.setSelected(true);

			cbBoxes.getChildren().addAll(Common.createSpacer(Common.SpacerEnum.HorizontalMin), new Separator(Orientation.VERTICAL), new Label("Use distances : "),
					Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.rbAll, Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.rbSignificant);
			cbBoxes.setAlignment(Pos.CENTER_LEFT);
			this.boxWithRadioButtons.getChildren().addAll(cbBoxes, this.btnScan);
			HBox.setHgrow(cbBoxes, Priority.ALWAYS);
			this.boxWithRadioButtons.setAlignment(Pos.CENTER_LEFT);
			this.main.add(this.boxWithRadioButtons, 0, 2, 2, 1);

			this.boxWithFunctions = new HBox();

			this.cbVisible = new CheckBox();
			this.cbVisible.setGraphic(new ImageView(this.map.get(PieceKind.VISIBLE)));

			this.cbCount = new CheckBox();
			this.cbCount.setGraphic(new ImageView(this.map.get(PieceKind.COUNT)));

			this.cbWidthHeight = new CheckBox();
			this.cbWidthHeight.setGraphic(new ImageView(this.map.get(PieceKind.WIDTH)));

			this.cbContains = new CheckBox();
			this.cbContains.setGraphic(new ImageView(this.map.get(PieceKind.CONTAINS)));

			this.cbNear = new CheckBox();
			this.cbNear.setGraphic(new ImageView(this.map.get(PieceKind.LEFT)));

			this.cbIn = new CheckBox();
			this.cbIn.setGraphic(new ImageView(this.map.get(PieceKind.INSIDE_LEFT)));

			this.cbOn = new CheckBox();
			this.cbOn.setGraphic(new ImageView(this.map.get(PieceKind.ON_LEFT)));

			this.cbAligned = new CheckBox();
			this.cbAligned.setGraphic(new ImageView(this.map.get(PieceKind.LEFT_ALIGNED)));

			this.cbVCentered = new CheckBox();
			this.cbVCentered.setGraphic(new ImageView(this.map.get(PieceKind.VERTICAL_CENTERED)));

			this.cbHCentered = new CheckBox();
			this.cbHCentered.setGraphic(new ImageView(this.map.get(PieceKind.HORIZONTAL_CENTERED)));

			this.boxWithFunctions.getChildren().add(new Label("Select functions : "));

			Stream.of(this.cbVisible, this.cbCount, this.cbWidthHeight, this.cbContains, this.cbNear, this.cbIn, this.cbOn, this.cbAligned, this.cbVCentered, this.cbHCentered).forEach(cb -> {
				cb.setSelected(true);
				this.boxWithFunctions.getChildren().addAll(Common.createSpacer(Common.SpacerEnum.HorizontalMax), cb);
			});
			this.boxWithFunctions.setAlignment(Pos.CENTER_LEFT);
			this.main.add(this.boxWithFunctions, 0, 3, 2, 1);
		}
		//endregion

		//region table
		{
			this.main.getRowConstraints().addAll(createBigRow.get());
			HBox pane = new HBox();
			pane.setAlignment(Pos.CENTER);
			pane.getChildren().addAll(this.btnCheckTable, this.piCheckTable);
			this.checkGrid.add(pane, 0, 0);

			this.bpView = new BorderPane();
			this.main.add(this.bpView, 1, 4);
		}
		//endregion

		//region errors
		{
			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(errorRow - 4 * 8);
			r0.setPrefHeight(errorRow - 4 * 8);
			r0.setVgrow(Priority.SOMETIMES);
			r0.setPercentHeight(-1);

			this.errorArea = new TextArea();
			this.errorArea.setEditable(false);

			this.main.getRowConstraints().addAll(r0);

			this.main.add(this.errorArea, 0, 5, 2, 1);

		}
		//endregion

		borderPane.setCenter(this.main);

		this.cbConnections.getItems().setAll(WizardCommonHelper.getAllConnections(this.matrix.getFactory().getConfiguration()));

		this.btnScan.setDisable(true);
		this.boxWithRadioButtons.setDisable(true);
		this.boxWithFunctions.setDisable(true);

		hideTableAndView();
		//		listeners();
	}

	//region private methods
	private void fillMap()
	{
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/visible.png"), PieceKind.VISIBLE);                    // v - visible
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/count.png"), PieceKind.COUNT);                    // c - count
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/widthHeight.png"), PieceKind.WIDTH, PieceKind.HEIGHT);    // S - size
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/contains.png"), PieceKind.CONTAINS);                    // c - contains
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/near.png"), PieceKind.LEFT, PieceKind.RIGHT, PieceKind.TOP, PieceKind.BOTTOM); // D - distance
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/in.png"), PieceKind.INSIDE_LEFT, PieceKind.INSIDE_RIGHT, PieceKind.INSIDE_TOP, PieceKind.INSIDE_BOTTOM); // I - inside
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/on.png"), PieceKind.ON_LEFT, PieceKind.ON_RIGHT, PieceKind.ON_TOP, PieceKind.ON_BOTTOM); // O - on
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/align.png"), PieceKind.LEFT_ALIGNED, PieceKind.RIGHT_ALIGNED, PieceKind.TOP_ALIGNED,
				PieceKind.BOTTOM_ALIGNED); //A - align
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/horizontalCentered.png"), PieceKind.HORIZONTAL_CENTERED); // R - centeRed
		this.addToMap(new Image("/com/exactprosystems/jf/tool/wizard/all/icons/verticalCentered.png"), PieceKind.VERTICAL_CENTERED); // R - centeRed
	}

	private void addToMap(Image image, PieceKind... kinds)
	{
		for (PieceKind kind : kinds)
		{
			map.put(kind, image);
		}
	}

	private void hideTableAndView()
	{
		this.errorArea.clear();
		this.checkGrid.getRowConstraints().clear();
		this.checkGrid.getColumnConstraints().clear();
		this.checkGrid.getChildren().removeIf(node -> node instanceof LayoutWizard.RelationButton || node instanceof Text);

		this.checkGrid.setVisible(false);
		this.bpView.getChildren().clear();
	}


	//endregion
}
