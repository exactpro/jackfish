package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.WizardMatcher;
import com.exactprosystems.jf.tool.wizard.related.ConnectionBean;
import com.exactprosystems.jf.tool.wizard.related.WizardCommonHelper;
import com.exactprosystems.jf.tool.wizard.related.WizardLoader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	private ExecutorService executor;

	private Table table;

	private GridPane main;
	private Text waitText;
	
	private ComboBox<ConnectionBean> cbConnections;
	private ComboBox<IWindow>        cbDialogs;

	private ImageViewWithScale imageViewWithScale;
	private ListView<IControlWithCheck> lvControls;

	private Button btnCheckTable;
	private GridPane checkGrid;
	private Button btnScan;

	//region AbstractWizard methods
	@Override
	public void init(IContext context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);
		this.matrix = super.get(MatrixFx.class, parameters);
		this.item = super.get(MatrixItem.class, parameters);
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
		this.lvControls.setCellFactory(p -> new CheckedCell());
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

			HBox box = new HBox();
			HBox cbBoxes = new HBox();
			//TODO fill cbBoxes

			box.getChildren().addAll(cbBoxes, this.btnScan);
			HBox.setHgrow(cbBoxes, Priority.ALWAYS);
			box.setAlignment(Pos.CENTER_LEFT);
			this.main.add(box, 0, 2, 2, 1);
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

		}
		//endregion

		borderPane.setCenter(this.main);

		this.cbConnections.getItems().setAll(WizardCommonHelper.getAllConnections(this.matrix.getFactory().getConfiguration()));

		listeners();
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		return ArrayList::new;
	}

	@Override
	public boolean beforeRun()
	{
		return true;
	}
	//endregion

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

		this.cbDialogs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				boolean removeIf = this.main.getChildren().removeIf(node -> node == this.waitText);
				if (removeIf)
				{
					this.main.add(this.imageViewWithScale, 0, 1);
				}
				this.lvControls.getItems().clear();
				this.wizardLoader = new WizardLoader(this.appConnection, newValue.getSelfControl(), (image, doc) ->
				{
					Collection<IControl> controls = newValue.getControls(IWindow.SectionKind.Run);
					this.lvControls.getItems().setAll(controls.stream().filter(c -> !Str.IsNullOrEmpty(c.getID())).map(IControlWithCheck::new).collect(Collectors.toList()));

					this.imageViewWithScale.displayImage(image);

					List<Rectangle> list = XpathUtils.collectAllRectangles(doc);
					this.imageViewWithScale.setListForSearch(list);

					PluginInfo info = this.appConnection.getApplication().getFactory().getInfo();
					this.wizardMatcher = new WizardMatcher(info);

					//TODO remake and uncomment it
//					this.imageViewWithScale.setOnRectangleClick(rectangle -> this.controls.getItems().forEach(controlItem ->
//					{
//						Rectangle itemRectangle = controlItem.getRectangle(wizardMatcher);
//						if (rectangle.equals(itemRectangle))
//						{
//							controlItem.toggle();
//							if (controlItem.isOn())
//							{
//								this.imageViewWithScale.showRectangle(rectangle, MarkerStyle.MARK, "", true);
//							}
//							else
//							{
//								this.imageViewWithScale.hideRectangle(rectangle, MarkerStyle.MARK);
//							}
//						}
//					}));
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
		if (this.lvControls.getItems().stream().noneMatch(c -> c.isChecked))
		{
			DialogsHelper.showInfo("Select");
			return;
		}
		this.btnScan.setDisable(true);

		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		ProgressIndicator indicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
		indicator.setMinSize(64.0, 64.0);

		box.getChildren().addAll(indicator, new Text("Creating table..."));

		this.checkGrid.setVisible(false);
		this.main.add(box, 0, 3);

		this.executor = Executors.newSingleThreadExecutor();
		Task<Void> task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				scan0();
				return null;
			}
		};
		task.setOnSucceeded(e ->
		{
			this.btnScan.setDisable(false);
			this.btnCheckTable.setDisable(false);
			this.main.getChildren().removeIf(node -> node == box);
			this.checkGrid.setGridLinesVisible(true);
			this.checkGrid.setVisible(true);

			//TODO implement
		});
		task.setOnFailed(e ->
		{
			this.btnScan.setDisable(false);
			this.btnCheckTable.setDisable(true);
			this.main.getChildren().removeIf(node -> node == box);
			this.checkGrid.setVisible(false);
			//TODO implement
			e.getSource().getException().printStackTrace();

		});
		executor.submit(task);
	}

	private void scan0() throws Exception
	{
		List<IControl> collect = this.lvControls.getItems().stream().filter(c -> c.isChecked).map(c -> c.control).collect(Collectors.toList());
		IntStream.range(0, collect.size() +1)
				.forEach(i -> {
					RowConstraints r0 = new RowConstraints();
					r0.setPercentHeight(-1);
					r0.setVgrow(Priority.ALWAYS);

					ColumnConstraints c0 = new ColumnConstraints();
					c0.setPercentWidth(-1);
					c0.setHgrow(Priority.ALWAYS);

					this.checkGrid.getColumnConstraints().add(c0);
					this.checkGrid.getRowConstraints().add(r0);
					Platform.runLater(() -> this.checkGrid.add(new Text("" + i), i, i));
				});

		this.btnCheckTable.setDisable(true);
		Thread.sleep(500);
	}

	private void clearTable()
	{
		this.checkGrid.getRowConstraints().clear();
		this.checkGrid.getColumnConstraints().clear();
		this.checkGrid.getChildren().removeIf(node -> node instanceof Text);
	}

	//region private classes
	private static class IControlWithCheck
	{
		private IControl control;
		private boolean isChecked;

		public IControlWithCheck(IControl control)
		{
			this.control = control;
		}

		public void setChecked(boolean checked)
		{
			isChecked = checked;
		}
	}

	private static class CheckedCell extends ListCell<IControlWithCheck>
	{
		private CheckBox checkBox = new CheckBox();

		@Override
		protected void updateItem(IControlWithCheck item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				this.checkBox.setSelected(item.isChecked);
				this.checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> item.setChecked(newValue));
				this.checkBox.setText(item.control.getID());
				this.setGraphic(this.checkBox);
			}
			else
			{
				setGraphic(null);
			}
		}
	}
	//endregion
}
