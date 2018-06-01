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

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.Call;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.refactor.*;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@WizardAttribute(
        name                = R.REFACTOR_WIZARD_NAME,
        pictureName         = "RefactorWizard.jpg", 
        category            = WizardCategory.MATRIX, 
        shortDescription    = R.REFACTOR_WIZARD_SHORT_DESCRIPTION,
        experimental        = false, 
        strongCriteries     = true, 
        criteries           = { MatrixFx.class, SubCase.class }, 
        detailedDescription = R.REFACTOR_WIZARD_DETAILED_DESCRIPTION
)
public class RefactorWizard extends AbstractWizard
{
    private MatrixFx              currentMatrix;
    private SubCase               currentSubCase;
    private NameSpace             currentNameSpace;

    private ListView<Refactor>    listView;
    private TextField             prevSubcase;
    private TextField             prevNamespace;

    private TextField             nextSubcase;
    private CustomFieldWithButton nextNamespace;

    private boolean               success = false;

    @Override
    public boolean beforeRun()
    {
        MatrixItem parent = this.currentSubCase.findParent(NameSpace.class);
        if (parent instanceof NameSpace)
        {
            this.currentNameSpace = ((NameSpace) parent);
        }else
        {
            DialogsHelper.showError(R.WIZARD_INVOKE_FROM_NAMESPACE.get());
            return false;
        }
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
        borderPane.setMinWidth(750);
        borderPane.setPrefWidth(750);
        this.listView = new ListView<>();
        this.listView.setEditable(false);
        this.listView.setMinHeight(400);
        this.listView.setMaxHeight(600);

        String subcase = this.currentSubCase.getId();
        String namespace = this.currentNameSpace == null ? "" : this.currentNameSpace.getId();
        
        this.prevSubcase    = new TextField(subcase);
        this.prevSubcase.setDisable(true);
        this.prevNamespace  = new TextField(namespace);
        this.prevNamespace.setDisable(true);

        this.nextSubcase    = new TextField(subcase);
        this.nextNamespace  = new CustomFieldWithButton(namespace);
        this.nextNamespace.setEditable(false);
        this.nextNamespace.setButtonText("≡");
        this.nextNamespace.setHandler(e -> 
        {
            String currentText = this.nextNamespace.getText();
            List<ReadableValue> list = getNamespaces();
            ReadableValue value = new ReadableValue(currentText);
            value = DialogsHelper.selectFromList(R.REFACTOR_WIZARD_NAMESPACES.get(), value, list);
            this.nextNamespace.setText(value.getValue());
        });
        
        GridPane pane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setMaxWidth(200);
        columnConstraints.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.SOMETIMES);
        pane.getColumnConstraints().addAll(columnConstraints, columnConstraints1);
        pane.setVgap(8);
        pane.setHgap(4);

        Button refresh = new Button(R.REFACTOR_WIZARD_SCAN.get());
        refresh.setOnAction(event -> scanChangeds() );

        pane.add(new Label(R.REFACTOR_WIZARD_SUBCASE.get()), 0, 0);
        pane.add(new Label(R.REFACTOR_WIZARD_NAMESPACE.get()), 0, 1);
        pane.add(this.prevSubcase, 1, 0);
        pane.add(this.prevNamespace, 1, 1);
        pane.add(new Label("==>"), 2, 0);
        pane.add(new Label("==>"), 2, 1);
        pane.add(this.nextSubcase, 3, 0);
        pane.add(this.nextNamespace, 3, 1);
        pane.add(refresh, 0, 3);
        pane.add(new Label(R.REFACTOR_WIZARD_AFFECTED_FILES.get()), 1, 3, 2, 1);
        pane.add(this.listView, 0, 4, 4, 1);

        borderPane.setCenter(pane);
        BorderPane.setAlignment(pane, Pos.CENTER);
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
    	List<WizardCommand> list = new ArrayList<>();
    	if (this.success)
    	{
	       this.listView.getItems().forEach(i -> list.addAll(i.getCommands()));
	       list.addAll(CommandBuilder.start()
                   .refreshConfig(context.getConfiguration())
                   .build());
        }
    	else
    	{
    	    DialogsHelper.showError(R.REFACTOR_WIZARD_WRONG_PARAMETERS.get());
    	}

    	return () -> list;
    }

    @Override
    public void init(Context context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentSubCase = super.get(SubCase.class, parameters);
    }
    
    private List<ReadableValue> getNamespaces()
    {
        return super.context.getConfiguration()
                .getLibs()
                .keySet()
                .stream()
                .map(ReadableValue::new)
                .collect(Collectors.toList());
    }
    
    private void scanChangeds()
    {
        this.success = true;
        ObservableList<Refactor> items = this.listView.getItems();
        items.clear();

        String oldSubcase   = this.prevSubcase.getText();
        String oldNamespace = this.prevNamespace.getText();
        String newSubcase   = this.nextSubcase.getText();
        String newNamespace = this.nextNamespace.getText();
        
        String oldCallPoint = (oldNamespace.isEmpty() ? "" : (oldNamespace + ".")) + oldSubcase;
        String newCallPoint = (newNamespace.isEmpty() ? "" : (newNamespace + ".")) + newSubcase;
        
        if (!Str.areEqual(oldCallPoint, newCallPoint))
        {
            Configuration config = super.context.getConfiguration();
    
            if (!Str.areEqual(oldSubcase, newSubcase))
            {
                // at first rename this subcase
                items.add(new RefactorSetField(this.currentMatrix, Tokens.Id, newSubcase, Collections.singletonList(this.currentSubCase.getNumber())));
            }
            
            if (!Str.areEqual(oldNamespace, newNamespace))
            {
                // ... and move it to another namespace / file
                items.add(new RefactorRemoveItem(this.currentMatrix, this.currentSubCase));
                
                Matrix newLib = config.getLib(newNamespace);
				Matrix oldLib = config.getLib(oldNamespace);

				if (Objects.equals(new File(newLib.getNameProperty().get()).getAbsolutePath(), new File(oldLib.getNameProperty().get()).getAbsolutePath()))
				{
					newLib = this.currentMatrix;
				}

				Matrix finalNewLib = newLib;
				newLib.getRoot()
						.find(i -> i instanceof NameSpace && Objects.equals(i.get(Tokens.Id), newNamespace))
						.ifPresent(item -> {
							items.add(new RefactorAddItem(finalNewLib, item, this.currentSubCase, 0));
							items.add(new RefactorSaveDocument(finalNewLib));
						});
            }
            
            
            if (oldNamespace.isEmpty())
            {
                // don't need to scan all files
                List<Call> calls = findCalls(this.currentMatrix, oldCallPoint);
				items.add(new RefactorSetField(this.currentMatrix, Tokens.Call, newCallPoint, calls.stream()
						.map(MatrixItem::getNumber)
						.collect(Collectors.toList())));
            }
            else
            {
                // scan everything
                boolean onlyCheck = newNamespace.isEmpty();
                config.forEach(d ->
                {
                    Matrix matrix = (Matrix)d;
                    // try to find ...
                    List<Call> calls = findCalls(matrix, oldCallPoint);
                    if (calls.size() > 0)
                    {
                        if (onlyCheck)
                        {
                            items.add(new RefactorEmpty(MessageFormat.format(R.WIZARD_MATRIX_CONTAINS_REFERENCES_2.get(), matrix.getNameProperty().get(), calls.size())));
                            this.success = false;
                        }
                        else
                        {
							items.add(new RefactorLoadDocument(matrix));
                            items.add(new RefactorSetField(matrix, Tokens.Call, newCallPoint, calls
									.stream()
                                    .map(MatrixItem::getNumber)
									.collect(Collectors.toList())));
							items.add(new RefactorSaveDocument(matrix));
                        }
                    }
                }, DocumentKind.LIBRARY, DocumentKind.MATRIX);
            }
        }        

        if (items.size() == 0)
        {
            items.add(new RefactorEmpty(R.WIZARD_NO_CHANGES_NEEDED.get()));
            this.success = false;
        }
    }
    
    private List<Call> findCalls(Matrix matrix, String name)
    {
        return matrix.getRoot()
				.findAll(i -> i instanceof Call && i.get(Tokens.Call).equals(name))
				.stream()
				.map(i -> (Call)i)
				.collect(Collectors.toList());
    }
    
}
