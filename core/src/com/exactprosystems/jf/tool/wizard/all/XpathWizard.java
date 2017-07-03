////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.utils.JfService;
import com.exactprosystems.jf.common.utils.XpathUtils;
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
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.ImageAndOffset;
import com.exactprosystems.jf.tool.wizard.related.XmlTreeItem;
import javafx.concurrent.Service;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@WizardAttribute(
        name 				= "Xpath wizard",
        pictureName 		= "DictionaryWizard.png",
        category 			= WizardCategory.GUI_DICTIONARY,
        shortDescription 	= "This wizard help to build xpath expression to find an element on screen.",
        detailedDescription = "Here you descrioption might be",
        experimental 		= true,
        strongCriteries 	= true,
        criteries 			= { DictionaryFx.class, Window.class, SectionKind.class, AbstractControl.class }
    )
public class XpathWizard extends AbstractWizard
{
    private static class OneLine
    {
        public OneLine(int count)
        {
            this.btnXpath          = new Button("");
			this.btnXpath.setMaxWidth(Double.MAX_VALUE);
			BorderPane.setAlignment(this.btnXpath, Pos.CENTER);
			this.btnCopyToRelative = new Button("Rel");
            this.labelXpathCount   = new Label("0");

            this.borderPane = new BorderPane();
			this.borderPane.setCenter(this.btnXpath);

			HBox box = new HBox();
			box.setAlignment(Pos.CENTER);
			box.setPrefWidth(76.0);
			box.setPrefHeight(29.0);
			BorderPane.setAlignment(box, Pos.BOTTOM_LEFT);
			box.getChildren().addAll(this.btnCopyToRelative, Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.labelXpathCount);
			borderPane.setRight(box);
        }

        javafx.scene.Node getNode()
		{
			return this.borderPane;
		}

		public Label  labelXpathCount;
        public Button btnXpath;
        public Button btnCopyToRelative;
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
    
    private AppConnection                      currentConnection = null;
    private Window                             currentWindow     = null;
    private Section                            currentSection    = null;
    private AbstractControl                    currentControl    = null;

    private ExecutorService                    executor          = null;
    private Service<ImageAndOffset>            imageService      = null;
    private Service<Document>                  documentService   = null;

    private volatile ImageAndOffset            imageAndOffset    = null;
    private volatile Document                  document          = null;
    private volatile Node                      currentNode       = null;

    private SplitPane                          splitPane;
    private ImageViewWithScale                 imageViewWithScale;
    private XmlTreeView                        xmlTreeView;
    private FindPanel<TreeItem<XmlTreeItem>>   findPanel;
    private OneLine[]                          lines;
    private CheckBox                           useText;
    private HBox                               hBoxCheckboxes;
    private CustomFieldWithButton              cfRelativeFrom;
    private CustomFieldWithButton              cfMainExpression;
    private Label                              lblFound;

    public XpathWizard()
    {
        this.executor = Executors.newFixedThreadPool(1);
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        
        this.currentConnection = super.get(AppConnection.class, parameters);
        this.currentWindow     = super.get(Window.class, parameters);
        SectionKind kind       = super.get(SectionKind.class, parameters);
        if (this.currentWindow != null && kind != null)
        {
            this.currentSection = (Section)this.currentWindow.getSection(kind);
        }
        this.currentControl    = super.get(AbstractControl.class, parameters);
    }

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		borderPane.setPrefHeight(1000.0);
		borderPane.setPrefWidth(1000.0);

		//region center
		this.splitPane = new SplitPane();
		this.splitPane.setOrientation(Orientation.VERTICAL);
		this.splitPane.setDividerPositions(0.0);
		this.splitPane.setOrientation(Orientation.VERTICAL);
		this.splitPane.setPrefSize(160.0, 200.0);
		BorderPane.setAlignment(this.splitPane, Pos.CENTER);

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

		this.xmlTreeView = new XmlTreeView(v->{}, v->{});
		this.xmlTreeView.setMarkersVisible(false);

		this.imageViewWithScale = new ImageViewWithScale();
		this.imageViewWithScale.setOnRectangleClick(rectangle -> this.xmlTreeView.selectItem(rectangle));

		gridPane.add(this.xmlTreeView, 0, 0);
		this.splitPane.getItems().addAll(this.imageViewWithScale, gridPane);


		//endregion

		//region bottom
		BorderPane resultPane = new BorderPane();

		this.cfMainExpression = new CustomFieldWithButton();
		this.cfMainExpression.setPromptText("Enter xpath here");

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
		titledPane.setText("Helper");

		GridPane gp = new GridPane();

		ColumnConstraints cc0 = new ColumnConstraints();
		cc0.setHgrow(Priority.SOMETIMES);
		cc0.setPrefWidth(100.0);

		gp.getColumnConstraints().addAll(cc0, new ColumnConstraints(), new ColumnConstraints());

		Supplier<RowConstraints> supplier = () -> {
			RowConstraints r = new RowConstraints();
			r.setMinHeight(10.0);
			r.setPrefHeight(30.0);
			r.setVgrow(Priority.SOMETIMES);
			return r;
		};

		IntStream.range(0, 7).forEach(i -> gp.getRowConstraints().addAll(supplier.get()));

		BorderPane relativePane = new BorderPane();
		GridPane.setColumnSpan(relativePane, 3);
		Label lblRelative = new Label("Relative");
		BorderPane.setAlignment(lblRelative, Pos.CENTER);
		this.cfRelativeFrom = new CustomFieldWithButton();
		relativePane.setCenter(this.cfRelativeFrom);
		relativePane.setLeft(lblRelative);
		gp.add(relativePane, 0, 0);

		BorderPane chekboxPane = new BorderPane();
		GridPane.setColumnSpan(chekboxPane, 3);
		this.useText = new CheckBox("use text()");
		BorderPane.setAlignment(this.useText, Pos.CENTER_LEFT);
		chekboxPane.setLeft(this.useText);

		this.hBoxCheckboxes = new HBox();
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
		borderPane.setCenter(this.splitPane);

		initListeners();
	}

	@Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        AbstractControl copy = this.currentControl;
        try
        {
            copy = AbstractControl.createCopy(this.currentControl);
            copy.set(AbstractControl.xpathName, this.cfMainExpression.getText());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        AbstractControl replace = copy;
        return () ->
        {
            List<WizardCommand> commands = CommandBuilder
                    .start()
                    .replaceControl(this.currentSection, this.currentControl, replace)
                    .build();
            
            return commands;
        };
    }
    
    @Override
    public boolean beforeRun()
    {
        try
        {
                
            if (this.currentConnection == null)
            {
                DialogsHelper.showError("Esteblish connection at first");
                return false;
            }
            
            IControl self = this.currentWindow.getSelfControl();
            Locator selfLocator = self == null ? null : self.locator();
            IRemoteApplication service = this.currentConnection.getApplication().service();
            
            // get picture
            this.imageService = new JfService<ImageAndOffset>(this.executor, 
                    () -> Common.tryCatch(() ->
                    {
                        Rectangle rectangle = service.getRectangle(null, selfLocator);
                        BufferedImage image = service.getImage(null, selfLocator).getImage();
                        return new ImageAndOffset(image, rectangle.x, rectangle.y);
                    }, "Error on getting image", null));
    
    
            this.imageService.setOnSucceeded(event ->
            {
                this.imageAndOffset = (ImageAndOffset) event.getSource().getValue();
                this.imageViewWithScale.displayImage(this.imageAndOffset.image);
            });
    
            this.imageService.setOnFailed(event ->
            {
                Throwable exception = event.getSource().getException();
                String message = exception.getMessage();
                if (exception.getCause() instanceof JFRemoteException)
                {
                    message = ((JFRemoteException) exception.getCause()).getErrorKind().toString();
                }
                DialogsHelper.showError(message);
            });
    
            // get XML document
            this.documentService = new JfService<Document>(this.executor, 
                    () -> Common.tryCatch(() -> 
                    { 
                        byte[] treeBytes = service.getTreeBytes(selfLocator);
                        return Converter.convertByteArrayToXmlDocument(treeBytes);
                    }, "Error on document getting", null));
            this.documentService.setOnSucceeded(event ->
            {
                this.document = (Document) event.getSource().getValue();
                this.currentNode = XpathUtils.getFirst(this.document, "/*");
                if (this.imageAndOffset != null)
                {
                    // TODO offsetts are applied here
                    XpathUtils.applyOffset(this.document, this.imageAndOffset.offsetX, this.imageAndOffset.offsetY);                    
                    this.xmlTreeView.displayDocument(this.document);
                    List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
                    this.imageViewWithScale.setListForSearch(list);
                }
            });
            this.documentService.setOnFailed(event ->
            {
                Throwable exception = event.getSource().getException();
                String message = exception.getMessage();
                if (exception.getCause() instanceof JFRemoteException)
                {
                    message = ((JFRemoteException) exception.getCause()).getErrorKind().toString();
                }
                DialogsHelper.showError(message);
            });
            
            // start them
            this.imageService.start();
            this.documentService.start();
        }
        catch (Exception e)
        {
            DialogsHelper.showError(e.getMessage());
            return false;
        }
        
        return true;
    }

    private void initListeners()
    {
        this.findPanel.setListener(new IFind<TreeItem<XmlTreeItem>>()
        {
            @Override
            public void find(TreeItem<XmlTreeItem> xpathItemTreeItem)
            {
                xmlTreeView.selectAndScroll(xpathItemTreeItem);
            }

            @Override
            public List<TreeItem<XmlTreeItem>> findItem(String what, boolean matchCase, boolean wholeWord)
            {
                return xmlTreeView.findItem(what, matchCase, wholeWord);
            }
        });
        
        for (int i = 0; i < this.lines.length - 1; i++)
        {
            this.lines[i].btnCopyToRelative.setOnAction(ev -> 
            {
                String text = ((Button) ev.getSource()).getText();
                this.cfRelativeFrom.setText(text);
            });

            this.lines[i].btnXpath.setOnAction(ev -> 
            {
                this.cfMainExpression.setText(((Button) ev.getSource()).getText());
            });
        }
		OneLine magicLine = lines[lines.length-1];
        //TODO add listener
		magicLine.btnCopyToRelative.setOnAction(e -> {});

	}
}
