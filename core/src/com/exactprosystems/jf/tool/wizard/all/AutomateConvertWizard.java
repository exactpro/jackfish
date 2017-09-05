package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.app.ApplicationResize;
import com.exactprosystems.jf.api.app.Resize;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
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
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@WizardAttribute(
		name = "Universal automate wizard",
		pictureName = "AutomateConverterWizard.png",
		category = WizardCategory.CONFIGURATION,
		shortDescription = "This wizard can help users automatically change something.",
		experimental = false,
		strongCriteries = true,
		criteries = {Configuration.class},
		detailedDescription ="{{`This wizard can help users automatically change matrix, dictionaries and etc, when added new features.`}}"
				+"{{`User need select one of available converters from the {{$combobox$}}`}}"
				+"{{`On {{$area$}} below under combobox will show short description about the converter`}}"
				+"{{`After that, user need click the button {{$Scan$}} and wait, until wizard will find documents`}}"
				+"{{`And after that, user can accept changes by pushing the button Accept or refused by pushing the button Refused`}}"
)
public class AutomateConvertWizard extends AbstractWizard
{
	private Configuration configuration;

	private ListView<Refactor>  listView;
	private ComboBox<Converter> comboBox;

	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Service<List<Refactor>> service;

	private final List<Converter> CONVERTER_LIST = Arrays.asList(
			new ApplicationResizeConverter()
	);

	@Override
	public void init(IContext context, WizardManager wizardManager, Object... parameters)
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

		Button btnScan = new Button("Scan");

		TextArea shortDescription = new TextArea("Select converter from the above combobox");
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
		progressBox.getChildren().addAll(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS), new Label("Scanning..."));
		scanPane.add(progressBox, 0, 0);
		progressBox.setVisible(false);

		btnScan.setOnAction(e -> {
			Converter converter = this.comboBox.getSelectionModel().getSelectedItem();
			if (converter != null)
			{
				if (this.service != null)
				{
					this.service.cancel();
					if (this.executor != null)
					{
						this.executor.shutdownNow();
					}
					this.executor = null;
					this.service = null;
				}
				this.listView.getItems().clear();

				this.service= new Service<List<Refactor>>()
				{
					@Override
					protected Task<List<Refactor>> createTask()
					{
						return new Task<List<Refactor>>()
						{
							@Override
							protected List<Refactor> call() throws Exception
							{
								progressBox.setVisible(true);
								btnScan.setDisable(true);
								return converter.scan();
							}
						};
					}
				};
				this.service.setOnSucceeded(event ->
				{
					this.listView.getItems().setAll(((List<Refactor>) event.getSource().getValue()));
					progressBox.setVisible(false);
					btnScan.setDisable(false);
				});
				this.service.setOnFailed(event ->
				{
					DialogsHelper.showError(event.getSource().getException().getMessage());
					progressBox.setVisible(false);
					btnScan.setDisable(false);
				});
				this.executor = Executors.newSingleThreadExecutor();
				this.service.setExecutor(executor);
				this.service.start();
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
		if (this.service != null && this.service.isRunning() && this.executor != null)
		{
			this.executor.shutdownNow();
		}
		super.onRefused();
	}

	interface Converter
	{
		String shortDescription();
		List<Refactor> scan();
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
			return "It converting the action ApplicationResize from old to new format.\n"
					+ "Old action contains 3 fields : Maximize, Minimize and Normal.\n"
					+ "User can set several parameters and it will throw exception.\n"
					+ "In new format user can't do it, because these parameters replaced to one new parameter : Resize\n"
					+ "Example.\n"
					+ "Old action :\n"
					+ "    #Action;$AppConnection;$Maximize\n"
					+ "    ApplicationResize;CALL_7;true\n"
					+ "will converted to \n"
					+ "    #Action;$AppConnection;$Resize\n"
					+ "    ApplicationResize;Resize.Maximize\n"
			;
		}

		@Override
		public List<Refactor> scan()
		{
			List<Refactor> list = new ArrayList<>();
			configuration.forEach(document ->
			{
				Matrix matrix = (Matrix) document;
				MatrixItem root = matrix.getRoot();
				root.bypass(item ->
				{
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
}
