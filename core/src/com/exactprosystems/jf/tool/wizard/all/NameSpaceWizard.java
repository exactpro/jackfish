////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.wizard.all;


import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.Call;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.refactor.Refactor;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorAddItem;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorRemoveItem;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorSetField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@WizardAttribute(
        name = "NameSpace wizard",
        pictureName = "NameSpaceWizard.png",
        category = WizardCategory.MATRIX,
        shortDescription = "This wizard helps to move SubCases between NameSpaces",
        experimental = false,
        strongCriteries = true,
        criteries = {MatrixFx.class, NameSpace.class},
        detailedDescription = "{{`Wizard moves SubCases from one NameSpace to another and brings changes to the all affected matrices`}}"
                +"{{`Simply choose SubCases that you want to move.`}}"
                +"{{`Than choose another NameSpace in dropbox and press Scan(important).`}}"
                +"{{`After you will see the list of changes in section below.`}}"
                +"{{`Press accept to apply changes.`}}"
                +"{{`Better make backup of your files before.`}}"
)
public class NameSpaceWizard extends AbstractWizard {


    private MatrixFx currentMatrix;
    private NameSpace currentNameSpace;

    private ListView<Item> listView = new ListView<>();
    private ListView<Refactor> listView2 = new ListView<>();
    private CustomFieldWithButton nextNamespace;
    private ProgressIndicator indicator = new ProgressIndicator();
    private BorderPane pane1 = new BorderPane();


    @Override
    public boolean beforeRun() {

        for (int i = 0; i < currentNameSpace.count(); i++)
        {
            this.listView.getItems().add(new Item((SubCase) this.currentNameSpace.get(i), false));
        }

        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.setMinWidth(550);
        borderPane.setPrefWidth(600);
        borderPane.setPrefHeight(500);

        this.listView.setEditable(false);
        this.listView.setMinHeight(200);
        this.listView.setMaxHeight(600);
        this.listView2.setMaxHeight(300);
        this.listView2.setMinHeight(200);

        String namespace = this.currentNameSpace == null ? "" : this.currentNameSpace.getId();

        this.nextNamespace = new CustomFieldWithButton(namespace);
        this.nextNamespace.setEditable(false);
        this.nextNamespace.setButtonText("≡");
        this.nextNamespace.setHandler(e ->
        {
            String currentText = this.nextNamespace.getText();
            List<ReadableValue> list = getNamespaces();
            ReadableValue value = new ReadableValue(currentText);
            value = DialogsHelper.selectFromList(R.WIZARD_NAMESPACES.get(), value, list);
            this.nextNamespace.setText(value.getValue());
        });

        listView.setCellFactory(CheckBoxListCell.forListView(Item::onProperty));

        GridPane pane = new GridPane();

        Button refresh = new Button("Scan");
        refresh.setOnAction(event -> createCommands());
        indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setMaxWidth(250);
        columnConstraints.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints2 = new ColumnConstraints();
        columnConstraints2.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints3 = new ColumnConstraints();
        columnConstraints3.setHgrow(Priority.SOMETIMES);
        pane.getColumnConstraints().addAll(columnConstraints, columnConstraints1, columnConstraints2, columnConstraints3);
        pane.setVgap(4);
        pane.setHgap(4);
        pane.add(listView, 0, 0, 7, 1);
        pane.add(new Label(), 0, 1);
        pane.add(new Label(R.WIZARD_WHERE_TO_MOVE.get()), 1, 1);
        GridPane.setFillWidth(nextNamespace, false);
        pane.add(nextNamespace, 2, 1);
        pane.add(new Label(), 4, 1);
        pane.add(refresh, 5, 1);
        pane.add(new Label(), 6, 1);
        pane.add(pane1, 0, 2, 7, 1);

        pane1.setCenter(listView2);
        borderPane.setCenter(pane);
        BorderPane.setAlignment(pane, Pos.CENTER);
    }


    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        List<WizardCommand> res = new LinkedList<>();
        this.listView2.getItems().forEach(i -> res.addAll(i.getCommands()));
        res.addAll(CommandBuilder.start()
                .refreshConfig(context.getConfiguration())
                .build()
        );
        return () -> res;
    }

    private void createCommands() {
        Configuration config = super.context.getConfiguration();
        String newNamespace = this.nextNamespace.getText();
        listView2.getItems().clear();

        pane1.getChildren().remove(listView2);
        double height = listView2.getHeight()*0.9;
        indicator.setPrefHeight(height);
        indicator.setMinHeight(height);
        indicator.setMaxHeight(height);
        pane1.setCenter(indicator);

        Label label = new Label(R.WIZARD_STATUS_LOADING.get());
        BorderPane.setAlignment(label, Pos.CENTER);
        Task<List<Refactor>> task = new Task<List<Refactor>>() {
            @Override
            protected List<Refactor> call() throws Exception {
                Common.runLater(() -> pane1.setBottom(label));
                List<Refactor> res = new LinkedList<>();

                config.forEach(document -> listView.getItems().forEach(item ->
                {
                    if (item.isOn())
                    {
                        String oldSubName = currentNameSpace.getId() + "." + item.getName();
                        String newSubName = newNamespace + "." + item.getName();

                        res.add(new RefactorRemoveItem(currentMatrix, item.getSub()));

                        Matrix newLib = config.getLib(newNamespace);
                        Optional<MatrixItem> namespace = newLib.getRoot().find(i -> i instanceof NameSpace && Objects.equals(i.get(Tokens.Id), newNamespace));
                        res.add(new RefactorAddItem(newLib, namespace.get(), item.getSub(), 0));

                        List<Call> calls = findCalls((Matrix) document, oldSubName);
                        calls.addAll(findCalls((currentMatrix), item.getName()));

                        if (calls.size() > 0)
                        {
                            res.add(new RefactorSetField((Matrix) document, Tokens.Call, newSubName, calls.stream()
                                    .map(c -> c.getNumber()).collect(Collectors.toList())));
                        }
                    }

                }), DocumentKind.MATRIX, DocumentKind.LIBRARY);
                return res;
            }
        };

        task.setOnSucceeded(event -> {
            List<Refactor> value = (List<Refactor>) event.getSource().getValue();
            listView2.getItems().addAll(value);
            pane1.getChildren().remove(indicator);
            pane1.setCenter(listView2);
            pane1.getChildren().remove(label);

        });
        new Thread(task).start();
    }

    @Override
    public void init(Context context, WizardManager wizardManager, Object... parameters) {
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


    private List<Call> findCalls(Matrix matrix, String name) {
        return matrix.getRoot().findAll(i -> i instanceof Call && i.get(Tokens.Call).equals(name))
                .stream().map(i -> (Call) i).collect(Collectors.toList());
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