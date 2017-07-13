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
import com.exactprosystems.jf.documents.matrix.parser.items.Call;
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
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorSetField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
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
        experimental = true,
        strongCriteries = true,
        criteries = {MatrixFx.class, NameSpace.class},
        detailedDescription = "When it's need to move several SubCases to another known NameSpace."
)
public class NameSpaceWizard extends AbstractWizard {


    private MatrixFx currentMatrix;
    private NameSpace currentNameSpace;

    private ListView<Item> listView = new ListView<>();
    private ListView<Refactor> listView2 = new ListView<>();
    private CustomFieldWithButton nextNamespace;


    @Override
    public boolean beforeRun() {

        for (int i = 0; i < currentNameSpace.count(); i++)
        {
            this.listView.getItems().add(new Item((SubCase) this.currentNameSpace.get(i),false));
        }

        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.setMinWidth(400);
        borderPane.setPrefWidth(400);

        this.listView.setEditable(false);
        this.listView.setMinHeight(300);
        this.listView.setMaxHeight(600);
        listView2.setMaxHeight(300);

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

        Button refresh = new Button("Scan");
        refresh.setOnAction(event -> createCommands(this.listView, this.nextNamespace));

        pane.add(this.listView, 0, 0, 1, 2);
        pane.add(new Label("Move to: "), 2, 0);
        pane.add(this.nextNamespace, 2, 1);
        pane.add(refresh,0,2);
        pane.add(this.listView2,0,3,3,1);
        pane.setGridLinesVisible(true);


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
        ObservableList<Refactor> items = listView2.getItems();
        items.clear();

        list.getItems().forEach(item ->
        {
            if (item.isOn())
            {
                String oldSubName = currentNameSpace.getId() + "." + item.getName();
                String newSubName = newNamespace + "." + item.getName();

                res.add(new RefactorRemoveItem(currentMatrix, item.getSub()));

                Matrix newLib = config.getLib(newNamespace);
                Optional<MatrixItem> namespace = newLib.getRoot().find(i -> i instanceof NameSpace && Objects.equals(i.get(Tokens.Id), newNamespace));
                res.add(new RefactorAddItem(newLib, namespace.get(), item.getSub(), 0));

                List<Call> calls = findCalls(this.currentMatrix, oldSubName);
                RefactorSetField refactorSetField = new RefactorSetField(this.currentMatrix, Tokens.Call, newSubName, calls.stream()
                        .map(MatrixItem::getNumber).collect(Collectors.toList()));
                res.add(refactorSetField);
                items.add(refactorSetField);
            }
        });

        return res;

    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentNameSpace = super.get(NameSpace.class, parameters);

    }

    private List<ReadableValue> getNamespaces() {
        return super.context.getConfiguration()
                .getLibs()
                .keySet()
                .stream()
                .map(ReadableValue::new)
                .collect(Collectors.toList());
    }


    private List<Call> findCalls(Matrix matrix, String name)
    {
        return matrix.getRoot().findAll(i -> i instanceof Call && i.get(Tokens.Call).equals(name))
                .stream().map(i -> (Call)i).collect(Collectors.toList());
    }

    public static class Item {
        private SubCase sub;
        private final BooleanProperty on = new SimpleBooleanProperty();

        public SubCase getSub() {
            return sub;
        }

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