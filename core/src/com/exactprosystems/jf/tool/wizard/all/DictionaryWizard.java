////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.elementstable.ElementsTable;
import com.exactprosystems.jf.tool.custom.elementstable.TableBean;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.xmltree.XmlTreeView;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.WizardHelper;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@WizardAttribute(
        name 				= "Test dictionary wizard",
        pictureName 		= "DictionaryWizard.png",
        category 			= WizardCategory.GUI_DICTIONARY,
        shortDescription 	= "This wizard is only for test purpose.",
        detailedDescription = "Here you descrioption might be",
        experimental 		= true,
        strongCriteries 	= true,
        criteries 			= { DictionaryFx.class, Window.class }
    )
public class DictionaryWizard extends AbstractWizard
{
	private AppConnection currentConnection = null;
	private DictionaryFx  currentDictionary = null;
	private Window        currentWindow     = null;
	private Window        copyWindow        = null;

	private volatile Document document    = null;
	private volatile Node     currentNode = null;

	private SplitPane                   centralSplitPane   = null;
	private ImageViewWithScale          imageViewWithScale = null;
	private XmlTreeView                 xmlTreeView        = null;
	private FindPanel<org.w3c.dom.Node> findPanel          = null;

	private TextField                    tfDialogName;
	private Label                        lblSelfId;
	private javafx.scene.control.Button  btnGenerateOnOpen;
	private javafx.scene.control.Button  btnGenerateOnClose;
	private ElementsTable				 tableView;
	private Button btnWizard;
	private Button btnNextMark;
	private Button btnPrevMark;
	private CheckBox cbUpdate;
	private CheckBox cbAdd;
	private CheckBox cbMark;
	private CheckBox cbQuestion;
	

	public DictionaryWizard()
	{
	}

	//region AbstractWizard methods
	@Override
	public void init(IContext context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);

		this.currentConnection = super.get(AppConnection.class, parameters);
		this.currentDictionary = super.get(DictionaryFx.class, parameters);
		this.currentWindow = super.get(Window.class, parameters);
		try
		{
			this.copyWindow = Window.createCopy(this.currentWindow);
			this.copyWindow.setName(this.currentWindow.getName());
		}
		catch (Exception e)
		{
			//TODO AB. I have no idea, that we need do on a exception.
		}
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		borderPane.setPrefHeight(1000.0);
		borderPane.setPrefWidth(1500.0);

		this.centralSplitPane = new SplitPane();
		this.centralSplitPane.setOrientation(Orientation.VERTICAL);
		this.centralSplitPane.setDividerPositions(0.5);
		this.centralSplitPane.setPrefSize(160.0, 200.0);
		BorderPane.setAlignment(this.centralSplitPane, Pos.CENTER);

		this.xmlTreeView = new XmlTreeView();

		this.imageViewWithScale = new ImageViewWithScale();
		this.imageViewWithScale.setOnRectangleClick(rectangle -> this.xmlTreeView.selectItem(rectangle));

		this.findPanel = new FindPanel<>();
		this.findPanel.getStyleClass().remove(CssVariables.FIND_PANEL);

		//region top
		SplitPane topSplitPane = new SplitPane();
		topSplitPane.setPrefSize(200.0, 200.0);
		topSplitPane.setDividerPositions(0.5);

		topSplitPane.getItems().add(this.imageViewWithScale);

		BorderPane bp = new BorderPane();
		bp.setPrefSize(200.0, 200.0);
		bp.setMinWidth(538.0);
		topSplitPane.getItems().add(bp);

		VBox vBox = new VBox();
		BorderPane.setAlignment(vBox, Pos.CENTER);
		bp.setTop(vBox);

		GridPane gridPane = new GridPane();
		VBox.setVgrow(gridPane, Priority.ALWAYS);

		Function<Double, ColumnConstraints> createColumn = percent ->
		{
			ColumnConstraints c = new ColumnConstraints();
			c.setHalignment(HPos.CENTER);
			c.setHgrow(Priority.SOMETIMES);
			c.setMinWidth(10.0);
			c.setPercentWidth(percent);
			c.setPrefWidth(100.0);
			return c;
		};
		gridPane.getColumnConstraints().addAll(
				createColumn.apply(15.0)
				, createColumn.apply(25.0)
				, createColumn.apply(10.0)
				, createColumn.apply(15.0)
				, createColumn.apply(35.0)
		);

		RowConstraints r0 = new RowConstraints();
		r0.setMinHeight(10.0);
		r0.setPrefHeight(30.0);
		r0.setVgrow(Priority.SOMETIMES);
		gridPane.getRowConstraints().addAll(r0);

		Label lblDialog = new Label("Dialog : ");
		gridPane.add(lblDialog, 0, 0);
		GridPane.setHalignment(lblDialog, HPos.RIGHT);

		this.tfDialogName = new TextField();
		this.tfDialogName.setText(this.copyWindow.getName());
		gridPane.add(this.tfDialogName, 1, 0);

		Label lblSelf = new Label("Self id : ");
		gridPane.add(lblSelf, 2, 0);
		GridPane.setHalignment(lblSelf, HPos.RIGHT);

		this.lblSelfId = new Label();
		Common.tryCatch(() -> this.lblSelfId.setText(this.copyWindow.getSelfControl().getID()), "Error on set self id");
		gridPane.add(this.lblSelfId, 3, 0);
		GridPane.setHalignment(this.lblSelfId, HPos.LEFT);
		this.lblSelfId.getStyleClass().add(CssVariables.BOLD_LABEL);

		HBox hBox = new HBox();
		gridPane.add(hBox, 4, 0);
		GridPane.setHalignment(hBox, HPos.CENTER);
		GridPane.setValignment(hBox, VPos.CENTER);
		hBox.setAlignment(Pos.CENTER_RIGHT);

		this.btnGenerateOnOpen = new Button("On open");
		this.btnGenerateOnClose = new Button("On close");
		hBox.getChildren().addAll(this.btnGenerateOnOpen, Common.createSpacer(Common.SpacerEnum.HorizontalMid), this.btnGenerateOnClose);

		vBox.getChildren().addAll(Common.createSpacer(Common.SpacerEnum.VerticalMid), gridPane, Common.createSpacer(Common.SpacerEnum.VerticalMid));

		this.tableView = new ElementsTable();
		BorderPane.setAlignment(this.tableView, Pos.CENTER);
		bp.setCenter(this.tableView);

		//endregion

		//region bottom
		BorderPane paneTreeView = new BorderPane();
		paneTreeView.setPrefSize(200.0, 200.0);
		paneTreeView.setCenter(this.xmlTreeView);
		
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPrefSize(1498.0, 30.0);

		this.btnWizard = new Button();
		this.btnWizard.setId("btnWizard");
		this.btnWizard.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		this.btnNextMark = new Button();
		this.btnNextMark.setId("btnNextMark");
		this.btnNextMark.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		this.btnPrevMark = new Button();
		this.btnPrevMark.setId("btnPrevMark");
		this.btnPrevMark.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		this.cbUpdate = new CheckBox("0");
		this.cbUpdate.setId("cbUpdate");
		this.cbUpdate.setMinWidth(75.0);
		this.cbUpdate.setPrefWidth(75.0);
		this.cbUpdate.setMaxWidth(75.0);
		this.cbUpdate.setSelected(true);

		this.cbAdd = new CheckBox("0");
		this.cbAdd.setId("cbAdd");
		this.cbAdd.setMinWidth(75.0);
		this.cbAdd.setPrefWidth(75.0);
		this.cbAdd.setMaxWidth(75.0);
		this.cbAdd.setSelected(true);

		this.cbMark = new CheckBox("0");
		this.cbMark.setId("cbMark");
		this.cbMark.setMinWidth(75.0);
		this.cbMark.setPrefWidth(75.0);
		this.cbMark.setMaxWidth(75.0);
		this.cbMark.setSelected(true);

		this.cbQuestion = new CheckBox("0");
		this.cbQuestion.setId("cbQuestion");
		this.cbQuestion.setMinWidth(75.0);
		this.cbQuestion.setPrefWidth(75.0);
		this.cbQuestion.setMaxWidth(75.0);
		this.cbQuestion.setSelected(true);

		GridPane gp = new GridPane();
		HBox.setHgrow(gp, Priority.ALWAYS);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setHgrow(Priority.SOMETIMES);
		c0.setMinWidth(10.0);
		c0.setPrefWidth(100.0);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setHgrow(Priority.SOMETIMES);
		c1.setMinWidth(10.0);
		c1.setPercentWidth(65.0);
		c1.setPrefWidth(100.0);

		gp.getColumnConstraints().addAll(c0, c1);

		RowConstraints r00 = new RowConstraints();
		r00.setMinHeight(10.0);
		r00.setPrefHeight(30.0);
		r00.setVgrow(Priority.SOMETIMES);
		gp.getRowConstraints().addAll(r00);

		gp.add(this.findPanel, 1, 0);
		box.getChildren().addAll(
				  this.btnWizard
				, new Separator(Orientation.VERTICAL)
				, this.btnNextMark
				, this.btnPrevMark
				, new Separator(Orientation.VERTICAL)
				, this.cbUpdate
				, new Separator(Orientation.VERTICAL)
				, this.cbAdd
				, new Separator(Orientation.VERTICAL)
				, this.cbMark
				, new Separator(Orientation.VERTICAL)
				, this.cbQuestion
				, new Separator(Orientation.VERTICAL)
				, gp
		);
		paneTreeView.setTop(box);
		//endregion

		this.centralSplitPane.getItems().addAll(topSplitPane, paneTreeView);
		borderPane.setCenter(this.centralSplitPane);
		initListeners();
		updateOnButtons();
		displayElements();
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		return () -> CommandBuilder
				.start()
				//TODO uncomment the lines below
//				.replaceWindow(this.currentDictionary, this.currentWindow, this.copyWindow)
//				.displayWindow(this.currentDictionary, this.copyWindow)
				.build();
	}

	@Override
	public boolean beforeRun()
	{
		try
		{
			if (this.currentConnection == null || !this.currentConnection.isGood())
			{
				DialogsHelper.showError("Application is not started.\nStart it before call the wizard.");
				return false;
			}

			IControl self = this.currentWindow.getSelfControl();
			WizardHelper.gainImageAndDocument(this.currentConnection, self, (image, doc) ->
			{
				this.imageViewWithScale.displayImage(image);

				this.document = doc;
				this.xmlTreeView.displayDocument(this.document);
				List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
				this.imageViewWithScale.setListForSearch(list);
			}, ex ->
			{
				String message = ex.getMessage();
				if (ex.getCause() instanceof JFRemoteException)
				{
					message = ((JFRemoteException) ex.getCause()).getErrorKind().toString();
				}
				DialogsHelper.showError(message);
			});
		}
		catch (Exception e)
		{
			DialogsHelper.showError(e.getMessage());
			return false;
		}

		return true;
	}
	//endregion

	//region private methods
	private void initListeners()
	{
		this.findPanel.setListener(new IFind<org.w3c.dom.Node>()
		{
			@Override
			public void find(org.w3c.dom.Node item)
			{
				xmlTreeView.select(item);
			}

			@Override
			public List<org.w3c.dom.Node> findItem(String what, boolean matchCase, boolean wholeWord)
			{
				return xmlTreeView.findItem(what, matchCase, wholeWord);
			}
		});

		this.xmlTreeView.setOnSelectionChanged((oldItem, oldMarker, newItem, newMarker) ->
		{
			if (oldItem != null)
			{
				this.imageViewWithScale.hideRectangle(oldItem.getRectangle(), oldMarker);
				if (oldItem.getStyle() != null)
				{
					this.imageViewWithScale.showRectangle(oldItem.getRectangle(), oldItem.getStyle(), oldItem.getText(), false);
				}
			}

			if (newItem != null)
			{
				if (newMarker != null)
				{
					this.imageViewWithScale.showRectangle(newItem.getRectangle(), newMarker, newItem.getText(), true);
				}
			}
		});

		this.xmlTreeView.setOnMarkerChanged((item, oldMarker, newMarker, selected) ->
		{
			if (item != null)
			{
				this.imageViewWithScale.hideRectangle(item.getRectangle(), oldMarker);
				this.imageViewWithScale.showRectangle(item.getRectangle(), newMarker, item.getText(), selected);
			}
		});

		this.btnNextMark.setOnAction(e -> this.xmlTreeView.selectNextMark());
		this.btnPrevMark.setOnAction(e -> this.xmlTreeView.selectPrevMark());

		this.btnWizard.setOnAction(e ->
		{
			//TODO
		});

		this.btnGenerateOnOpen.setOnAction(e -> Common.tryCatch(() -> {
			AbstractControl onOpen = generate(Addition.WaitToAppear);
			Section section = (Section) this.copyWindow.getSection(IWindow.SectionKind.OnOpen);
			section.clearSection();
			section.addControl(onOpen);
			updateOnButtons();
		}, "Error on generate onOpen"));
		this.btnGenerateOnClose.setOnAction(e ->Common.tryCatch(() -> {
			AbstractControl onOpen = generate(Addition.WaitToDisappear);
			Section section = (Section) this.copyWindow.getSection(IWindow.SectionKind.OnClose);
			section.clearSection();
			section.addControl(onOpen);
			updateOnButtons();
		}, "Error on generate onClose"));
	}

	private void updateOnButtons()
	{
		boolean onOpenEmpty = this.copyWindow.getSection(IWindow.SectionKind.OnOpen).getControls().isEmpty();
		boolean onCloseEmpty = this.copyWindow.getSection(IWindow.SectionKind.OnClose).getControls().isEmpty();

		this.btnGenerateOnOpen.setStyle("-fx-background-color : " + (!onOpenEmpty ? "rgba(0,255,0, 0.1)" : "rgba(255,0,0, 0.1)"));
		this.btnGenerateOnClose.setStyle("-fx-background-color : " + (!onCloseEmpty ? "rgba(0,255,0, 0.1)" : "rgba(255,0,0, 0.1)"));
	}

	private AbstractControl generate(Addition addition) throws Exception
	{
		AbstractControl on = AbstractControl.create(ControlKind.Wait);
		on.set(AbstractControl.refIdName, this.copyWindow.getSelfControl().getID());
		on.set(AbstractControl.additionName, addition);
		on.set(AbstractControl.timeoutName, 5000);
		on.set(AbstractControl.idName, addition == Addition.WaitToAppear ? "waitOpen" : "waitClose");
		return on;
	}

	private void displayElements()
	{
		Collection<IControl> controls = this.copyWindow.getControls(IWindow.SectionKind.Run);
		this.tableView.getItems().addAll(
				controls
						.stream()
						.map(c -> (AbstractControl)c)
						.map(TableBean::new)
						.collect(Collectors.toList())
		);
	}
	//endregion
}
