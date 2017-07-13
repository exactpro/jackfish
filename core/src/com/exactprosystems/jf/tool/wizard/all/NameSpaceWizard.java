////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.wizard.all;


import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.related.refactor.Refactor;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorAddItem;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorRemoveItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@WizardAttribute(
        name = "NameSpace wizard",
        pictureName = "NameSpaceWizard.jpg",
        category = WizardCategory.MATRIX,
        shortDescription = "This wizard helps to move SubCases between NameSpaces",
        experimental = false,
        strongCriteries = true,
        criteries = {MatrixFx.class, NameSpace.class},
        detailedDescription = "When it's need to move several SubCases to another known NameSpace."
)
public class NameSpaceWizard extends AbstractWizard {


    private MatrixFx currentMatrix;
    private List<SubCase> currentSubCases;
    private NameSpace currentNameSpace;

    private ListView<Item> listView;
    private CustomFieldWithButton nextNamespace;


    @Override
    public boolean beforeRun() {
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.setMinWidth(400);
        borderPane.setPrefWidth(400);
        this.listView = new ListView<>();
        this.listView.setEditable(false);
        this.listView.setMinHeight(400);
        this.listView.setMaxHeight(600);

        String namespace = this.currentNameSpace == null ? "" : this.currentNameSpace.getId();

        this.nextNamespace = new CustomFieldWithButton(namespace);
        this.nextNamespace.setEditable(false);
        this.nextNamespace.setButtonText("≡");
        this.nextNamespace.setHandler(e ->
        {
            String currentText = this.nextNamespace.getText();
            List<ReadableValue> list = getNamespaces();
            ReadableValue value = new ReadableValue(currentText);
            value = DialogsHelper.selectFromList("Namespases", value, list);
            this.nextNamespace.setText(value.getValue());
        });

        currentSubCases.forEach(subCase ->
        {
            Item item = new Item(subCase, false);
            listView.getItems().add(item);
        });

        listView.setCellFactory(CheckBoxListCell.forListView(Item::onProperty));


        GridPane pane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setMaxWidth(250);
        columnConstraints.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.NEVER);
        pane.getColumnConstraints().addAll(columnConstraints, columnConstraints1);
        pane.setVgap(8);
        pane.setHgap(4);

        pane.add(this.listView, 0, 0,1,3);
        pane.add(new Label("Move to: "),2,0);
        pane.add(this.nextNamespace,2,1);


        borderPane.setCenter(pane);
        BorderPane.setAlignment(pane, Pos.CENTER);
    }


    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        List<WizardCommand> res = new LinkedList<>();
        createCommands(this.listView, this.nextNamespace).forEach(i -> res.addAll(i.getCommands()));
        return () -> res;
    }

    private List<Refactor> createCommands(ListView<Item> list, TextField field) {
        List<Refactor> res = new LinkedList<>();
        Configuration config = super.context.getConfiguration();
        String newNamespace = field.getText();

        list.getItems().forEach(item ->{
            if (item.isOn())
            {
                res.add(new RefactorRemoveItem(currentMatrix, item.getSub()));

                Matrix newLib = config.getLib(newNamespace);
                Optional<MatrixItem> namespace = newLib.getRoot().find(i -> i instanceof NameSpace && Objects.equals(i.get(Tokens.Id), newNamespace));
                res.add(new RefactorAddItem(newLib, namespace.get(), item.getSub(), 0));
            }
        });

        return res;

    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentNameSpace = super.get(NameSpace.class, parameters);
        int count = this.currentNameSpace.count();
        this.currentSubCases = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            this.currentSubCases.add((SubCase) this.currentNameSpace.get(i));
        }
    }

    private List<ReadableValue> getNamespaces() {
        return super.context.getConfiguration()
                .getLibs()
                .keySet()
                .stream()
                .map(ReadableValue::new)
                .collect(Collectors.toList());
    }


    public static class Item {
        private SubCase sub;

        public SubCase getSub() {
            return sub;
        }

        private final BooleanProperty on = new SimpleBooleanProperty();

        private Item(SubCase sub, boolean on) {
            this.sub = sub;
            setOn(on);
        }


        public final String getName() {
            return this.sub.getId();
        }

        public final BooleanProperty onProperty() {
            return this.on;
        }

        public final boolean isOn() {
            return this.onProperty().get();
        }

        public final void setOn(final boolean on) {
            this.onProperty().set(on);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

}