////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;

import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.related.refactor.Refactor;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorAddSubcase;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorRenameCall;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.*;
import java.util.function.Supplier;

@WizardAttribute(
        name                = "Library wizard", 
        pictureName         = "GherkinWizard.jpg", 
        category            = WizardCategory.MATRIX, 
        shortDescription    = "This wizard helps refactor libraries and  matrix.", 
        experimental        = true, 
        strongCriteries     = true, 
        criteries           = { MatrixFx.class, SubCase.class }, 
        detailedDescription = ""
)
public class LibraryWizard extends AbstractWizard
{
    private MatrixFx         currentMatrix;
    private SubCase          currentSubCase;
    private NameSpace        currentNameSpace;

    private ListView<Refactor> listView;
    private TextField nameOfSub;
    private TextField nameOfNameSpace;
    private TextField nameOfaNewFile;

    @Override
    public boolean beforeRun()
    {
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {

        borderPane.setPrefWidth(630);
        this.listView = new ListView<>();
        this.listView.setEditable(false);
        this.listView.setMinHeight(400);
        this.listView.setMaxHeight(600);

        Label label1 = new Label("SubCase: " + this.currentSubCase);
        Label label2 = new Label("NameSpace: " + this.currentNameSpace);
        Label label3 = new Label("Library file: ");

        this.nameOfSub          = new TextField(this.currentSubCase.getId());
        this.nameOfNameSpace    = new TextField(this.currentNameSpace == null ? "" : this.currentNameSpace.getId());
        this.nameOfaNewFile     = new TextField(this.currentSubCase.getMatrix().getName());
        
        GridPane pane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setMaxWidth(200);
        columnConstraints.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.SOMETIMES);
        pane.getColumnConstraints().addAll(columnConstraints, columnConstraints1);
        pane.setVgap(8);
        pane.setHgap(4);

        Button refresh = new Button("Scan");
        refresh.setOnAction(event ->
        {
        	Configuration config = super.context.getConfiguration();
            config.forEach(d -> System.err.println(d), DocumentKind.LIBRARY, DocumentKind.MATRIX);

        	
        	this.listView.getItems().clear();
        	this.listView.getItems().add(new RefactorAddSubcase(this.currentMatrix, this.currentNameSpace, this.currentSubCase));
        	this.listView.getItems().add(new RefactorRenameCall("Rename Call1"));
        	this.listView.getItems().add(new RefactorRenameCall("Rename Call2"));
        	this.listView.getItems().add(new RefactorRenameCall("Rename Call3"));
        	this.listView.getItems().add(new RefactorRenameCall("Rename Call4"));
            // TODO

        });

        pane.add(label1, 0, 0);
        pane.add(label2, 0, 1);
        pane.add(label3, 0, 2);
        pane.add(this.nameOfSub, 1, 0);
        pane.add(this.nameOfNameSpace, 1, 1);
        pane.add(this.nameOfaNewFile, 1, 2);
        pane.add(refresh, 0, 3);
        pane.add(new Label("Affected files: "), 1, 3, 2, 1);
        pane.add(this.listView, 0, 4, 2, 1);

        borderPane.setCenter(pane);
        BorderPane.setAlignment(pane, Pos.CENTER);

    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
    	List<WizardCommand> list = new ArrayList<>();
    	this.listView.getItems().forEach(i -> list.addAll(i.getCommands()));

        return () ->
        {
        	return list;
        };
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentSubCase = super.get(SubCase.class, parameters);
        this.currentNameSpace = (NameSpace)this.currentSubCase.findParent(NameSpace.class);
    }
}
