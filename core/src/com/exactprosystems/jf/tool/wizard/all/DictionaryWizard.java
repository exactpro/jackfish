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
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.*;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.xmltree.XmlTreeView;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.WizardHelper;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import java.awt.Rectangle;
import java.util.List;
import java.util.function.Supplier;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
    private AppConnection               currentConnection  = null;
    private DictionaryFx                currentDictionary  = null;
    private Window                      currentWindow      = null;

    private volatile Document           document           = null;
    private volatile Node               currentNode        = null;

    private SplitPane                   splitPane          = null;
    private ImageViewWithScale          imageViewWithScale = null;
    private XmlTreeView                 xmlTreeView        = null;
    private FindPanel<org.w3c.dom.Node> findPanel          = null;
    
    public DictionaryWizard()
    {
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);
        
        this.currentConnection = super.get(AppConnection.class, parameters);
        this.currentDictionary = super.get(DictionaryFx.class, parameters);
        this.currentWindow     = super.get(Window.class, parameters);
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

        this.xmlTreeView = new XmlTreeView();
        
        this.xmlTreeView.setMarkersVisible(false);

        this.imageViewWithScale = new ImageViewWithScale();
        this.imageViewWithScale.setOnRectangleClick(rectangle -> this.xmlTreeView.selectItem(rectangle));

        gridPane.add(this.xmlTreeView, 0, 0);
        this.splitPane.getItems().addAll(this.imageViewWithScale, gridPane);
        //endregion

        borderPane.setCenter(this.splitPane);        
        
        initListeners();
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return () ->
        {
            List<WizardCommand> commands = CommandBuilder
                    .start()
                    .build();
            
            return commands;
        };
    }

    @Override
    public boolean beforeRun()
    {
        try
        {
            if (this.currentConnection == null && !this.currentConnection.isGood())
            {
                DialogsHelper.showError("Application is not started.\nStart it before call the wizard.");
                return false;
            }
            
            IControl self = this.currentWindow.getSelfControl();
            WizardHelper.gainImageAndDocument(this.currentConnection, self, 
            (image, doc) ->
            {
                this.imageViewWithScale.displayImage(image);

                this.document = doc;
                this.xmlTreeView.displayDocument(this.document);
                List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
                this.imageViewWithScale.setListForSearch(list);
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
    }
}
