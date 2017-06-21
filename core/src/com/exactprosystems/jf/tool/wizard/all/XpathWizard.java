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
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.common.utils.JfService;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.guidic.Section;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.Common.SpacerEnum;
import com.exactprosystems.jf.tool.custom.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.TreeTableViewWithRectangles;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.ImageAndOffset;
import com.exactprosystems.jf.tool.wizard.related.XpathTreeItem;

import javafx.concurrent.Service;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
            this.btnXpath          = new Button(" ");
            this.btnCopyToRelative = new Button("Rel");
            this.labelXpathCount   = new Label(" ");
            this.hbox = new HBox(this.btnXpath, this.btnCopyToRelative, this.labelXpathCount);
        }
        
        public Label  labelXpathCount;
        public Button btnXpath;
        public Button btnCopyToRelative;
        public HBox   hbox;
    }
    
    private AppConnection                      currentConnection = null;
    private DictionaryFx                       currentDictionary = null;
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
    private TreeTableViewWithRectangles        treeTableViewWithRectangles;
    private FindPanel<TreeItem<XpathTreeItem>> findPanel;
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
        this.currentDictionary = super.get(DictionaryFx.class, parameters);
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
        borderPane.setPrefSize(600.0, 800.0);
        borderPane.setMinSize(600.0, 800.0);

        this.treeTableViewWithRectangles = new TreeTableViewWithRectangles(v->{}, v->{});
        this.treeTableViewWithRectangles.hideFirstColumn();

        this.imageViewWithScale = new ImageViewWithScale();
        this.imageViewWithScale.hideIds();
        this.imageViewWithScale.setClickConsumer(this.treeTableViewWithRectangles::selectItem);
        
        this.splitPane = new SplitPane(this.imageViewWithScale, this.treeTableViewWithRectangles);
        this.splitPane.setOrientation(Orientation.VERTICAL);
        
        this.findPanel = new FindPanel<>();
        
        TitledPane helper = new TitledPane();
        helper.setText("Helper");
        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new Insets(5, 5, 5, 5));
        this.cfRelativeFrom = new CustomFieldWithButton();
        HBox relative = new HBox(new Label("Relative "), this.cfRelativeFrom);
        grid.add(relative, 0, 0);
        this.useText = new CheckBox("use text()");
        this.hBoxCheckboxes = new HBox(this.useText);
        grid.add(this.hBoxCheckboxes, 0, 1);
        
        this.lines = new OneLine[4];
        for (int i = 0; i < this.lines.length; i++)
        {
            this.lines[i] = new OneLine(i);
            grid.add(this.lines[i].hbox, 0, 2 + i);
        }
        helper.setContent(grid);
        Accordion accordion = new Accordion();
        accordion.getPanes().add(helper);
        
        this.cfMainExpression = new CustomFieldWithButton("Enter xpath here");
        this.lblFound = new Label("100");
        HBox resultBox = new HBox(this.cfMainExpression, Common.createSpacer(SpacerEnum.VerticalMin), this.lblFound);
        
        VBox mainBox = new VBox(this.splitPane, this.findPanel, accordion);
        
        borderPane.setCenter(mainBox);
        borderPane.setBottom(resultBox);
        
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
            Locator elementLocator = this.currentControl.locator();
            IRemoteApplication service = this.currentConnection.getApplication().service();
            
            // get picture
            this.imageService = new JfService<ImageAndOffset>(this.executor, 
                    () -> Common.tryCatch(() ->
                        {
                            int offsetX, offsetY;
                            Rectangle rectangle = service.getRectangle(selfLocator, elementLocator);
                            offsetX = rectangle.x;
                            offsetY = rectangle.y;
                            BufferedImage image = service.getImage(selfLocator, elementLocator).getImage();
                            return new ImageAndOffset(image, offsetX, offsetY);
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
                    () -> Common.tryCatch(() -> service.getTree(selfLocator), "Error on document getting", null));
            this.documentService.setOnSucceeded(event ->
            {
                this.document = (Document) event.getSource().getValue();
                this.currentNode = XpathUtils.getFirst(this.document, "/*");
                if (this.imageAndOffset != null)
                {
                    this.treeTableViewWithRectangles.displayDocument(this.document, this.imageAndOffset.offsetX, this.imageAndOffset.offsetY);
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
        this.findPanel.setListener(new IFind<TreeItem<XpathTreeItem>>()
        {
            @Override
            public void find(TreeItem<XpathTreeItem> xpathItemTreeItem)
            {
                treeTableViewWithRectangles.selectAndScroll(xpathItemTreeItem);
            }

            @Override
            public List<TreeItem<XpathTreeItem>> findItem(String what, boolean matchCase, boolean wholeWord)
            {
                return treeTableViewWithRectangles.findItem(what, matchCase, wholeWord);
            }
        });
        
        for (int i = 0; i < this.lines.length; i++)
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
    }
}
