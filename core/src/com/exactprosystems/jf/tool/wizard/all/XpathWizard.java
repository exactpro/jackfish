/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.xmltree.XmlTreeView;
import com.exactprosystems.jf.tool.documents.guidic.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.WizardMatcher;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.WizardLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.exactprosystems.jf.common.utils.XpathUtils.*;

@WizardAttribute(
        name 				= R.XPATH_WIZARD_NAME,
        pictureName 		= "XpathWizard.png",
        category 			= WizardCategory.GUI_DICTIONARY,
        shortDescription 	= R.XPATH_WIZARD_SHORT_DESCRIPTION,
        experimental 		= false,
        strongCriteries 	= true,
        criteries 			= { DictionaryFx.class, Window.class, SectionKind.class, AbstractControl.class },
		detailedDescription = R.XPATH_WIZARD_DETAILED_DESCRIPTION
)
public class XpathWizard extends AbstractWizard
{
	private static class OneLine
	{
		public OneLine(int count)
		{
			this.btnXpath = new Button("");
			this.btnXpath.setAlignment(Pos.BASELINE_LEFT);
			this.btnXpath.setMaxWidth(Double.MAX_VALUE);
			BorderPane.setAlignment(this.btnXpath, Pos.CENTER_LEFT);
			this.btnCopyToRelative = new Button(R.XPATH_WIZARD_COPY_TO_RELATIVE.get());
			this.labelXpathCount = new Label("0");
			this.labelXpathCount.setMaxWidth(30.0);
			this.labelXpathCount.setPrefWidth(30.0);

			this.borderPane = new BorderPane();
			this.borderPane.setCenter(this.btnXpath);

			HBox box = new HBox();
			box.setAlignment(Pos.CENTER);
			box.setMaxWidth(86.0);
			box.setPrefWidth(86.0);
			box.setPrefHeight(29.0);
			BorderPane.setAlignment(box, Pos.BOTTOM_LEFT);
			box.getChildren().addAll(
					  Common.createSpacer(Common.SpacerEnum.HorizontalMin)
					, this.btnCopyToRelative
					, Common.createSpacer(Common.SpacerEnum.HorizontalMin)
					, this.labelXpathCount
			);
			borderPane.setRight(box);
		}

		javafx.scene.Node getNode()
		{
			return this.borderPane;
		}

		public  Label      labelXpathCount;
		public  Button     btnXpath;
		public  Button     btnCopyToRelative;
		private BorderPane borderPane;
	}

	private static class OneMagicLine extends OneLine
	{
		public OneMagicLine(int count)
		{
			super(count);
			this.btnCopyToRelative.setText("");
			this.btnCopyToRelative.setId("btnWizard");
		}
	}

	private AbstractEvaluator evaluator;
	private AppConnection   currentConnection = null;
	private WizardMatcher	wizardMatcher	  = null;
	private PluginInfo		pluginInfo		  = null;
	private DictionaryFx	currentDictionary = null;
	private Window          currentWindow     = null;
	private Section         currentSection    = null;
	private AbstractControl currentControl    = null;
	private IControl		ownerControl	  = null;

	private WizardLoader wizardHelper = null;

	private volatile Document document    = null;
	private volatile Node     currentNode = null;

	private ImageViewWithScale          imageViewWithScale;
	private XmlTreeView                 xmlTreeView;
	private FindPanel<org.w3c.dom.Node> findPanel;
	private OneLine[]                   lines;
	private CheckBox                    useText;
	private HBox                        hBoxCheckboxes;
	private CustomFieldWithButton       cfRelativeFrom;
	private CustomFieldWithButton       cfMainExpression;
	private Label                       lblFound;

	public XpathWizard()
	{
	}

	//region AbstractWizard methods
	@Override
	public void init(Context context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);

		this.currentConnection = super.get(AppConnection.class, parameters);
		this.currentDictionary = super.get(DictionaryFx.class, parameters);
		this.currentWindow = super.get(Window.class, parameters);
		SectionKind kind = super.get(SectionKind.class, parameters);
		if (this.currentWindow != null && kind != null)
		{
			this.currentSection = (Section) this.currentWindow.getSection(kind);
		}
		this.currentControl = super.get(AbstractControl.class, parameters);
		this.ownerControl = this.currentWindow.getControlForName(null, this.currentControl.getOwnerID());
		if (this.ownerControl == null)
		{
			this.ownerControl = this.currentWindow.getSelfControl();
		}
		this.evaluator = context.getEvaluator();
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		this.pluginInfo = this.currentConnection.getApplication().getFactory().getInfo();
		this.wizardMatcher = new WizardMatcher(this.pluginInfo);

		borderPane.setPrefHeight(1000.0);
		borderPane.setPrefWidth(1000.0);

		//region center
		SplitPane splitPane = new SplitPane();
		splitPane.setDividerPositions(0.0);
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.setPrefSize(160.0, 200.0);
		BorderPane.setAlignment(splitPane, Pos.CENTER);

		GridPane gridPane = new GridPane();
		ColumnConstraints c0 = new ColumnConstraints();
		c0.setHgrow(Priority.SOMETIMES);
		c0.setMinWidth(10.0);

		gridPane.getColumnConstraints().addAll(c0);

		RowConstraints r0 = new RowConstraints();
		r0.setVgrow(Priority.SOMETIMES);
		r0.setMinHeight(10.0);

		RowConstraints r1 = new RowConstraints();
		r1.setVgrow(Priority.SOMETIMES);
		r1.setMinHeight(30.0);
		r1.setMaxHeight(30.0);
		r1.setPrefHeight(30.0);

		gridPane.getRowConstraints().addAll(r0, r1);

		this.findPanel = new FindPanel<>();
		this.findPanel.getStyleClass().remove(CssVariables.FIND_PANEL);
		gridPane.add(this.findPanel, 0, 1);

		this.xmlTreeView = new XmlTreeView();

		this.xmlTreeView.setMarkersVisible(false);

		this.imageViewWithScale = new ImageViewWithScale();
		this.imageViewWithScale.setOnRectangleClick(rectangle -> this.xmlTreeView.selectItem(rectangle));

		gridPane.add(this.xmlTreeView, 0, 0);
		splitPane.getItems().addAll(this.imageViewWithScale, gridPane);

		//endregion

		//region bottom
		BorderPane resultPane = new BorderPane();

		this.cfMainExpression = new CustomFieldWithButton();
		this.cfMainExpression.setPromptText(R.WIZARD_ENTER_XPATH.get());
		try
		{
			Object obj = this.currentControl.get(AbstractControl.xpathName);
			if (obj != null)
			{
				this.cfMainExpression.setText("" + obj);
			}
		}
		catch (Exception e)
		{
			//nothing
		}
		this.lblFound = new Label("0");
		this.lblFound.setPrefWidth(30);
		BorderPane.setAlignment(this.lblFound, Pos.CENTER);
		BorderPane.setAlignment(resultPane, Pos.CENTER);
		resultPane.setCenter(this.cfMainExpression);
		resultPane.setRight(this.lblFound);

		Accordion accordion = new Accordion();
		accordion.setRotate(180.0);
		BorderPane.setAlignment(accordion, Pos.CENTER);

		TitledPane titledPane = new TitledPane();
		titledPane.setAnimated(false);
		titledPane.setContentDisplay(ContentDisplay.CENTER);
		titledPane.setExpanded(false);
		titledPane.setRotate(180.0);
		titledPane.setText(R.WIZARD_HELPER.get());

		GridPane gp = new GridPane();

		ColumnConstraints cc0 = new ColumnConstraints();
		cc0.setHgrow(Priority.SOMETIMES);
		cc0.setPrefWidth(100.0);

		gp.getColumnConstraints().addAll(cc0, new ColumnConstraints(), new ColumnConstraints());

		Supplier<RowConstraints> supplier = () ->
		{
			RowConstraints r = new RowConstraints();
			r.setMinHeight(10.0);
			r.setPrefHeight(30.0);
			r.setVgrow(Priority.SOMETIMES);
			return r;
		};

		IntStream.range(0, 7).forEach(i -> gp.getRowConstraints().addAll(supplier.get()));

		BorderPane relativePane = new BorderPane();
		GridPane.setColumnSpan(relativePane, 3);
		Label lblRelative = new Label(R.WIZARD_RELATIVE.get());
		BorderPane.setAlignment(lblRelative, Pos.CENTER);
		this.cfRelativeFrom = new CustomFieldWithButton();
		relativePane.setCenter(this.cfRelativeFrom);
		relativePane.setLeft(lblRelative);
		gp.add(relativePane, 0, 0);

		BorderPane chekboxPane = new BorderPane();
		GridPane.setColumnSpan(chekboxPane, 3);
		this.useText = new CheckBox(R.WIZARD_USE_TEXT.get());
		BorderPane.setAlignment(this.useText, Pos.CENTER_LEFT);
		chekboxPane.setLeft(this.useText);

		this.hBoxCheckboxes = new HBox();
		this.hBoxCheckboxes.setSpacing(4.0);
		this.hBoxCheckboxes.setAlignment(Pos.CENTER_LEFT);
		BorderPane.setAlignment(this.hBoxCheckboxes, Pos.CENTER);
		chekboxPane.setCenter(this.hBoxCheckboxes);
		gp.add(chekboxPane, 0, 1);

		this.lines = new OneLine[5];
		for (int i = 0; i < this.lines.length - 1; i++)
		{
			this.lines[i] = new OneLine(i);
			gp.add(this.lines[i].getNode(), 0, 2 + i);
		}
		this.lines[4] = new OneMagicLine(4);
		gp.add(this.lines[4].getNode(), 0, 6);

		titledPane.setContent(gp);

		accordion.getPanes().add(titledPane);

		resultPane.setTop(accordion);
		//endregion

		borderPane.setBottom(resultPane);
		borderPane.setCenter(splitPane);

		initListeners();
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		//TODO remove unnecessary copy control
		AbstractControl copy = this.currentControl;
		try
		{
			copy = AbstractControl.createCopy(this.currentControl);
			copy.set(AbstractControl.xpathName, this.cfMainExpression.getText());
			this.currentControl.set(AbstractControl.xpathName, this.cfMainExpression.getText());
		}
		catch (Exception e)
		{
			DialogsHelper.showError(e.getMessage());
		}

		AbstractControl replace = copy;
		return () -> CommandBuilder.start()
//				.replaceControl(this.currentSection, this.currentControl, replace)
				.displayControl(this.currentDictionary, this.currentWindow, this.currentSection, this.currentControl)
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
			if (this.currentConnection == null || !this.currentConnection.isGood())
			{
				DialogsHelper.showError(R.WIZARD_APPLICATION_NOT_STARTED.get());
				return false;
			}

			IControl self = null;
			if (!this.currentSection.getSectionKind().equals(SectionKind.Self))
			{
				self = this.currentWindow.getSelfControl();
			}

			this.wizardHelper = new WizardLoader(this.currentConnection, self, this.evaluator, (image, doc) ->
			{
				this.imageViewWithScale.displayImage(image);

				this.document = doc;
				this.xmlTreeView.displayDocument(this.document);
				List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
				this.imageViewWithScale.setListForSearch(list);
				Common.runLater(() -> this.applyXpath(this.cfMainExpression.getText()));
			},
			ex ->
			{
				String message = ex.getMessage();
				if (ex.getCause() instanceof JFRemoteException)
				{
					message = ((JFRemoteException) ex.getCause()).getErrorKind().toString();
				}
				DialogsHelper.showError(message);
			});
			this.wizardHelper.start();
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

		Arrays.stream(this.lines)
				.forEach(oneLine -> {
					oneLine.btnCopyToRelative.setOnAction(ev -> this.cfRelativeFrom.setText(oneLine.btnXpath.getText()));
					if (oneLine instanceof OneMagicLine)
					{
						oneLine.btnCopyToRelative.setOnAction(e -> {
							Node root = this.document;
							if (this.ownerControl != null)
							{
								root = XpathUtils.getFirst(this.document, "/*");
							}
							String bestXpath = this.findBestXpath(this.currentNode, root);
							if (bestXpath != null)
							{
								oneLine.btnXpath.setText(bestXpath);
								Node rootNode = this.document;
								if (this.ownerControl != null)
								{
									rootNode = getFirst(this.document, "/*");
								}
								List<Node> evaluate = evaluate(rootNode, bestXpath);
								oneLine.labelXpathCount.setText(evaluate == null ? "" : "" + evaluate.size());
							}
						});
					}
					oneLine.btnXpath.setOnAction(ev -> this.cfMainExpression.setText(((Button) ev.getSource()).getText()));
					oneLine.labelXpathCount.textProperty().addListener((observable, oldValue, newValue) -> {
						Label lbl = oneLine.labelXpathCount;
						lbl.getStyleClass().remove(CssVariables.FOUND_ONE_ELEMENT);
						boolean foundOneElement = newValue.equals("1");
						if (foundOneElement && !lbl.getStyleClass().contains(CssVariables.FOUND_ONE_ELEMENT))
						{
							lbl.getStyleClass().add(CssVariables.FOUND_ONE_ELEMENT);
						}
					});
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
				updateNode(newItem.getNode());
			}
		});

		this.cfRelativeFrom.setHandler(e -> {
			this.cfRelativeFrom.setText(null);
			createXpaths(this.useText.isSelected(), getSelectedAttributes());
		});

		this.useText.selectedProperty().addListener((observable, oldValue, newValue) -> createXpaths(newValue, getSelectedAttributes()));

		this.cfMainExpression.textProperty().addListener((observable, oldValue, newValue) -> applyXpath(newValue));
	}

	private void applyXpath(String newValue)
	{
		this.cfMainExpression.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
		Node rootNode = this.document;
		if (this.ownerControl != null)
		{
			rootNode = getFirst(this.document, "/*");
		}
		displayNodes(evaluate(rootNode, newValue));
	}

	private void updateNode(Node node)
	{
		this.currentNode = node;
		this.displayParams(getAllNodeAttribute(node));
		this.createXpaths(this.useText.isSelected(), getSelectedAttributes());
	}

	private void displayParams(List<String> params)
	{
		this.hBoxCheckboxes.getChildren().clear();
		this.hBoxCheckboxes.getChildren().addAll(params.stream().map(p ->
		{
			CheckBox box = new CheckBox(p);
			box.setSelected(true);
			box.selectedProperty().addListener((observable, oldValue, newValue) -> this.createXpaths(this.useText.isSelected(), getSelectedAttributes()));
			return box;
		}).collect(Collectors.toList()));
	}

	private List<String> getSelectedAttributes()
	{
		return this.hBoxCheckboxes.getChildren()
				.stream()
				.filter(node -> ((CheckBox) node).isSelected())
				.map(node -> (((CheckBox) node).getText()))
				.collect(Collectors.toList());
	}

	private void createXpaths(boolean useText, List<String> parameters)
	{
		String relativePath = null;
		Node relativeNode = null;
		Node rootNode = this.document;
		if (this.ownerControl != null)
		{
			relativePath = ".";
			relativeNode = getFirst(this.document, "/*");
			rootNode = relativeNode;
		}
		String relativeXpath = this.cfRelativeFrom.getText();
		if (!Str.IsNullOrEmpty(relativeXpath))
		{
			relativePath = relativeXpath;
			relativeNode = getFirst(this.document, relativeXpath);
		}

		String xpath1 = fullXpath(relativePath, relativeNode, currentNode, false, null, true, null);
		String xpath2 = fullXpath(relativePath, relativeNode, currentNode, useText, parameters, true, null);
		String xpath3 = fullXpath(relativePath, relativeNode, currentNode, false, null, false, null);
		String xpath4 = fullXpath(relativePath, relativeNode, currentNode, useText, parameters, false, null);

		int i = 0;
		this.lines[i].btnXpath.setText(xpath1);
		List<Node> nodes1 = evaluate(rootNode, xpath1);
		this.lines[i++].labelXpathCount.setText(nodes1 == null ? "" : "" + nodes1.size());

		this.lines[i].btnXpath.setText(xpath2);
		List<Node> nodes2 = evaluate(rootNode, xpath2);
		this.lines[i++].labelXpathCount.setText(nodes2 == null ? "" : "" + nodes2.size());

		this.lines[i].btnXpath.setText(xpath3);
		List<Node> nodes3 = evaluate(rootNode, xpath3);
		this.lines[i++].labelXpathCount.setText(nodes3 == null ? "" : "" + nodes3.size());

		List<Node> nodes4 = evaluate(rootNode, xpath4);
		this.lines[i].btnXpath.setText(xpath4);
		this.lines[i++].labelXpathCount.setText(nodes4 == null ? "" : "" + nodes4.size());

		this.lines[i].btnXpath.setText("");
		this.lines[i++].labelXpathCount.setText("" + 0);

		if (this.currentNode != null)
		{
			Rectangle rectangle = (Rectangle) this.currentNode.getUserData(IRemoteApplication.rectangleName);
			this.imageViewWithScale.showRectangle(rectangle, MarkerStyle.SELECT, null, true);
		}
	}

	private void displayNodes(List<Node> nodes)
	{
		if (nodes == null)
		{
			this.cfMainExpression.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
			if (!Str.IsNullOrEmpty(this.cfMainExpression.getText()))
			{
				this.cfMainExpression.getStyleClass().add(CssVariables.INCORRECT_FIELD);
			}
			this.lblFound.setText("0");
		}
		else
		{
			this.xmlTreeView.highlightNodes(nodes);
			this.lblFound.setText("" + nodes.size());
		}
	}

	private String findBestXpath(Node node, Node owner)
	{
		Locator locator = FindLocator.start((l, n) -> this.tryLocator(l, n, owner), "", composeKind(node), node, this.pluginInfo).findByXpath(owner).build();
		if (locator != null)
		{
			return locator.getXpath();
		}
		return null;
	}

	private int tryLocator(Locator locator, Node node, Node owner)
	{
		if (locator == null)
		{
			return 0;
		}

		try
		{
			List<Node> list = findAll(locator, owner, node.getNodeName());
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

	private List<Node> findAll(Locator locator, Node owner, String nodeName) throws Exception
	{
		return this.wizardMatcher.findAll(owner, locator, nodeName);
	}

	private ControlKind composeKind(Node node)
	{
		return this.pluginInfo.controlKindByNode(node);
	}
	//endregion
}
