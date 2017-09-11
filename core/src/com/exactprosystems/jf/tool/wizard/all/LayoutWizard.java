package com.exactprosystems.jf.tool.wizard.all;

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
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
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
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.w3c.dom.Document;

import java.awt.Rectangle;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@WizardAttribute(
		name = "LayoutWizard",
		pictureName = "AutomateConverterWizard.png",
		category = WizardCategory.MATRIX,
		shortDescription = "Short description",
		strongCriteries = true,
		experimental = true,
		criteries = {MatrixFx.class, MatrixItem.class},
		detailedDescription = "Detailed description"
)
public class LayoutWizard extends AbstractWizard
{
	private Matrix matrix;
	private MatrixItem item;
	private AppConnection appConnection;
	private WizardMatcher wizardMatcher;
	private WizardLoader wizardLoader;
	private IWindow currentWindow;
	private ExecutorService executor;

	private Table table;

	private GridPane main;
	private Text waitText;
	
	private ComboBox<ConnectionBean> cbConnections;
	private ComboBox<IWindow>        cbDialogs;

	private ImageViewWithScale imageViewWithScale;
	private ListView<IControlWithCheck> lvControls;

	private Button            btnCheckTable;
	private GridPane          checkGrid;
	private Button            btnScan;
	private BorderPane        bpView;
	private AbstractEvaluator evaluator;

	private CheckBox cbNumber;
	private CheckBox cbLess;
	private CheckBox cbGreat;
	private CheckBox cbAbout;
	private CheckBox cbBetween;

	private HBox boxWithCheckBoxes;

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
		Optional.ofNullable(this.executor).ifPresent(ExecutorService::shutdownNow);
		super.onRefused();
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		borderPane.setMinHeight(800.0);
		borderPane.setPrefHeight(800.0);

		borderPane.setMinWidth(1000.0);
		borderPane.setPrefWidth(1000.0);

		this.cbConnections = new ComboBox<>();
		this.cbDialogs = new ComboBox<>();
		this.cbDialogs.setDisable(true);
		this.imageViewWithScale = new ImageViewWithScale();
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
		this.waitText = new Text("Select connection and dialog");

		this.main = new GridPane();
		this.main.getStyleClass().addAll(CssVariables.HGAP_MID, CssVariables.VGAP_MID);

		this.checkGrid = new GridPane();
		this.checkGrid.setGridLinesVisible(true);
		this.main.add(this.checkGrid, 0, 3);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(70.0);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(30.0);
		this.main.getColumnConstraints().addAll(c0, c1);

		//region image and lv
		{
			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(32.0);
			r0.setMaxHeight(32.0);
			r0.setPrefHeight(32.0);

			RowConstraints r1 = new RowConstraints();
			r1.setMinHeight((800 - 32 * 3 - 4 * 8) / 2);
			r1.setPrefHeight((800 - 32 * 3 - 4 * 8) / 2);
			r1.setVgrow(Priority.SOMETIMES);

			this.main.getRowConstraints().addAll(r0, r1);

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
			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(32.0);
			r0.setMaxHeight(32.0);
			r0.setPrefHeight(32.0);
			this.main.getRowConstraints().addAll(r0);

			this.boxWithCheckBoxes = new HBox();
			HBox cbBoxes = new HBox();

			this.cbNumber = new CheckBox("Number");
			this.cbAbout = new CheckBox("About");
			this.cbLess = new CheckBox("Less");
			this.cbGreat = new CheckBox("Great");
			this.cbBetween = new CheckBox("Between");

			cbBoxes.getChildren().addAll(
					  this.cbNumber
					, Common.createSpacer(Common.SpacerEnum.HorizontalMin)
					, this.cbAbout
					, Common.createSpacer(Common.SpacerEnum.HorizontalMin)
					, this.cbLess
					, Common.createSpacer(Common.SpacerEnum.HorizontalMin)
					, this.cbGreat
					, Common.createSpacer(Common.SpacerEnum.HorizontalMin)
					, this.cbBetween
			);

			this.boxWithCheckBoxes.getChildren().addAll(cbBoxes, this.btnScan);
			HBox.setHgrow(cbBoxes, Priority.ALWAYS);
			this.boxWithCheckBoxes.setAlignment(Pos.CENTER_LEFT);
			this.main.add(this.boxWithCheckBoxes, 0, 2, 2, 1);
		}
		//endregion

		//region table
		{
			RowConstraints r0 = new RowConstraints();
			r0.setPercentHeight(-1);
			r0.setMinHeight((800 - 32 * 3 - 4 * 8) / 2);
			r0.setPrefHeight((800 - 32 * 3 - 4 * 8) / 2);
			r0.setVgrow(Priority.SOMETIMES);

			RowConstraints r1 = new RowConstraints();
			r1.setMinHeight(32.0);
			r1.setMaxHeight(32.0);
			r1.setPrefHeight(32.0);

			this.main.getRowConstraints().addAll(r0, r1);

			this.main.add(this.btnCheckTable, 0, 4);

			this.bpView = new BorderPane();
			this.main.add(this.bpView, 1, 3);
		}
		//endregion

		borderPane.setCenter(this.main);

		this.cbConnections.getItems().setAll(WizardCommonHelper.getAllConnections(this.matrix.getFactory().getConfiguration()));

		this.btnScan.setDisable(true);
		this.boxWithCheckBoxes.setDisable(true);
		listeners();
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		//TODO implement
		return ArrayList::new;
	}

	@Override
	public boolean beforeRun()
	{
		return true;
	}
	//endregion

	private IRemoteApplication service()
	{
		return this.appConnection.getApplication().service();
	}

	private void hideTableAndView()
	{
		this.checkGrid.getRowConstraints().clear();
		this.checkGrid.getColumnConstraints().clear();
		this.checkGrid.getChildren().removeIf(node -> node instanceof RelationButton || node instanceof Text);

		this.checkGrid.setVisible(false);
		this.bpView.getChildren().clear();
	}

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
		this.btnScan.setDisable(true);

		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		ProgressIndicator indicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		indicator.setMinSize(64.0, 64.0);

		box.getChildren().addAll(indicator, new Text("Creating table..."));

		this.checkGrid.setVisible(false);
		this.main.add(box, 0, 3);

		this.executor = Executors.newSingleThreadExecutor();
		this.btnCheckTable.setDisable(true);
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
			this.btnScan.setDisable(false);
			this.btnCheckTable.setDisable(false);
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
			this.btnScan.setDisable(false);
			this.btnCheckTable.setDisable(true);
			this.main.getChildren().removeIf(node -> node == box);
			this.checkGrid.setVisible(false);
			//TODO implement
			e.getSource().getException().printStackTrace();

		});
		service.setExecutor(executor);
		service.start();
	}

	private List<RelationButton> scan0() throws Exception
	{
		List<IControl> collect = this.lvControls.getItems()
				.stream()
				.filter(c -> c.onProperty().getValue())
				.map(c -> c.control)
				.collect(Collectors.toList());

		IntStream.range(0, collect.size() +1)
				.forEach(i -> {
					RowConstraints r0 = new RowConstraints();
					r0.setPercentHeight(-1);
					r0.setVgrow(Priority.ALWAYS);

					ColumnConstraints c0 = new ColumnConstraints();
					c0.setHalignment(HPos.CENTER);
					c0.setPercentWidth(-1);
					c0.setHgrow(Priority.ALWAYS);

					this.checkGrid.getColumnConstraints().add(c0);
					this.checkGrid.getRowConstraints().add(r0);
				});

		Platform.runLater(() -> IntStream.rangeClosed(1, collect.size())
				.forEach(i ->
				{
					String id = collect.get(i - 1).getID();
					this.checkGrid.add(new Text(id), 0, i);
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
		btn.setOnAction(e -> this.bpView.setCenter(btn.createView()));
		return btn;
	}

	private Spec createFormula(IControl top, IControl left)
	{
		Rectangle topRectangle = null;
		Rectangle leftRectangle = null;

		try
		{
			IControl topOwner = this.currentWindow.getOwnerControl(top);
			topRectangle = service().getRectangle(topOwner == null ? null : topOwner.locator(), top.locator());

			if (top == left)
			{
				leftRectangle = new Rectangle(topRectangle);
			}
			else
			{
				IControl leftOwner = this.currentWindow.getOwnerControl(left);
				leftRectangle = service().getRectangle(leftOwner == null ? null : leftOwner.locator(), left.locator());
			}
		}
		catch (Exception e)
		{
			//TODO what we need return??
			return Spec.create().invisible();
		}
		//same control
		if (top == left)
		{
			Spec spec = Spec.create()
					.visible()
					.count(1);
			addSpecs(topRectangle.getHeight(), spec::height, spec::height);
			addSpecs(topRectangle.getWidth(), spec::width, spec::width);

			return spec;
		}
		return Spec.create();
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

	//endregion

	//region private classes
	private class RelationButton extends Button
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

			main.getChildren().add(new Text(String.format("Relation %s -> %s", topName, leftName)));
			main.getChildren().add(new Separator(Orientation.HORIZONTAL));
			main.getChildren().add(new Text(DoSpec.class.getSimpleName()));
			Iterator<Piece> iterator = this.formula.iterator();
			Consumer<OneRow> removeAll = this.boxWithFields.getChildren()::removeAll;
			iterator.forEachRemaining(piece -> this.boxWithFields.getChildren().add(new OneRow(piece.toString(), evaluator, removeAll)));

			Button btnAdd = new Button();
			btnAdd.setId("cbAdd");
			btnAdd.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
			this.boxWithFields.getChildren().add(btnAdd);
			btnAdd.setOnAction(e -> this.boxWithFields.getChildren().add(this.boxWithFields.getChildren().size() - 1, new OneRow("", evaluator, removeAll)));

			sp.setFitToHeight(true);
			sp.setFitToWidth(true);
			sp.setContent(this.boxWithFields);
			main.getChildren().add(sp);
			VBox.setVgrow(sp, Priority.ALWAYS);

			Button btnCheck = new Button("Check");
			Button btnSave = new Button("Save");
			HBox hBox = new HBox();
			hBox.setAlignment(Pos.CENTER_RIGHT);
			btnSave.setOnAction(e -> this.save());
			btnCheck.setOnAction(e -> {
				List<String> check = this.check();
				if (check == null)
				{
					//all ok
				}
				//TODO where place check result???
				System.out.println(check);
			});
			hBox.getChildren().addAll(btnSave, Common.createSpacer(Common.SpacerEnum.HorizontalMid), btnCheck);
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
		public List<String> check()
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
			this.formula = formula;
			//TODO add icons on button ( icon of pieces)
		}
	}

	private static class OneRow extends HBox
	{
		private TextField field;

		OneRow(String formula, AbstractEvaluator evaluator, Consumer<OneRow> handler)
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
		}

		public void requestFocus()
		{
			Common.setFocused(field);
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
			this.rectangle = Common.tryCatch(() ->
			{
				List<org.w3c.dom.Node> all = wizardMatcher.findAll(doc, this.control.locator());
				return ((Rectangle) all.get(0).getUserData(IRemoteApplication.rectangleName));
			}, "", null);
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
