package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.actions.gui.DialogCheckLayout;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.WizardMatcher;
import com.exactprosystems.jf.tool.wizard.related.ConnectionBean;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.WizardCommonHelper;
import com.exactprosystems.jf.tool.wizard.related.WizardLoader;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.w3c.dom.Document;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@WizardAttribute(
		name = "LayoutWizard",
		pictureName = "LayoutWizard.png",
		category = WizardCategory.MATRIX,
		shortDescription = "This wizard creates RawTable and action DialogCheckLayout",
		strongCriteries = true,
		experimental = true,
		criteries = {MatrixFx.class, MatrixItem.class},
		detailedDescription = "{{`First of all you need to select one of stored connection and dialog.`}}"
				+ "{{`For store connection user may use {{$ConnectionWizard$}} or store the connection inside a matrix.`}}"
				+ "{{`In top left corner will appear image of selected dialog.`}}"
				+ "{{`Then you need to select one or more controls in the list or the image.`}}"
				+ "{{`After it you may check options under image:`}}"
				+ "{{`CheckBox {{$Number$}} - will generate DoSpec function with number. E.g. {{$DoSpec.width(10)$}}.`}}"
				+ "{{`CheckBox {{$About$}} - will generate DoSpec function with function about. Boundary values from 90% to 110%. E.g. {{$DoSpec.width(about(10))$}}.`}}"
				+ "{{`CheckBox {{$Less$}} - will generate DoSpec function with function less. E.g. {{$DoSpec.width(less(11))$}}.`}}"
				+ "{{`CheckBox {{$Great$}} - will generate DoSpec function with function great. E.g. {{$DoSpec.width(great(9))$}}.`}}"
				+ "{{`CheckBox {{$Between$}} - will generate DoSpec function with function great.Boundary values from 0% to 200% E.g. {{$DoSpec.width(between(0, 20))$}}.`}}"
				+ "{{`For selected controls will be generated the table with relations between controls.`}}"
				+ "{{`User can see created relation by switching a toggle button at intersection controls.`}}"
				+ "{{`User can edit created relation (add/remove/edit functions) and {{$save$}} it or {{$check$}}.`}}"
				+ "{{`User can check whole table by pushing the {{$Check table$}} button.`}}"
				+ "{{`The check result will appear in the area at the bottom of the wizard.`}}"
				+ "{{`After press 'Accept' button, in the matrix will be added 2 items - {{$RowTable$}} with relations and action {{$DialogCheckLayout$}} with link on the RowTable`}}"
)
public class LayoutWizard extends AbstractWizard
{
	private Matrix          matrix;
	private MatrixItem      item;
	private AppConnection   appConnection;
	private WizardMatcher   wizardMatcher;
	private WizardLoader    wizardLoader;
	private IWindow         currentWindow;
	private ExecutorService scanExecutor;
	private ExecutorService checkTableExecutor;
	private ExecutorService checkRelationExecutor;

	private Table table;

	private GridPane main;
	private Text waitText;
	
	private ComboBox<ConnectionBean> cbConnections;
	private ComboBox<IWindow>        cbDialogs;

	private ImageViewWithScale imageViewWithScale;
	private ListView<IControlWithCheck> lvControls;
	private TextArea errorArea;

	private Button            btnCheckTable;
	private ProgressIndicator piCheckTable;
	private GridPane          checkGrid;
	private Button            btnScan;
	private BorderPane        bpView;
	private AbstractEvaluator evaluator;

	private CheckBox cbNumber;
	private CheckBox cbLess;
	private CheckBox cbGreat;
	private CheckBox cbAbout;
	private CheckBox cbBetween;
	private ToggleGroup toggleGroup = new ToggleGroup();

	private HBox boxWithCheckBoxes;

	private Map<PieceKind, String> map = new HashMap<>(); //TODO remake this map to <PieceKind, Icon>

	//region AbstractWizard methods
	@Override
	public void init(IContext context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);
		this.matrix = super.get(MatrixFx.class, parameters);
		this.item = super.get(MatrixItem.class, parameters);
		this.evaluator = this.matrix.getFactory().createEvaluator();
	}

	@Override
	protected void onRefused()
	{
		Optional.ofNullable(this.wizardLoader).ifPresent(WizardLoader::stop);
		Optional.ofNullable(this.scanExecutor).ifPresent(WizardCommonHelper::shutdownExec);
		Optional.ofNullable(this.checkTableExecutor).ifPresent(WizardCommonHelper::shutdownExec);
		Optional.ofNullable(this.checkRelationExecutor).ifPresent(WizardCommonHelper::shutdownExec);

		super.onRefused();
	}

	/*
	main pane
	1 row - combobox x2 ( app and dialogs)  32.0 px
	2 row - image and listview (elements)	(height - 32 * 2 - 4 * 8 - 48) / 2  // 32 - 1 and 3 rows, 8 - grid gap
	3 row - checkboxes and btn scan			32.0 px
	4 row - grid and view					(height - 32 * 2 - 4 * 8 - 48) / 2
	5 row - errors ( if presents)			96px
	*/
	@Override
	protected void initDialog(BorderPane borderPane)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double height = screenSize.getHeight() - 150;
		borderPane.setMinHeight(height);
		borderPane.setPrefHeight(height);

		borderPane.setMinWidth(Math.min(1000.0, screenSize.width));
		borderPane.setPrefWidth(Math.min(1000.0, screenSize.width));

		this.cbConnections = new ComboBox<>();
		this.cbDialogs = new ComboBox<>();
		this.cbDialogs.setDisable(true);
		this.imageViewWithScale = new ImageViewWithScale();
		this.imageViewWithScale.addShowGrid();
		this.lvControls = new ListView<>();
		this.lvControls.setOnKeyPressed(event -> {
			IControlWithCheck selectedItem = this.lvControls.getSelectionModel().getSelectedItem();
			if (selectedItem != null && event.getCode() == KeyCode.SPACE)
			{
				selectedItem.toggle();
			}
		});
		this.lvControls.setCellFactory(CheckBoxListCell.forListView(IControlWithCheck::onProperty));
		this.btnScan = new Button("Scan");
		this.btnCheckTable = new Button("Check table");
		this.btnCheckTable.setDisable(true);
		this.btnCheckTable.setOnAction(e -> this.checkTable());
		this.piCheckTable = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		this.piCheckTable.setMinSize(32.0, 32.0);
		this.piCheckTable.setPrefSize(32.0, 32.0);
		this.piCheckTable.setMaxSize(32.0, 32.0);
		this.piCheckTable.setVisible(false);
		this.waitText = new Text("Select connection and dialog");

		this.main = new GridPane();
		this.main.getStyleClass().addAll(CssVariables.HGAP_MID, CssVariables.VGAP_MID);

		this.checkGrid = new GridPane();
		this.checkGrid.setGridLinesVisible(true);
		ScrollPane sp = new ScrollPane();
		sp.setContent(this.checkGrid);
		sp.setFitToWidth(true);
		sp.setFitToHeight(true);
		this.main.add(sp, 0, 3);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(70.0);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(30.0);
		this.main.getColumnConstraints().addAll(c0, c1);

		final double smallRow = 32.0;
		final double errorRow = 96.0;
		Supplier<RowConstraints> createSmallRow = () -> {
			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(smallRow);
			r0.setMaxHeight(smallRow);
			r0.setPrefHeight(smallRow);
			return r0;
		};

		Supplier<RowConstraints> createBigRow = () -> {
			RowConstraints r1 = new RowConstraints();
			r1.setMinHeight((height - smallRow * 2 - 4 * 8 - errorRow) / 2);
			r1.setPrefHeight((height - smallRow * 2 - 4 * 8 - errorRow) / 2);
			r1.setVgrow(Priority.SOMETIMES);
			r1.setPercentHeight(-1);
			return r1;
		};

		//region image and lv
		{
			this.main.getRowConstraints().addAll(createSmallRow.get(), createBigRow.get());

			HBox connectionBox = new HBox();
			connectionBox.getChildren().addAll(new Label("Connection : "), this.cbConnections);
			this.cbConnections.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(this.cbConnections, Priority.ALWAYS);
			connectionBox.setAlignment(Pos.CENTER_LEFT);

			HBox dialogBox = new HBox();
			dialogBox.getChildren().addAll(new Label("Dialog : "), this.cbDialogs);
			this.cbDialogs.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(this.cbDialogs, Priority.ALWAYS);
			dialogBox.setAlignment(Pos.CENTER_RIGHT);

			this.main.add(connectionBox, 0, 0);
			this.main.add(dialogBox, 1, 0);

			this.main.add(this.waitText, 0, 1);
			GridPane.setHalignment(this.waitText, HPos.CENTER);
			this.main.add(this.lvControls, 1, 1);
		}
		//endregion

		//region scan and checkboxes
		{
			this.main.getRowConstraints().addAll(createSmallRow.get());

			this.boxWithCheckBoxes = new HBox();
			HBox cbBoxes = new HBox();

			this.cbNumber = new CheckBox("Number");
			this.cbNumber.setSelected(true); // default value
			this.cbAbout = new CheckBox("About");
			this.cbLess = new CheckBox("Less");
			this.cbGreat = new CheckBox("Great");
			this.cbBetween = new CheckBox("Between");

			cbBoxes.getChildren().addAll(this.cbNumber, Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.cbAbout, Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.cbLess, Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.cbGreat, Common.createSpacer(Common.SpacerEnum.HorizontalMin), this.cbBetween);

			this.boxWithCheckBoxes.getChildren().addAll(cbBoxes, this.btnScan);
			HBox.setHgrow(cbBoxes, Priority.ALWAYS);
			this.boxWithCheckBoxes.setAlignment(Pos.CENTER_LEFT);
			this.main.add(this.boxWithCheckBoxes, 0, 2, 2, 1);
		}
		//endregion

		//region table
		{
			this.main.getRowConstraints().addAll(createBigRow.get());
			HBox pane = new HBox();
			pane.setAlignment(Pos.CENTER);
			pane.getChildren().addAll(this.btnCheckTable, this.piCheckTable);
			this.checkGrid.add(pane, 0, 0);

			this.bpView = new BorderPane();
			this.main.add(this.bpView, 1, 3);
		}
		//endregion

		//region errors
		{
			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(errorRow - 4 * 8);
			r0.setPrefHeight(errorRow  - 4 * 8);
			r0.setVgrow(Priority.SOMETIMES);
			r0.setPercentHeight(-1);

			this.errorArea = new TextArea();
			this.errorArea.setEditable(false);

			this.main.getRowConstraints().addAll(r0);

			this.main.add(this.errorArea, 0, 4, 2, 1);

		}
		//endregion

		borderPane.setCenter(this.main);

		this.cbConnections.getItems().setAll(WizardCommonHelper.getAllConnections(this.matrix.getFactory().getConfiguration()));

		this.btnScan.setDisable(true);
		this.boxWithCheckBoxes.setDisable(true);
		hideTableAndView();
		listeners();

		fillMap();
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		ArrayList<Spec> list = new ArrayList<>();
		this.forEachRelationButton(rb -> list.add(rb.formula));

		ArrayList<String> headers = new ArrayList<>();
		this.<TopText>forEach(node -> node instanceof TopText, topText -> headers.add(topText.getText()));

		String[][] lines = new String[headers.size() + 1][headers.size() + 1];
		lines[0][0] = "";
		for (int i = 1; i < lines[0].length; i++)
		{
			String header = headers.get(i - 1);
			lines[0][i] = header;
			lines[i][0] = header;
		}
		Iterator<Spec> iterator = list.iterator();
		for (int i = 1; i < lines.length; i++)
		{
			for (int j = 1; j < lines[i].length; j++)
			{
				lines[j][i] = iterator.next().toString();
			}
		}
		CommandBuilder builder = CommandBuilder.start();
		MatrixItem rowTable = createRawTable(lines);
		MatrixItem dialogCheckLayout = createDialogCheckLayout(rowTable.getId());
		int index = this.item.getParent().index(this.item);
		builder.addMatrixItem(this.matrix, this.item.getParent(), rowTable, index);
		builder.addMatrixItem(this.matrix, this.item.getParent(), dialogCheckLayout, index + 1);
		return builder::build;
	}

	@Override
	public boolean beforeRun()
	{
		return true;
	}
	//endregion

	private void fillMap()
	{
		this.addToMap("V", PieceKind.VISIBLE); 					// v - visible
		this.addToMap("C", PieceKind.COUNT);   					// c - count
		this.addToMap("S", PieceKind.WIDTH, PieceKind.HEIGHT);	// S - size
		this.addToMap("C", PieceKind.CONTAINS);					// c - contains
		this.addToMap("D", PieceKind.LEFT, PieceKind.RIGHT, PieceKind.TOP, PieceKind.BOTTOM); // D - distance
		this.addToMap("I", PieceKind.INSIDE_LEFT, PieceKind.INSIDE_RIGHT, PieceKind.INSIDE_TOP, PieceKind.INSIDE_BOTTOM); // I - inside
		this.addToMap("O", PieceKind.ON_LEFT, PieceKind.ON_RIGHT, PieceKind.ON_TOP, PieceKind.ON_BOTTOM); // O - on
		this.addToMap("A", PieceKind.LEFT_ALIGNED, PieceKind.RIGHT_ALIGNED, PieceKind.TOP_ALIGNED, PieceKind.BOTTOM_ALIGNED); //A - align
		this.addToMap("R", PieceKind.HORIZONTAL_CENTERED, PieceKind.VERTICAL_CENTERED); // R - centeRed
	}

	private void addToMap(String word, PieceKind ... kinds)
	{
		for (PieceKind kind : kinds)
		{
			map.put(kind, word);
		}
	}

	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	private void hideTableAndView()
	{
		this.errorArea.clear();
		this.checkGrid.getRowConstraints().clear();
		this.checkGrid.getColumnConstraints().clear();
		this.checkGrid.getChildren().removeIf(node -> node instanceof RelationButton || node instanceof Text);

		this.checkGrid.setVisible(false);
		this.bpView.getChildren().clear();
	}

	private MatrixItem createRawTable(String[][] lines)
	{
		MatrixItem rawTable = CommandBuilder.create(this.matrix, RawTable.class.getSimpleName(), Table.class.getSimpleName());
		Stream.of(lines).forEach(rawTable::processRawData);
		rawTable.createId();
		return rawTable;
	}

	private MatrixItem createDialogCheckLayout(String tableId)
	{
		MatrixItem dcl = CommandBuilder.create(this.matrix, Tokens.Action.get(), DialogCheckLayout.class.getSimpleName());
		Parameters parameters = new Parameters();
		parameters.add(DialogCheckLayout.connectionName, "", TypeMandatory.Mandatory);
		parameters.add(DialogCheckLayout.dialogName, evaluator.createString(this.cbDialogs.getSelectionModel().getSelectedItem().getName()), TypeMandatory.Mandatory);
		parameters.add(DialogCheckLayout.tableName, tableId, TypeMandatory.NotMandatory);
		dcl.addKnownParameters();
		try
		{
			dcl.init(this.matrix, null, new HashMap<>(), parameters);
		}
		catch (Exception e)
		{
			;
		}
		dcl.createId();
		return dcl;
	}

	//region work with console
	private void displayResults(List<String> errorList)
	{
		if (errorList == null || errorList.isEmpty())
		{
			removeAndSet(CssVariables.EVALUATE_SUCCESS);
			this.errorArea.setText("All ok");
		}
		else
		{
			removeAndSet(CssVariables.COMPILE_FAILED);
			this.errorArea.setText(errorList.stream().collect(Collectors.joining(System.lineSeparator())));
		}
	}

	private void displayErrors(String error)
	{
		removeAndSet(CssVariables.COMPILE_FAILED);
		this.errorArea.setText(error);
	}

	private void checking()
	{
		removeAndSet(CssVariables.EVALUATE_FAILED);
		this.errorArea.setText("Checking...");
	}

	private void removeAndSet(String styleClass)
	{
		this.errorArea.getStyleClass().removeAll(CssVariables.COMPILE_FAILED, CssVariables.EVALUATE_FAILED, CssVariables.EVALUATE_SUCCESS);
		this.errorArea.getStyleClass().addAll(styleClass);
	}
	//endregion

	//region private scan functions
	private void listeners()
	{
		this.cbConnections.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				this.appConnection = newValue.getConnection();
				this.cbDialogs.getItems().setAll(this.appConnection.getDictionary().getWindows());
				this.cbDialogs.setDisable(false);

			}
			else
			{
				this.cbDialogs.getItems().clear();
				this.cbDialogs.setDisable(true);
			}
		});

		this.cbDialogs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newDialog) ->
		{
			hideTableAndView();
			if (newDialog != null)
			{
				this.imageViewWithScale.removeCurrentImage();
				boolean removeIf = this.main.getChildren().removeIf(node -> node == this.waitText);
				if (removeIf)
				{
					this.main.add(this.imageViewWithScale, 0, 1);
				}
				this.lvControls.getItems().clear();

				PluginInfo info = this.appConnection.getApplication().getFactory().getInfo();
				this.wizardMatcher = new WizardMatcher(info);

				this.wizardLoader = new WizardLoader(this.appConnection, newDialog.getSelfControl(), (image, doc) ->
				{
					Collection<IControl> controls = newDialog.getControls(IWindow.SectionKind.Run);
					this.lvControls.getItems().setAll(
							controls.stream()
									.filter(c -> !Str.IsNullOrEmpty(c.getID()))
									.map(c -> new IControlWithCheck(c, doc))
									.filter(c -> Objects.nonNull(c.getRectangle()))
									.collect(Collectors.toList())
					);

					this.lvControls.getItems().forEach(ic -> ic.onProperty().addListener((observable1, oldValue1, newValue) ->
					{
						Rectangle rectangle = ic.getRectangle();
						if (rectangle == null)
						{
							return;
						}
						if (newValue)
						{
							this.imageViewWithScale.showRectangle(rectangle, MarkerStyle.MARK, "", true);
						}
						else
						{
							this.imageViewWithScale.hideRectangle(rectangle, MarkerStyle.MARK);
						}
					}));

					this.imageViewWithScale.displayImage(image);

					List<Rectangle> list = XpathUtils.collectAllRectangles(doc);
					this.imageViewWithScale.setListForSearch(list);

					this.imageViewWithScale.setOnRectangleClick(rectangle -> this.lvControls.getItems()
							.stream()
							.filter(entry -> rectangle.equals(entry.getRectangle()))
							.findFirst()
							.map(IControlWithCheck::toggle)
							.ifPresent(isToggle ->
							{
								if (isToggle)
								{
									this.imageViewWithScale.showRectangle(rectangle, MarkerStyle.MARK, "", true);
								}
								else
								{
									this.imageViewWithScale.hideRectangle(rectangle, MarkerStyle.MARK);
								}
								this.lvControls.refresh();
							}));
				}
				, ex ->
				{
					String message = ex.getMessage();
					if (ex.getCause() instanceof JFRemoteException)
					{
						message = ((JFRemoteException) ex.getCause()).getErrorKind().toString();
					}
					DialogsHelper.showError(message);
				});
				this.wizardLoader.start();
				this.currentWindow = newDialog;
				this.btnScan.setDisable(false);
				this.boxWithCheckBoxes.setDisable(false);
			}
			else
			{
				this.lvControls.getItems().clear();
			}
		});

		this.btnScan.setOnAction(e -> this.scan());
	}

	private void scan()
	{
		if (this.lvControls.getItems().stream().noneMatch(c -> c.onProperty().getValue()))
		{
			DialogsHelper.showInfo("Select more than zero elements from listView above");
			return;
		}
		hideTableAndView();

		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		ProgressIndicator indicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		indicator.setMinSize(64.0, 64.0);

		box.getChildren().addAll(indicator, new Text("Creating table..."));

		this.checkGrid.setVisible(false);
		this.main.add(box, 0, 3);

		Optional.ofNullable(this.scanExecutor).ifPresent(WizardCommonHelper::shutdownExec);
		this.scanExecutor = Executors.newSingleThreadExecutor();
		Consumer<Boolean> setDisable = flag ->
		{
			this.btnCheckTable.setDisable(flag);
			this.cbConnections.setDisable(flag);
			this.cbDialogs.setDisable(flag);
			this.btnScan.setDisable(flag);
			this.boxWithCheckBoxes.setDisable(flag);
		};
		setDisable.accept(true);

		errorArea.clear();
		Service<List<RelationButton>> service = new Service<List<RelationButton>>()
		{
			@Override
			protected Task<List<RelationButton>> createTask()
			{
				return new Task<List<RelationButton>>()
				{
					@Override
					protected List<RelationButton> call() throws Exception
					{
						return scan0();
					}
				};
			}
		};
		service.setOnSucceeded(e ->
		{
			setDisable.accept(false);
			this.main.getChildren().removeIf(node -> node == box);
			this.checkGrid.setGridLinesVisible(true);
			this.checkGrid.setVisible(true);

			List<RelationButton> list = (List<RelationButton>) e.getSource().getValue();

			//list.size always is n^2
			int collectSqrt = (int)Math.sqrt(list.size());
			for (int i = 0; i < list.size(); i++)
			{
				RelationButton relationButton = list.get(i);
				this.checkGrid.add(relationButton, i / collectSqrt + 1, i % collectSqrt + 1);
			}
		});
		service.setOnFailed(e ->
		{
			setDisable.accept(false);
			this.main.getChildren().removeIf(node -> node == box);
			this.checkGrid.setVisible(false);
			displayErrors(e.getSource().getException().getMessage());
		});
		service.setExecutor(scanExecutor);
		service.start();
	}

	private List<RelationButton> scan0() throws Exception
	{
		List<IControl> collect = this.lvControls.getItems()
				.stream()
				.filter(c -> c.onProperty().getValue())
				.map(c -> c.control)
				.collect(Collectors.toList());

		String longestId = collect.stream().map(IControl::getID).filter(id -> !Str.IsNullOrEmpty(id)).max(Comparator.comparingInt(String::length)).orElse("");

		IntStream.range(0, collect.size() +1)
				.forEach(i -> {
					RowConstraints r0 = new RowConstraints();
					r0.setPercentHeight(-1);
					r0.setVgrow(Priority.ALWAYS);
					r0.setMaxHeight(32.0);

					ColumnConstraints c0 = new ColumnConstraints();
					c0.setHalignment(HPos.CENTER);
					c0.setPercentWidth(-1);
					if (i > 0)
					{
						String id = collect.get(i - 1).getID();
						double computeWidth = Common.computeTextWidth(Font.getDefault(), id, 0.0D);
						c0.setMinWidth(Math.max(100, computeWidth));
						c0.setPrefWidth(Math.max(100, computeWidth));
					}
					else
					{
						//first column
						double computeWidth = Common.computeTextWidth(Font.getDefault(), longestId, 0.0D);
						c0.setMinWidth(Math.max(150.0, computeWidth));
						c0.setPrefWidth(Math.max(150.0, computeWidth));
					}

					this.checkGrid.getColumnConstraints().add(c0);
					this.checkGrid.getRowConstraints().add(r0);
				});

		Platform.runLater(() -> IntStream.rangeClosed(1, collect.size())
				.forEach(i ->
				{
					String id = collect.get(i - 1).getID();
					this.checkGrid.add(new TopText(id), 0, i);
					this.checkGrid.add(new Text(id), i, 0);
				}));

		List<RelationButton> list = new ArrayList<>();

		for (IControl top : collect)
		{
			for (IControl left : collect)
			{
				list.add(this.createRelation(top, left));
			}
		}
		return list;
	}

	private RelationButton createRelation(IControl top, IControl left)
	{
		RelationButton btn = new RelationButton(createFormula(top, left), top, left);
		btn.setToggleGroup(this.toggleGroup);
		btn.setOnAction(e -> this.bpView.setCenter(btn.createView()));
		return btn;
	}

	private Spec createFormula(IControl topControl, IControl leftControl)
	{
		String leftId = leftControl.getID();
		Rectangle top = null;
		Rectangle left = null;

		try
		{
			IControl topOwner = this.currentWindow.getOwnerControl(topControl);
			top = service().getRectangle(topOwner == null ? null : topOwner.locator(), topControl.locator());

			if (topControl == leftControl)
			{
				left = new Rectangle(top);
			}
			else
			{
				IControl leftOwner = this.currentWindow.getOwnerControl(leftControl);
				left = service().getRectangle(leftOwner == null ? null : leftOwner.locator(), leftControl.locator());
			}
		}
		catch (Exception e)
		{
			return Spec.create().invisible();
		}
		//same control
		Spec spec = Spec.create();
		if (topControl == leftControl)
		{
			spec.visible().count(1);
			addSpecs(top.getHeight(), spec::height, spec::height);
			addSpecs(top.getWidth(), spec::width, spec::width);

			return spec;
		}
		else
		{
			//check full contains
			if (!(top.x > left.x || (top.x + top.width) < (left.x + left.width) || top.y > left.y || (top.y + top.height) < (left.y + left.height)))
			{
				spec.contains(leftId);
			}

			addSpecsAnother(PieceKind.LEFT, top, left, leftId, spec::left, spec::left);
			addSpecsAnother(PieceKind.RIGHT, top, left, leftId, spec::right, spec::right);
			addSpecsAnother(PieceKind.TOP, top, left, leftId, spec::top, spec::top);
			addSpecsAnother(PieceKind.BOTTOM, top, left, leftId, spec::bottom, spec::bottom);

			addSpecsAnother(PieceKind.INSIDE_LEFT, top, left, leftId, spec::inLeft, spec::inLeft);
			addSpecsAnother(PieceKind.INSIDE_RIGHT, top, left, leftId, spec::inRight, spec::inRight);
			addSpecsAnother(PieceKind.INSIDE_TOP, top, left, leftId, spec::inTop, spec::inTop);
			addSpecsAnother(PieceKind.INSIDE_BOTTOM, top, left, leftId, spec::inBottom, spec::inBottom);

			addSpecsAnother(PieceKind.ON_LEFT, top, left, leftId, spec::onLeft, spec::onLeft);
			addSpecsAnother(PieceKind.ON_RIGHT, top, left, leftId, spec::onRight, spec::onRight);
			addSpecsAnother(PieceKind.ON_TOP, top, left, leftId, spec::onTop, spec::onTop);
			addSpecsAnother(PieceKind.ON_BOTTOM, top, left, leftId, spec::onBottom, spec::onBottom);

			addSpecsAnother(PieceKind.LEFT_ALIGNED, top, left, leftId, spec::lAlign, spec::lAlign);
			addSpecsAnother(PieceKind.RIGHT_ALIGNED, top, left, leftId, spec::rAlign, spec::rAlign);
			addSpecsAnother(PieceKind.TOP_ALIGNED, top, left, leftId, spec::tAlign, spec::tAlign);
			addSpecsAnother(PieceKind.BOTTOM_ALIGNED, top, left, leftId, spec::bAlign, spec::bAlign);

			addSpecsAnother(PieceKind.HORIZONTAL_CENTERED, top, left, leftId, spec::hCenter, spec::hCenter);
			addSpecsAnother(PieceKind.VERTICAL_CENTERED, top, left, leftId, spec::vCenter, spec::vCenter);
		}
		return spec;
	}

	private void addSpecs(Number n, Consumer<Number> c0, Consumer<CheckProvider> c1)
	{
		if (this.cbNumber.isSelected())
		{
			c0.accept(n);
		}
		if (this.cbAbout.isSelected())
		{
			c1.accept(DoSpec.about(n));
		}
		if (this.cbLess.isSelected())
		{
			c1.accept(DoSpec.less(n.longValue() + 1));
		}
		if (this.cbGreat.isSelected())
		{
			c1.accept(DoSpec.great(n.longValue() - 1));
		}
		if (this.cbBetween.isSelected())
		{
			c1.accept(DoSpec.between(0, n.longValue() * 2));
		}
	}

	private void addSpecsAnother(Number n, String another, BiConsumer<String, Number> c0, BiConsumer<String, CheckProvider> c1)
	{
		if (this.cbNumber.isSelected())
		{
			c0.accept(another, n);
		}
		if (this.cbAbout.isSelected())
		{
			c1.accept(another, DoSpec.about(n));
		}
		if (this.cbLess.isSelected())
		{
			c1.accept(another, DoSpec.less(n.longValue() + 1));
		}
		if (this.cbGreat.isSelected())
		{
			c1.accept(another, DoSpec.great(n.longValue() - 1));
		}
		if (this.cbBetween.isSelected())
		{
			c1.accept(another, DoSpec.between(0, n.longValue() * 2));
		}
	}

	private void addSpecsAnother(PieceKind kind, Rectangle top, Rectangle left, String leftId, BiConsumer<String, Number> c0, BiConsumer<String, CheckProvider> c1)
	{
		int distance;
		if ((distance = kind.distance(top, left)) > 0)
		{
			addSpecsAnother(distance, leftId, c0, c1);
		}
	}

	private void checkTable()
	{
		Consumer<Boolean> setDisable = flag ->
		{
			this.bpView.setDisable(flag);
			this.cbDialogs.setDisable(flag);
			this.cbConnections.setDisable(flag);
			this.btnScan.setDisable(flag);
			this.piCheckTable.setVisible(flag);
			this.btnCheckTable.setDisable(flag);
			forEachRelationButton(rb -> rb.setDisable(flag));
		};
		setDisable.accept(true);
		Optional.ofNullable(this.checkTableExecutor).ifPresent(WizardCommonHelper::shutdownExec);
		this.checkTableExecutor = Executors.newSingleThreadExecutor();
		Service<List<String>> service = new Service<List<String>>()
		{
			@Override
			protected Task<List<String>> createTask()
			{
				return new Task<List<String>>()
				{
					@Override
					protected List<String> call() throws Exception
					{
						ArrayList<String> list = new ArrayList<>();
						checking();
						forEachRelationButton(rb -> {
							List<String> check = rb.check();
							Optional.ofNullable(check).ifPresent(list::addAll);
						});
						return list;
					}
				};
			}
		};
		service.setOnSucceeded(event ->
		{
			setDisable.accept(false);
			List<String> value = ((List<String>) event.getSource().getValue());
			this.displayResults(value);
		});
		service.setOnFailed(event ->
		{
			setDisable.accept(false);
			this.displayErrors(event.getSource().getException().getMessage());
			event.getSource().getException().printStackTrace();
		});
		service.setExecutor(this.checkTableExecutor);
		service.start();
	}

	private void forEachRelationButton(Consumer<RelationButton> consumer)
	{
		this.checkGrid.getChildren().stream()
				.filter(node -> node instanceof RelationButton)
				.map(node -> (RelationButton)node)
				.forEach(consumer);
	}

	private <T extends Node> void forEach(Predicate<Node> predicate, Consumer<T> consumer)
	{
		this.checkGrid.getChildren().stream()
				.filter(predicate)
				.map(node -> (T) node)
				.forEach(consumer);
	}


	//endregion

	//region private classes
	private class TopText extends Text
	{
		public TopText(String text)
		{
			super(text);
		}
	}

	private class RelationButton extends ToggleButton
	{
		private Spec formula;
		private String topName;
		private String leftName;
		private IControl control;
		private VBox boxWithFields;

		public RelationButton(Spec formula, IControl topControl, IControl leftControl)
		{
			super();
			this.setMaxHeight(Double.MAX_VALUE);
			this.setMaxWidth(Double.MAX_VALUE);

			this.topName = topControl.getID();
			this.leftName = leftControl.getID();
			setFormula(formula);
			this.control = topControl;
		}

		public Node createView()
		{
			VBox main = new VBox();

			this.boxWithFields = new VBox();

			ScrollPane sp = new ScrollPane();

			Text e1 = new Text(String.format("Relation %s -> %s", topName, leftName));
			e1.setWrappingWidth(this.getScene().getWindow().getWidth() * 0.25);
			main.getChildren().add(e1);
			main.getChildren().add(new Separator(Orientation.HORIZONTAL));
			main.getChildren().add(new Text(DoSpec.class.getSimpleName()));

			Button btnAdd = new Button();

			Iterator<Piece> iterator = this.formula.iterator();
			Consumer<OneRow> removeAll = this.boxWithFields.getChildren()::removeAll;
			BiConsumer<KeyEvent, OneRow> keyConsumer = (keyEvent, oneRow) ->
			{
				KeyCode keyCode = keyEvent.getCode();
				if (keyCode == KeyCode.DOWN || (keyCode == KeyCode.ENTER && !keyEvent.isShiftDown()) || (keyCode == KeyCode.TAB && !keyEvent.isShiftDown()))
				{
					int i = this.boxWithFields.getChildren().indexOf(oneRow);
					if (i == this.boxWithFields.getChildren().size() - 2)
					{
						btnAdd.fire();
					}
					else
					{
						this.boxWithFields.getChildren().get(i + 1).requestFocus();
					}
				}
				else if (keyCode == KeyCode.UP || (keyCode == KeyCode.TAB && keyEvent.isShiftDown()) || (keyCode == KeyCode.TAB || keyEvent.isShiftDown()))
				{
					int i = this.boxWithFields.getChildren().indexOf(oneRow);
					if (i > 0)
					{
						this.boxWithFields.getChildren().get(i - 1).requestFocus();
					}
				}
			};
			BiConsumer<String, Boolean> addNew = (str,flag) -> {
				OneRow oneRow = new OneRow(str, evaluator, removeAll, keyConsumer);
				int index = Math.max(0, this.boxWithFields.getChildren().size() - 1);
				this.boxWithFields.getChildren().add(index, oneRow);
				if (flag)
				{
					oneRow.requestFocus();
				}
			};

			iterator.forEachRemaining(piece -> addNew.accept(piece.toString(), false));

			btnAdd.setId("cbAdd");
			btnAdd.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
			this.boxWithFields.getChildren().add(btnAdd);
			btnAdd.setOnAction(e -> addNew.accept("", true));

			sp.setFitToHeight(true);
			sp.setFitToWidth(true);
			sp.setContent(this.boxWithFields);
			main.getChildren().add(sp);
			VBox.setVgrow(sp, Priority.ALWAYS);

			Button btnCheck = new Button("Check");
			Button btnSave = new Button("Save");
			HBox checkBox = new HBox();
			checkBox.setAlignment(Pos.CENTER_RIGHT);
			checkBox.getChildren().add(btnCheck);
			HBox.setHgrow(checkBox, Priority.ALWAYS);

			ProgressIndicator indicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
			indicator.setMinSize(32, 32);
			indicator.setPrefSize(32, 32);
			indicator.setMaxSize(32, 32);
			checkBox.getChildren().add(indicator);
			indicator.setVisible(false);
			HBox hBox = new HBox();
			hBox.setAlignment(Pos.CENTER_LEFT);
			btnSave.setOnAction(e -> this.save());
			Consumer<Boolean> setDisable = flag -> {
				indicator.setVisible(flag);

				this.boxWithFields.setDisable(flag);
				btnSave.setDisable(flag);
				btnCheck.setDisable(flag);
				indicator.setVisible(flag);
				forEachRelationButton(rb -> rb.setDisable(flag));
			};
			btnCheck.setOnAction(e -> {
				setDisable.accept(true);
				checking();
				Service<List<String>> service = new Service<List<String>>()
				{
					@Override
					protected Task<List<String>> createTask()
					{
						return new Task<List<String>>()
						{
							@Override
							protected List<String> call() throws Exception
							{
								return checkView();
							}
						};
					}
				};
				Optional.ofNullable(checkRelationExecutor).ifPresent(WizardCommonHelper::shutdownExec);
				checkRelationExecutor = Executors.newSingleThreadExecutor();
				service.setExecutor(checkRelationExecutor);
				service.setOnSucceeded(ev ->
				{
					setDisable.accept(false);

					List<String> res = (List<String>) ev.getSource().getValue();
					displayResults(res);
				});
				service.setOnFailed(ev ->
				{
					Throwable exception = ev.getSource().getException();
					exception.printStackTrace();
					setDisable.accept(true);
					displayErrors(exception.getMessage());
				});
				service.start();
			});

			hBox.getChildren().addAll(btnSave, Common.createSpacer(Common.SpacerEnum.HorizontalMid), checkBox);
			main.getChildren().addAll(Common.createSpacer(Common.SpacerEnum.VerticalMid), hBox);
			return main;
		}

		private void save()
		{
			Spec func = this.create(piece -> DialogsHelper.showError(String.format("Can't save, because %s is invalid doSpec function", piece)));
			if (func != null)
			{
				setFormula(func);
			}
		}

		/**
		 * @return null if all ok, otherwise list of errors
		 */
		public List<String> checkView()
		{
			Spec func = this.create(piece -> DialogsHelper.showError(String.format("Can't check, because %s is invalid doSpec function", piece)));
			if (func != null)
			{
				try
				{
					CheckingLayoutResult res = this.control.checkLayout(service(), currentWindow, func);
					if (!res.isOk())
					{
						return res.getErrors();
					}
				}
				catch (Exception e)
				{
					return Collections.singletonList(e.getMessage());
				}
			}
			return null;
		}

		/**
		 * @return null if all ok, otherwise list of errors
		 */
		public List<String> check()
		{
			try
			{
				CheckingLayoutResult res = this.control.checkLayout(service(), currentWindow, this.formula);
				if (!res.isOk())
				{
					return res.getErrors();
				}
			}
			catch (Exception e)
			{
				return Collections.singletonList(e.getMessage());
			}
			return null;
		}

		private Spec create(Consumer<String> errorConsumer)
		{
			List<String> pieces = this.boxWithFields.getChildren()
					.stream()
					.filter(node -> node instanceof OneRow)
					.map(node -> (OneRow) node)
					.map(OneRow::getValue)
					.collect(Collectors.toList());

			StringBuilder doSpecString = new StringBuilder(DoSpec.class.getSimpleName());
			Spec func = null;
			for (String piece : pieces)
			{
				if (piece.isEmpty())
				{
					continue;
				}
				doSpecString.append(piece);
				boolean needStop = false;
				try
				{
					Object evaluate = evaluator.evaluate(doSpecString.toString());
					if (!(evaluate instanceof Spec))
					{
						needStop = true;
					}
					else
					{
						func = (Spec) evaluate;
					}
				}
				catch (Exception e)
				{
					needStop = true;
				}
				if (needStop)
				{
					errorConsumer.accept(piece);
					return null;
				}
			}
			return func;
		}

		private void setFormula(Spec formula)
		{
			//TODO add icons on button ( icon of pieces)
			this.formula = formula;
			Iterator<Piece> iterator = this.formula.iterator();
			Set<String> set = new HashSet<>();
			iterator.forEachRemaining(piece ->
			{
				set.add(map.get(piece.getKind()));
			});
			this.setText(set.stream().collect(Collectors.joining("")));
		}
	}

	private static class OneRow extends HBox
	{
		private TextField field;

		OneRow(String formula, AbstractEvaluator evaluator, Consumer<OneRow> handler, BiConsumer<KeyEvent, OneRow> consumer)
		{
			this.setAlignment(Pos.CENTER_LEFT);
			this.field = new TextField(formula);
			HBox.setHgrow(this.field, Priority.ALWAYS);
			this.field.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!Str.IsNullOrEmpty(newValue))
				{
					this.field.getStyleClass().removeAll(CssVariables.INCORRECT_FIELD);
					try
					{
						Object evaluate = evaluator.evaluate(DoSpec.class.getSimpleName() + newValue);
						if (!(evaluate instanceof Spec))
						{
							this.field.getStyleClass().add(CssVariables.INCORRECT_FIELD);
						}
					}
					catch (Exception e)
					{
						this.field.getStyleClass().add(CssVariables.INCORRECT_FIELD);
					}
				}
			});
			this.field.setMaxWidth(Double.MAX_VALUE);
			this.getChildren().add(this.field);

			Button btnRemove = new Button();
			btnRemove.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
			btnRemove.setId("btnRemove");
			btnRemove.setOnAction(e -> handler.accept(this));
			this.getChildren().add(btnRemove);
			this.field.setOnKeyPressed(e -> consumer.accept(e, this));
		}

		public void requestFocus()
		{
			Common.setFocused(this.field, 30);
		}

		public String getValue()
		{
			return this.field.getText();
		}
	}

	private class IControlWithCheck
	{
		private IControl control;
		private final BooleanProperty on = new SimpleBooleanProperty(false);
		private Rectangle rectangle;

		public IControlWithCheck(IControl control, Document doc)
		{
			this.control = control;
			try
			{
				List<org.w3c.dom.Node> all = wizardMatcher.findAll(doc, this.control.locator());
				this.rectangle = ((Rectangle) all.get(0).getUserData(IRemoteApplication.rectangleName));
			}
			catch (Exception e)
			{
				this.rectangle = null;
			}
		}

		final BooleanProperty onProperty()
		{
			return this.on;
		}

		public boolean toggle()
		{
			this.on.set(!this.on.getValue());
			return this.on.getValue();
		}

		public Rectangle getRectangle()
		{
			return this.rectangle;
		}

		@Override
		public String toString()
		{
			return this.control.getID();
		}
	}
	//endregion
}
