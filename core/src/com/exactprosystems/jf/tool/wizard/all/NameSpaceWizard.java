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
import com.exactprosystems.jf.tool.wizard.related.refactor.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.*;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@WizardAttribute(
        name = R.NAMESPACE_WIZARD_NAME,
        pictureName = "NameSpaceWizard.png",
        category = WizardCategory.MATRIX,
        shortDescription = R.NAMESPACE_WIZARD_SHORT_DESCRIPTION,
        experimental = false,
        strongCriteries = true,
        criteries = {MatrixFx.class, NameSpace.class},
        detailedDescription = R.NAMESPACE_WIZARD_DETAILED_DESCRIPTION
)
public class NameSpaceWizard extends AbstractWizard {

    private MatrixFx currentMatrix;
    private NameSpace currentNameSpace;

    private ListView<Item>        subcaseListView  = new ListView<>();
    private ListView<Refactor>    refactorListView = new ListView<>();
    private CustomFieldWithButton nextNamespace;
    private ProgressIndicator     indicator        = new ProgressIndicator();
    private BorderPane            borderPane       = new BorderPane();

    @Override
    public boolean beforeRun() {

		IntStream.range(0, this.currentNameSpace.count())
				.mapToObj(i -> (SubCase) this.currentNameSpace.get(i))
				.map(subCase -> new Item(subCase, false))
				.forEach(this.subcaseListView.getItems()::add);

        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.setMinWidth(550);
        borderPane.setPrefWidth(600);
        borderPane.setPrefHeight(500);

        this.subcaseListView.setEditable(false);
        this.subcaseListView.setMinHeight(200);
        this.subcaseListView.setMaxHeight(600);
        this.refactorListView.setMaxHeight(300);
        this.refactorListView.setMinHeight(200);

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

        subcaseListView.setCellFactory(CheckBoxListCell.forListView(Item::onProperty));

        GridPane gridPane = new GridPane();

        Button refresh = new Button(R.NAMESPACE_WIZARD_SCAN.get());
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
        gridPane.getColumnConstraints().addAll(columnConstraints, columnConstraints1, columnConstraints2, columnConstraints3);
        gridPane.setVgap(4);
        gridPane.setHgap(4);
        gridPane.add(subcaseListView, 0, 0, 7, 1);
        gridPane.add(new Label(), 0, 1);
        gridPane.add(new Label(R.WIZARD_WHERE_TO_MOVE.get()), 1, 1);
        GridPane.setFillWidth(nextNamespace, false);
        gridPane.add(nextNamespace, 2, 1);
        gridPane.add(new Label(), 4, 1);
        gridPane.add(refresh, 5, 1);
        gridPane.add(new Label(), 6, 1);
        gridPane.add(this.borderPane, 0, 2, 7, 1);

        this.borderPane.setCenter(refactorListView);
        borderPane.setCenter(gridPane);
        BorderPane.setAlignment(gridPane, Pos.CENTER);
    }

	@Override
	protected Supplier<List<WizardCommand>> getCommands() {
		List<WizardCommand> res = new LinkedList<>();
		this.refactorListView.getItems().stream()
				.flatMap(refactor -> refactor.getCommands().stream())
				.forEach(res::add);

		res.addAll(CommandBuilder.start()
				.refreshConfig(context.getConfiguration())
				.build()
		);
		return () -> res;
	}

    private void createCommands() {
        Configuration config = super.context.getConfiguration();
        String newNamespace = this.nextNamespace.getText();
        refactorListView.getItems().clear();

        borderPane.getChildren().remove(refactorListView);
        double height = refactorListView.getHeight()*0.9;
        indicator.setPrefHeight(height);
        indicator.setMinHeight(height);
        indicator.setMaxHeight(height);
        borderPane.setCenter(indicator);

        Label label = new Label(R.WIZARD_STATUS_LOADING.get());
		BorderPane.setAlignment(label, Pos.CENTER);
		Task<List<Refactor>> task = new Task<List<Refactor>>() {
			@Override
			protected List<Refactor> call()
			{
				Common.runLater(() -> borderPane.setBottom(label));
				List<Refactor> resultList = new LinkedList<>();

				subcaseListView.getItems().forEach(item ->
				{
					if (item.isOn())
					{
						String oldPoint = currentNameSpace.getId() + "." + item.getName();
						String newPoint = newNamespace + "." + item.getName();

						resultList.add(new RefactorRemoveItem(currentMatrix, item.getSub()));

						Matrix newLib = config.getLib(newNamespace);

						boolean sameFile = Objects.equals(new File(newLib.getNameProperty().get()).getAbsolutePath(), new File(currentMatrix.getNameProperty().get()).getAbsolutePath());
						if (sameFile)
						{
							//separate files
							newLib = currentMatrix;
						}

						Matrix finalNewLib = newLib;
						newLib.getRoot()
								.find(i -> i instanceof NameSpace && Objects.equals(i.get(Tokens.Id), newNamespace))
								.ifPresent(ns -> {
									resultList.add(new RefactorAddItem(finalNewLib, ns, item.getSub(), 0));
									resultList.add(new RefactorSaveDocument(finalNewLib));
									if (!sameFile)
									{
										resultList.add(new RefactorSaveDocument(currentMatrix));
									}
								});

						config.forEach(document -> {
							List<Call> calls = findCalls((Matrix) document, oldPoint);

							if (calls.size() > 0)
							{
								resultList.add(new RefactorSetField((Matrix) document, Tokens.Call, newPoint, calls
										.stream()
										.map(MatrixItem::getNumber)
										.collect(Collectors.toList())));
								resultList.add(new RefactorSaveDocument(document));
							}}, DocumentKind.MATRIX, DocumentKind.LIBRARY);
					}
				});

				return resultList;
			}
		};

		task.setOnSucceeded(event -> {
			List<Refactor> value = (List<Refactor>) event.getSource().getValue();
			refactorListView.getItems().addAll(value);
			borderPane.getChildren().remove(indicator);
			borderPane.setCenter(refactorListView);
			borderPane.getChildren().remove(label);
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
		return matrix.getRoot()
				.findAll(i -> i instanceof Call && i.get(Tokens.Call).equals(name))
				.stream().map(i -> (Call) i)
				.collect(Collectors.toList());
	}

	private static class Item {
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