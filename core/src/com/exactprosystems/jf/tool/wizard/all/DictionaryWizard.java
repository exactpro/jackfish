////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.guidic.*;
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
import com.exactprosystems.jf.tool.wizard.WizardMatcher;
import com.exactprosystems.jf.tool.wizard.WizardSettings;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.WizardLoader;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@WizardAttribute(
        name 				= "Dictionary wizard",
        pictureName 		= "DictionaryWizard.png",
        category 			= WizardCategory.GUI_DICTIONARY,
        shortDescription 	= "The wizard helps to fill dialogs with elements and find them after they were changed.",
        experimental 		= false,
        strongCriteries 	= true,
        criteries 			= { DictionaryFx.class, Window.class },
		detailedDescription = "To open the wizard, you need :`}}"
				+ "{{`1. Start application.`}}"
				+ "{{`2. Fill self section with valid self control (self control must be found in your application in usual way)`}}"
				+ "{{` `}}"
				+ "{{`All changes applying only for press button Accept. If you do some changes and clicking button Refuse - nothing happens.`}}"
				+ "{{` `}}"
				+ "{{`After opening the wizard, wizard start to load and displaying image and tree from the application. `}}"
				+ "{{`After successful displaying tree, wizard start to find elements from section Run and display these elements on tree via special icons.`}}"
				+ "{{` `}}"
				+ "{{`Image pane ( top left pane) possibilities :`}}"
				+ "{{`   1. Zoom in/out.`}}"
				+ "{{`   2. Inspect element (like on xpath builder)`}}"
				+ "{{`   3. Show/hide id's of found elements.`}}"
				+ "{{`   4. Show mouse coordinates and pixel color under cursor.`}}"
				+ "{{` `}}"
				+ "{{`Table (top right pane) possibilities`}}"
				+ "{{`   1. Change name of current dialog.`}}"
				+ "{{`   2. Create and recreate element on sections OnClose and OnOpen.`}}"
				+ "{{`          If button OnClose/OnOpen is red/green, this means, that section OnClose/OnOpen is empty/not empty. `}}"
				+ "{{`          When you press OnClose/OnOpen button, this button redraw to green and element on section OnClose/OnOpen wil`}}"
				+ "{{`		   replaced to element with control Wait, ref to self, addition WaitToDisappear/WaitToAppear and default timeout to 5000ms.`}}"
				+ "{{`   3. Table with columns and rows.`}}"
				+ "{{`        Column # - serial number of a element.`}}"
				+ "{{`        Column Id - id of a element. You can change with field after double clicking.`}}"
				+ "{{`        Column Kind - control of a element. You can change this field after double clicking.`}}"
				+ "{{`        Xpath - this column shows you,that element is described via xpath or other fields. `}}"
				+ "{{`        New - this column shows you, that element is new or not.`}}"
				+ "{{`        Count - this column shows you the number of items found.`}}"
				+ "{{`        Options - column with button.`}}"
				+ "{{`            The first button - edit. You can change element on popup window.`}}"
				+ "{{`            The second button - update relation ( see below).`}}"
				+ "{{`            The third button - remove this element from dictionary.`}}"
				+ "{{` `}}"
				+ "{{`Tree pane ( bottom pane) possibilities`}}"
				+ "{{`    1. Magic button - see below.`}}"
				+ "{{`    2. NextMark/PreviousMark - jump on the marks."
				+ "{{`	  3. Checkboxes with states and count - If the checkbox is selected, rectangles ( for current state) on image will displaying`}}"
				+ "{{`Otherwise rectangles will hiding. Number of these checkboxes shows, how many elements with state are presented.`}}"
				+ "{{`    4. Find panel - like on xpath builder.`}}"
				+ "{{` `}}"
				+ "{{`Available states :`}}"
				+ "{{`    1. Mark`}}"
				+ "{{`    2. Question`}}"
				+ "{{`    3. Add`}}"
				+ "{{`    4. Update`}}"
				+ "{{`    `}}"
				+ "{{`State can be switched by clicking on left column in the same line or pressing key SPACE on the line`}}"
				+ "{{`State Mark means, that element found successful and count of items found - 1.`}}"
				+ "{{`State Question means, that element not found, but we try to find element via some information ( via sophistic algorithm), that was grabbed from last found. This info save to dictionary on tag info.`}}"
				+ "{{`For founding we evaluate some functions with weight. These weight you may set on SettingsPanel -> Wizard tab.`}}"
				+ "{{`State Add means, that on next push Magic button, we found this element, fill fields ( text, uid and etc) or xpath.`}}"
				+ "{{`State Update means, that we just update element.`}}"
				+ "{{` `}}"
				+ "{{`For adding new element, just click on left column in tree pane, state will changed to Add.`}}"
				+ "{{`If component has states Mark or Question, after pressing on this icon, state will changing to update. If you click again, state set to empty and relation for element in table will loose."
)
public class DictionaryWizard extends AbstractWizard
{
	private AppConnection currentConnection = null;
	private DictionaryFx  currentDictionary = null;
	private Window        currentWindow     = null;
	private Window        copyWindow        = null;

	private WizardMatcher matcher			= null;

	private volatile Document document    = null;
	private volatile Node     currentNode = null;
	private Rectangle dialogRectangle;
	private WizardSettings wizardSettings = null;
	private PluginInfo pluginInfo = null;

	private Node rootNode = null;

	private SplitPane                   centralSplitPane   = null;
	private ImageViewWithScale          imageViewWithScale = null;
	private XmlTreeView                 xmlTreeView        = null;
	private FindPanel<org.w3c.dom.Node> findPanel          = null;

	private TextField                    tfDialogName;
	private Label                        lblSelfId;
	private javafx.scene.control.Button  btnGenerateOnOpen;
	private javafx.scene.control.Button  btnGenerateOnClose;
	private ElementsTable				  tableView;
	private Button btnWizard;
	private Button btnNextMark;
	private Button btnPrevMark;
	private CheckBox cbUpdate;
	private CheckBox cbAdd;
	private CheckBox cbMark;
	private CheckBox cbQuestion;

	private WizardLoader wizardHelper = null;
	

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
			//AB. I have no idea, that we need do on a exception.
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

		Label lblDialog = new Label(R.WIZARD_LABEL_DIALOG.get());
		gridPane.add(lblDialog, 0, 0);
		GridPane.setHalignment(lblDialog, HPos.RIGHT);

		this.tfDialogName = new TextField();
		this.tfDialogName.setText(this.copyWindow.getName());
		gridPane.add(this.tfDialogName, 1, 0);

		Label lblSelf = new Label(R.WIZARD_LABEL_SELFID.get());
		gridPane.add(lblSelf, 2, 0);
		GridPane.setHalignment(lblSelf, HPos.RIGHT);

		this.lblSelfId = new Label();
		Common.tryCatch(() -> this.lblSelfId.setText(this.copyWindow.getSelfControl().getID()), R.WIZARD_ERROR_ON_SET_SELF_ID.get());
		gridPane.add(this.lblSelfId, 3, 0);
		GridPane.setHalignment(this.lblSelfId, HPos.LEFT);
		this.lblSelfId.getStyleClass().add(CssVariables.BOLD_LABEL);

		HBox hBox = new HBox();
		gridPane.add(hBox, 4, 0);
		GridPane.setHalignment(hBox, HPos.CENTER);
		GridPane.setValignment(hBox, VPos.CENTER);
		hBox.setAlignment(Pos.CENTER_RIGHT);

		this.btnGenerateOnOpen = new Button(R.WIZARD_LABEL_ON_OPEN.get());
		this.btnGenerateOnClose = new Button(R.WIZARD_LABEL_ON_CLOSE.get());
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
		addElementsIntoTable();
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		ISection section = this.copyWindow.getSection(IWindow.SectionKind.Run);
		section.getControls().forEach(((Section) section)::removeControl);
		this.tableView.getControls().forEach(section::addControl);

		return () -> CommandBuilder.start()
				.replaceWindow(this.currentDictionary, this.currentWindow, this.copyWindow)
				.displayWindow(this.currentDictionary, this.copyWindow)
				.build();
	}

	@Override
	protected void onRefused()
	{
		super.onRefused();
		Optional.ofNullable(this.wizardHelper).ifPresent(WizardLoader::stop);
	}

	@Override
	public boolean beforeRun()
	{
		try
		{
			IControl self = this.currentWindow.getSelfControl();
			if (self == null)
			{
				DialogsHelper.showError(R.WIZARD_SELF_ID_NOT_FOUND.get());
				return false;
			}
			if (self.getID().isEmpty())
			{
				DialogsHelper.showError(R.WIZARD_SELF_CONTROL_IS_EMPTY.get());
				return false;
			}

			if (this.currentConnection == null || !this.currentConnection.isGood())
			{
				DialogsHelper.showError(R.WIZARD_APPLICATION_NOT_STARTED.get());
				return false;
			}


			this.wizardHelper = new WizardLoader(this.currentConnection, self, (image, doc) ->
			{
				this.imageViewWithScale.displayImage(image);
				this.dialogRectangle = Common.tryCatch(() -> this.currentConnection.getApplication().service().getRectangle(null, Optional.ofNullable(this.copyWindow.getSelfControl()).map(IControl::locator).orElse(null)),
						R.WIZARD_DICTIONARY_EXCEPTION.get(), new Rectangle(0, 0, 0, 0));
				this.document = doc;
				this.rootNode = XpathUtils.getFirst(this.document, "/*");
				this.xmlTreeView.displayDocument(this.document);
				List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
				this.imageViewWithScale.setListForSearch(list);
				Common.runLater(() -> findElements(false));
			},
			ex ->
			{
				String message = ex.getMessage();
				if (ex.getCause() instanceof JFRemoteException)
				{
					JFRemoteException cause = (JFRemoteException) ex.getCause();
					message = cause.getErrorKind().toString() + ": " + cause.getMessage();
				}
				DialogsHelper.showError(message);
			});
			this.wizardHelper.start();

			this.wizardSettings = new WizardSettings(this.currentDictionary.getFactory().getSettings());
			this.pluginInfo = this.currentConnection.getApplication().getFactory().getInfo();
			this.matcher = new WizardMatcher(this.pluginInfo);
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
				this.imageViewWithScale.hideRectangle(oldItem.getRectangle(), oldMarker.color());
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
				this.imageViewWithScale.hideRectangle(item.getRectangle(), oldMarker == null ? null : oldMarker.color());
				this.imageViewWithScale.hideRectangle(item.getRectangle(), oldMarker);
				this.imageViewWithScale.showRectangle(item.getRectangle(), newMarker, item.getText(), selected);
				this.tableView.updateStyle(item.getNode(), newMarker == null ? null : newMarker.getCssStyle());
				if (newMarker == MarkerStyle.ADD)
				{
					this.tableView.clearRelation(item.getNode());
				}
				this.updateMarkers(oldMarker, newMarker);
			}
		});

		this.btnNextMark.setOnAction(e -> this.xmlTreeView.selectNextMark());
		this.btnPrevMark.setOnAction(e -> this.xmlTreeView.selectPrevMark());

		this.btnGenerateOnOpen.setOnAction(e -> Common.tryCatch(() -> {
			AbstractControl onOpen = generate(Addition.WaitToAppear);
			Section section = (Section) this.copyWindow.getSection(IWindow.SectionKind.OnOpen);
			section.clearSection();
			section.addControl(onOpen);
			updateOnButtons();
		}, R.WIZARD_ERROR_ON_GENERATE_ON_OPEN.get()));
		this.btnGenerateOnClose.setOnAction(e ->Common.tryCatch(() -> {
			AbstractControl onOpen = generate(Addition.WaitToDisappear);
			Section section = (Section) this.copyWindow.getSection(IWindow.SectionKind.OnClose);
			section.clearSection();
			section.addControl(onOpen);
			updateOnButtons();
		}, R.WIZARD_ERROR_ON_GENERATE_ON_CLOSE.get()));

		this.tfDialogName.textProperty().addListener((observable, oldValue, newValue) -> this.copyWindow.setName(newValue));

		this.cbAdd.selectedProperty().addListener((observable, oldValue, newValue) -> this.changeCheckBox(MarkerStyle.ADD, newValue));
		this.cbUpdate.selectedProperty().addListener((observable, oldValue, newValue) -> this.changeCheckBox(MarkerStyle.UPDATE, newValue));
		this.cbMark.selectedProperty().addListener((observable, oldValue, newValue) -> this.changeCheckBox(MarkerStyle.MARK, newValue));
		this.cbQuestion.selectedProperty().addListener((observable, oldValue, newValue) -> this.changeCheckBox(MarkerStyle.QUESTION, newValue));

		this.btnWizard.setOnAction(event -> this.magic());

		this.tableView.remove((ac, node) -> {
			this.clearRelation(node);
		});

		this.tableView.update((ac,node) -> {
			this.xmlTreeView.setMarker(node, MarkerStyle.UPDATE);
		});

		this.tableView.edit((ac,node) -> {
			this.clearRelation(node);
			this.findElement(ac, true);
		});
	}

	private void updateOnButtons()
	{
		boolean onOpenEmpty = this.copyWindow.getSection(IWindow.SectionKind.OnOpen).getControls().isEmpty();
		boolean onCloseEmpty = this.copyWindow.getSection(IWindow.SectionKind.OnClose).getControls().isEmpty();

		this.btnGenerateOnOpen.setStyle("-fx-background-color : " + (!onOpenEmpty ? "rgba(0,255,0, 0.1)" : "rgba(255,0,0, 0.1)"));
		this.btnGenerateOnClose.setStyle("-fx-background-color : " + (!onCloseEmpty ? "rgba(0,255,0, 0.1)" : "rgba(255,0,0, 0.1)"));
	}

	private void updateMarkers(MarkerStyle oldValue, MarkerStyle newValue)
	{
		CheckBox cbOld = checkBoxByMarkedStyle(oldValue);
		if (cbOld != null && oldValue != newValue)
		{
			cbOld.setText(String.valueOf(Math.max(Integer.parseInt(cbOld.getText()) - 1, 0)));
		}

		CheckBox cbNew = checkBoxByMarkedStyle(newValue);
		if (cbNew != null)
		{
			cbNew.setText(String.valueOf(Integer.parseInt(cbNew.getText()) + 1));
		}
	}

	private CheckBox checkBoxByMarkedStyle(MarkerStyle style)
	{
		if (style == null)
		{
			return null;
		}
		switch (style)
		{
			case UPDATE: return this.cbUpdate;
			case ADD: return this.cbAdd;
			case MARK: return this.cbMark;
			case QUESTION: return this.cbQuestion;
		}
		return null;
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

	private void addElementsIntoTable()
	{
		Collection<IControl> controls = this.copyWindow.getControls(IWindow.SectionKind.Run);
		this.tableView.getItems().setAll(
				controls.stream()
						.map(c -> (AbstractControl)c)
						.map(TableBean::new)
						.collect(Collectors.toList())
		);
	}

	private void findElements(boolean isNew)
	{
		this.tableView.getControls().forEach(ac -> this.findElement(ac, isNew));
	}

	private void findElement(AbstractControl control, boolean isNew)
	{
		int count = 0;
		Node found = null;
		if (control.getAddition() == Addition.Many || Str.IsNullOrEmpty(control.getOwnerID()))
		{
			this.updateElement(control, null, 0, CssVariables.COLOR_NOT_FINDING, isNew);
		}
		else
		{
			Locator locator = control.locator();
			List<Node> nodeList;
			try
			{
				String nodeName = null;
				try
				{
					IExtraInfo info = control.getInfo();

					if (info instanceof ExtraInfo)
					{
						String s = (String) ((ExtraInfo) info).get(ExtraInfo.nodeName);
						if (!Str.IsNullOrEmpty(s))
						{
							nodeName = s;
						}
					}
				}
				catch (Exception e)
				{
					//nothing to do
				}

				nodeList = this.matcher.findAll(this.rootNode, locator, nodeName);
				count = nodeList.size();
				found = count > 0 ? nodeList.get(0) : null;
			}
			catch (Exception e)
			{
				//noting to do
			}
			MarkerStyle style;
			if (count == 1)
			{
				style = MarkerStyle.MARK;
			}
			else
			{
				found = findBestIndex(control);
				style = MarkerStyle.QUESTION;
			}

			this.updateElement(control, found, count, style.getCssStyle(), isNew);
			this.xmlTreeView.setMarker(found, style);
		}
	}

	private void updateElement(AbstractControl control, Node node, int count, String style, boolean isNew)
	{
		this.tableView.updateElement(control, node, count, style, isNew);
	}

	//region sophisticated functions
	private Node findBestIndex(AbstractControl control)
	{
		if (control == null)
		{
			return null;
		}
		ControlKind kind = control.getBindedClass();
		ExtraInfo info = (ExtraInfo) control.getInfo();

		Map<Double, Node> candidates = new HashMap<>();

		XpathUtils.passTree(this.rootNode, node -> candidates.put(similarityFactor(node, kind, info), node));
		Double maxKey = candidates.keySet().stream().max(Double::compare).orElse(Double.MIN_VALUE);
		return maxKey > this.wizardSettings.getThreshold() ? candidates.get(maxKey) : null;
	}

	private double similarityFactor(Node node, ControlKind kind, ExtraInfo info)
	{
		if (node == null || this.dialogRectangle == null || kind == null || info == null)
		{
			return 0.0;
		}

		try
		{
			Rect actualRectangle     		= relativeRect(this.dialogRectangle, (Rectangle)node.getUserData(IRemoteApplication.rectangleName));
			String      actualName          = node.getNodeName();
			String      actualPath          = XpathUtils.fullXpath("", this.rootNode, node, false, null, true, this.pluginInfo::isStable);
			List<Attr>  actualAttr          = XpathUtils.extractAttributes(node);

			Rect        expectedRectangle   = (Rect)info.get(ExtraInfo.rectangleName);
			String      expectedName        = (String) info.get(ExtraInfo.nodeName);
			String      expectedPath        = (String) info.get(ExtraInfo.xpathName);
			List<Attr>  expectedAttr        = (List<Attr>) info.get(ExtraInfo.attrName);

			double sum = 0.0;
			// name
			sum += normalize(Str.areEqual(actualName, expectedName) ? 1.0 : 0.0, WizardSettings.Kind.TYPE);

			// position
			Point2D actualPos   = actualRectangle.center();
			Point2D expectedPos = expectedRectangle.center();
			double distance =  Math.sqrt( Math.pow(actualPos.getX() - expectedPos.getX(), 2) + Math.pow(actualPos.getY() - expectedPos.getY(), 2));
			sum += normalize(1 / (1 + Math.abs(distance)), WizardSettings.Kind.POSITION);

			// size
			double different = actualRectangle.square() - expectedRectangle.square();
			sum += normalize(1 / (1 + Math.abs(different)), WizardSettings.Kind.SIZE);

			// path
			String[] actualPathDim = Str.asString(actualPath).split("/");
			String[] expectedPathDim = Str.asString(expectedPath).split("/");
			int count = 0;
			for (int i = expectedPathDim.length - 1; i >= 0; i--)
			{
				int ind = actualPathDim.length - expectedPathDim.length + i;
				if (ind < 0)
				{
					break;
				}
				count += (Str.areEqual(expectedPathDim[i], actualPathDim[ind]) ? 1 : 0);
			}
			sum += normalize(count / expectedPathDim.length, WizardSettings.Kind.PATH);

			// attributes
			double attrFactor = 0.0;
			if (actualAttr != null && expectedAttr != null && expectedAttr.size() > 0)
			{
				Set<Attr> s1 = new HashSet<>(actualAttr);
				Set<Attr> s2 = new HashSet<>(expectedAttr);
				s2.retainAll(s1);

				attrFactor = (double)s2.size() / (double)expectedAttr.size();
			}
			sum += normalize(attrFactor, WizardSettings.Kind.PATH);

			sum *= wizardSettings.scale();

			return sum;
		}
		catch (Exception e)
		{
			return 0.0;
		}
	}

	private Rect relativeRect(Rectangle relative, Rectangle rect)
	{
		if (relative == null || rect == null)
		{
			return new Rect();
		}

		if (relative.height == 0 || relative.width == 0)
		{
			return new Rect();
		}

		double scaleX = 1 / relative.getWidth();
		double scaleY = 1 / relative.getHeight();

		return new Rect(rect.getX() * scaleX, rect.getY() * scaleY, (rect.getX() + rect.getWidth()) * scaleX, (rect.getY() + rect.getHeight()) * scaleY);
	}

	private double normalize(double value, WizardSettings.Kind kind)
	{
		double min = this.wizardSettings.getMin(kind);
		double max = this.wizardSettings.getMax(kind);

		return min + (max - min)*value;
	}
	//endregion

	private void arrangeOne(Node node, AbstractControl control, MarkerStyle style) throws Exception
	{
		switch (style)
		{
			case ADD:
				Locator locator = compile(composeId(node), composeKind(node), node);
				if (locator != null)
				{
					AbstractControl copyControl = AbstractControl.create(locator, this.copyWindow.getSelfControl().getID());
					updateExtraInfo(node, copyControl);
					TableBean e = new TableBean(copyControl);
					e.setStyle(CssVariables.FOUND_ONE_ELEMENT);
					e.setCount(1);
					e.setId(copyControl.getID());
					e.setNode(node);
					this.tableView.getItems().add(e);
				}
				break;

			case UPDATE:
				Locator locatorUpdate = compile(control.getID(), control.getBindedClass(), node);
				if (locatorUpdate != null)
				{
					AbstractControl copyControl = AbstractControl.create(locatorUpdate, this.copyWindow.getSelfControl().getID());
					updateExtraInfo(node, copyControl);
					this.tableView.updateControl(node, copyControl);
				}
				break;
			case MARK:
				updateExtraInfo(node, control);
				break;

			case QUESTION:
				break;
		}
	}

	private void magic()
	{
		ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
		int sum = this.xmlTreeView.getMarkedRowCount();
		if (sum == 0)
		{
			DialogsHelper.showInfo(R.WIZARD_NOTHING_TO_UPDATE_INFO.get());
			return;
		}
		Dialog<String> dialog = new Dialog<>();
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setWidth(400.0);

		BorderPane borderPane = new BorderPane();
		borderPane.setPrefWidth(400.0);
		Label lblInfo = new Label();
		ProgressBar progressBar = new ProgressBar();
		progressBar.setMaxWidth(Double.MAX_VALUE);
		progressBar.setProgress(0);

		Button btnStop = new Button(R.WIZARD_STOP_APPLICATION.get());
		btnStop.setOnAction(e -> {
			dialog.setResult("");
			taskExecutor.shutdownNow();
			dialog.close();
		});

		borderPane.setTop(lblInfo);
		borderPane.setCenter(progressBar);
		borderPane.setBottom(btnStop);
		BorderPane.setAlignment(btnStop, Pos.CENTER_RIGHT);
		BorderPane.setMargin(btnStop, new Insets(8, 0, 0, 0));

		dialog.getDialogPane().setContent(borderPane);
		dialog.getDialogPane().setHeader(new Label());
		dialog.setTitle(R.WIZARD_UPDATING_ELEMENTS.get());
		dialog.show();

		Service<Void> service = new Service<Void>()
		{
			@Override
			protected Task<Void> createTask()
			{
				return new Task<Void>()
				{
					@Override
					protected Void call() throws Exception
					{
						clearCheckboxes();
						final int[] count = {0};

						for (javafx.util.Pair<Node, MarkerStyle> pair : xmlTreeView.getMarkedItems())
						{
							Node node = pair.getKey();
							MarkerStyle style = pair.getValue();
							AbstractControl control = tableView.controlByNode(node);

							if (control != null && (control.getAddition() == Addition.Many || Str.IsNullOrEmpty(control.getOwnerID())))
							{
								updateElement(control, null, 0, CssVariables.COLOR_NOT_FINDING, false);
								continue;
							}

							Thread.sleep(200);
							Common.runLater(() -> lblInfo.setText(MessageFormat.format(R.WIZARD_START_UPDATING_ITEMS.get(), ++count[0], sum)));
							Common.tryCatch(() -> arrangeOne(node, control, style), R.WIZARD_ERROR_ON_ARRANGE_ONE.get());
							Common.runLater(() -> {
								lblInfo.setText(MessageFormat.format(R.WIZARD_END_UPDATING.get(), count[0], sum));
								progressBar.setProgress((double) count[0] / sum);
							});
						}
						Common.runLater(() -> xmlTreeView.refresh());
						return null;
					}
				};
			}
		};
		service.setExecutor(taskExecutor);
		service.setOnSucceeded(e -> {
			Common.tryCatch(() -> Thread.sleep(200), "");
			Common.tryCatch(() -> findElements(true), R.WIZARD_ERROR_ON_FIND.get());
			dialog.setResult("");
			dialog.close();
		});
		service.start();
	}

	//region createLocator
	private void updateExtraInfo(Node node, AbstractControl control) throws Exception
	{
		ExtraInfo info = new ExtraInfo();
		Rectangle rec = (Rectangle)node.getUserData(IRemoteApplication.rectangleName);
		Rect rectangle = relativeRect(this.dialogRectangle, rec);

		info.set(ExtraInfo.xpathName,       XpathUtils.fullXpath("", this.rootNode, node, false, null, true, this.pluginInfo::isStable));
		info.set(ExtraInfo.nodeName,        node.getNodeName());
		info.set(ExtraInfo.rectangleName,   rectangle);
		List<Attr> attributes = XpathUtils.extractAttributes(node);
		if (!attributes.isEmpty())
		{
			info.set(ExtraInfo.attrName, attributes);
		}
		control.set(AbstractControl.infoName, info);
	}

	private ControlKind composeKind(Node node)
	{
		return this.pluginInfo.controlKindByNode(node);
	}

	private String composeId(Node node)
	{
		String res = null;
		if (node.hasAttributes())
		{
			res = composeFromAttr(res, node, LocatorFieldKind.UID);
			res = composeFromAttr(res, node, LocatorFieldKind.NAME);
			res = composeFromAttr(res, node, LocatorFieldKind.TITLE);
			res = composeFromAttr(res, node, LocatorFieldKind.ACTION);
		}
		res = composeFromText(res, node);
		if (res == null)
		{
			return null;
		}
		String trimmed = res.trim();
		String capitalizeAllLetters = Arrays.stream(trimmed.split(" "))
				//capitalize first letters
				.map(part -> part.substring(0,1).toUpperCase() + part.substring(1))
				.collect(Collectors.joining(""));

		return capitalizeAllLetters.substring(0, 1).toLowerCase() + capitalizeAllLetters.substring(1);
	}

	private String composeFromAttr(String res, Node node, LocatorFieldKind kind)
	{
		if (res != null)
		{
			return res;
		}
		String attrName = this.pluginInfo.attributeName(kind);
		if (attrName == null)
		{
			return null;
		}

		if (node.hasAttributes())
		{
			Node attrNode = node.getAttributes().getNamedItem(attrName);
			if (attrNode == null)
			{
				return null;
			}

			String attr = attrNode.getNodeValue();
			if (XpathUtils.isStable(attr, this.pluginInfo::isStable))
			{
				return attr;
			}
		}
		return null;
	}

	private String composeFromText(String res, Node node)
	{
		if (res != null)
		{
			return res;
		}

		String text = XpathUtils.text(node);//node.getTextContent();
		if (XpathUtils.isStable(text, this.pluginInfo::isStable))
		{
			return text;
		}
		return null;
	}

	private Locator compile(String id, ControlKind kind, Node node)
	{
		Locator locator = XpathUtils.FindLocator
				.start(this::tryLocator, id, kind, node, this.pluginInfo)
				.findById()
				.findByAttrs()
				.findByXpath(this.rootNode)
				.build();
		if (locator != null)
		{
			return locator;
		}
		return null; // can't compile the such locator
	}

	private int tryLocator(Locator locator, Node node)
	{
		if (locator == null)
		{
			return 0;
		}

		try
		{
			List<Node> list = findAll(locator, node.getNodeName());
			if (list.size() != 1)
			{
				return list.size();
			}

			if (list.get(0) == node)
			{
				return 1;
			}
		}
		catch (Exception e)
		{
			// nothing to do
		}
		return 0;
	}

	private List<Node> findAll(Locator locator, String nodeName) throws Exception
	{
		return this.matcher.findAll(this.rootNode, locator, nodeName);
	}
	//endregion

	private void clearCheckboxes()
	{
		Common.runLater(() -> {
			this.cbQuestion.setText("0");
			this.cbMark.setText("0");
			this.cbUpdate.setText("0");
			this.cbAdd.setText("0");
		});
	}

	private void clearRelation(Node node)
	{
		this.xmlTreeView.setMarker(node, null);
		this.xmlTreeView.refresh();
	}

	private void changeCheckBox(MarkerStyle style, boolean newValue)
	{
		this.xmlTreeView.setMarkersVisible(style, newValue);
		this.imageViewWithScale.visibleRectangle(style, newValue);
	}
	//endregion
}
