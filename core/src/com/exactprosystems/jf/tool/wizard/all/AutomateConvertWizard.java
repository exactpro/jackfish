////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.app.ApplicationResize;
import com.exactprosystems.jf.api.app.Resize;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.related.refactor.Refactor;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorAddParameter;
import com.exactprosystems.jf.tool.wizard.related.refactor.RefactorRemoveParameters;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@WizardAttribute(
		name = R.AUTOMATE_CONVERT_WIZARD_NAME,
		pictureName = "AutomateConverterWizard.png",
		category = WizardCategory.CONFIGURATION,
		shortDescription = R.AUTOMATE_CONVERT_WIZARD_SHORT_DESCRIPTION,
		experimental = false,
		strongCriteries = true,
		criteries = {Configuration.class},
		detailedDescription = R.AUTOMATE_CONVERT_WIZARD_DETAILED_DESCRIPTION
)
public class AutomateConvertWizard extends AbstractWizard
{
	private Configuration configuration;

	private ListView<Refactor>  listView;
	private ComboBox<Converter> comboBox;

	private Task<List<Refactor>> task;

	private final List<Converter> CONVERTER_LIST = Arrays.asList(
			new ApplicationResizeConverter(),
			new ActionRemoveEmptyNotMandatoryFields()
	);

	@Override
	public void init(Context context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);
		this.configuration = get(Configuration.class, parameters);
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		borderPane.setMinWidth(800);
		borderPane.setPrefWidth(800);

		this.comboBox = new ComboBox<>();
		this.comboBox.getItems().addAll(CONVERTER_LIST);
		this.comboBox.setMaxWidth(Double.MAX_VALUE);

		Button btnScan = new Button(R.WIZARD_SCAN.get());

		TextArea shortDescription = new TextArea(R.WIZARD_SELECT_CONVERTER_FROM_COMBOBOX.get());
		shortDescription.setEditable(false);

		shortDescription.setMaxWidth(Double.MAX_VALUE);
		shortDescription.setMinHeight(100);

		GridPane scanPane = new GridPane();
		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(50.0);
		c0.setHalignment(HPos.LEFT);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(50.0);
		c1.setHalignment(HPos.RIGHT);

		scanPane.getColumnConstraints().addAll(c0, c1);
		RowConstraints r0 = new RowConstraints();
		r0.setMaxHeight(32.0);
		r0.setMinHeight(32.0);
		r0.setPrefHeight(32.0);
		scanPane.getRowConstraints().add(r0);

		scanPane.add(btnScan, 1, 0);

		HBox progressBox = new HBox();
		progressBox.setAlignment(Pos.CENTER_LEFT);
		progressBox.getChildren().addAll(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS), new Label(R.WIZARD_STATUS_SCANNING.get()));
		scanPane.add(progressBox, 0, 0);
		progressBox.setVisible(false);

		btnScan.setOnAction(e -> {
			Converter converter = this.comboBox.getSelectionModel().getSelectedItem();
			if (converter != null)
			{
				if (this.task != null)
				{
					this.task.cancel();
				}
				Consumer<Boolean> setDisable = flag ->
				{
					progressBox.setVisible(flag);
					btnScan.setDisable(flag);
				};
				this.task = new Task<List<Refactor>>()
				{
					@Override
					protected List<Refactor> call() throws Exception
					{
						setDisable.accept(true);
						return converter.scan(this::isCancelled);
					}
				};
				this.listView.getItems().clear();

				this.task.setOnSucceeded(event ->
				{
					this.listView.getItems().setAll(this.task.getValue());
					setDisable.accept(false);
				});
				this.task.setOnFailed(event ->
				{
					DialogsHelper.showError(event.getSource().getException().getMessage());
					setDisable.accept(false);
				});
				this.task.setOnCancelled(event -> setDisable.accept(false));
				new Thread(this.task).start();
			}
		});


		this.listView = new ListView<>();
		this.listView.setMinHeight(400);

		VBox box = new VBox();

		box.getChildren().addAll(
				this.comboBox
				, Common.createSpacer(Common.SpacerEnum.VerticalMid)
				, shortDescription
				, Common.createSpacer(Common.SpacerEnum.VerticalMid)
				, scanPane
				, Common.createSpacer(Common.SpacerEnum.VerticalMid)
				, this.listView);

		this.comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null)
			{
				shortDescription.setText(newValue.shortDescription());
				this.listView.getItems().clear();
				Optional.ofNullable(this.task).ifPresent(Task::cancel);
			}
		});

		borderPane.setCenter(box);
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		List<WizardCommand> list = new ArrayList<>();
		ObservableList<Refactor> items = this.listView.getItems();
		items.stream().map(Refactor::getCommands).forEach(list::addAll);
		return () -> list;
	}

	@Override
	public boolean beforeRun()
	{
		return true;
	}

	@Override
	protected void onRefused()
	{
		Optional.ofNullable(this.task).ifPresent(Task::cancel);
		super.onRefused();
	}

	interface Converter
	{
		String shortDescription();
		List<Refactor> scan(BooleanSupplier stopSupplier);
	}

	private class ApplicationResizeConverter implements Converter
	{
		@Override
		public String toString()
		{
			return this.getClass().getSimpleName();
		}

		@Override
		public String shortDescription()
		{
			return R.WIZARD_APP_RESIZE_DESCRIPTION.get();
		}

		@Override
		public List<Refactor> scan(BooleanSupplier stopSupplier)
		{
			List<Refactor> list = new ArrayList<>();
			configuration.forEach(document ->
			{
				if (stopSupplier.getAsBoolean())
				{
					return;
				}
				Matrix matrix = (Matrix) document;
				MatrixItem root = matrix.getRoot();
				root.bypass(item ->
				{
					if (stopSupplier.getAsBoolean())
					{
						return;
					}
					if (item instanceof ActionItem)
					{
						ActionItem actionItem = (ActionItem) item;
						if (actionItem.getActionClass() == ApplicationResize.class)
						{
							Parameters parameters = actionItem.getParameters();
							List<Integer> integerList = new ArrayList<>();
							String expression = null;

							if (parameters.containsKey(ApplicationResize.maximizeName))
							{
								integerList.add(parameters.getIndex(parameters.getByName(ApplicationResize.maximizeName)));
								expression = Resize.class.getSimpleName() + "." + Resize.Maximize.name();
							}
							if (parameters.containsKey(ApplicationResize.minimizeName))
							{
								integerList.add(parameters.getIndex(parameters.getByName(ApplicationResize.minimizeName)));
								expression = Resize.class.getSimpleName() + "." + Resize.Minimize.name();
							}
							if (parameters.containsKey(ApplicationResize.normalName))
							{
								integerList.add(parameters.getIndex(parameters.getByName(ApplicationResize.normalName)));
								expression = Resize.class.getSimpleName() + "." + Resize.Normal.name();
							}

							if (!integerList.isEmpty())
							{
								list.add(new RefactorRemoveParameters(item, integerList.stream().mapToInt(i -> i).toArray()));
								Parameter parameter = new Parameter(ApplicationResize.resizeName, expression);
								parameter.setType(TypeMandatory.NotMandatory);
								list.add(new RefactorAddParameter(item, parameter, -1));
							}
						}
					}
				});
			}, DocumentKind.MATRIX, DocumentKind.LIBRARY);

			return list;
		}
	}

	private class ActionRemoveEmptyNotMandatoryFields implements Converter
	{
		@Override
		public String toString()
		{
			return this.getClass().getSimpleName();
		}

		@Override
		public String shortDescription()
		{
			return R.WIZARD_REMOVE_EMPTY_PARAMETERS.get();
		}

		@Override
		public List<Refactor> scan(BooleanSupplier stopSupplier)
		{
			List<Refactor> list = new ArrayList<>();
			configuration.forEach(document ->
			{
				if (stopSupplier.getAsBoolean())
				{
					return;
				}
				Matrix matrix = (Matrix) document;
				MatrixItem root = matrix.getRoot();
				root.bypass(item ->
				{
					if (stopSupplier.getAsBoolean())
					{
						return;
					}
					if (item instanceof ActionItem)
					{
						ActionItem actionItem = (ActionItem) item;
						Class<? extends AbstractAction> actionClass = actionItem.getActionClass();
						Parameters parameters = actionItem.getParameters();

						parameters.stream()
								.filter(parameter -> Str.IsNullOrEmpty(parameter.getExpression()))
								//check that parameter is presented on a class ( this means, that parameter is mandatory or notMandatory)
								.filter(parameter -> Arrays.stream(actionClass.getDeclaredFields())
										.map(field ->  field.getAnnotation(ActionFieldAttribute.class))
										.filter(Objects::nonNull)
										.anyMatch(annotation -> annotation.name().equals(parameter.getName()) && annotation.shouldFilled()))
								.map(parameter -> new int[]{parameters.getIndex(parameter)})
								.forEach(indexArray -> list.add(new RefactorRemoveParameters(actionItem, indexArray)));
					}
				});
			}, DocumentKind.MATRIX, DocumentKind.LIBRARY);

			return list;
		}
	}
}
