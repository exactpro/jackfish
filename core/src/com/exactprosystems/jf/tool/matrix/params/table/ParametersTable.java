package com.exactprosystems.jf.tool.matrix.params.table;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.actions.gui.DialogFill;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.FormulaGenerator;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.DragDetector;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.layout.LayoutExpressionBuilder;
import com.exactprosystems.jf.tool.custom.scroll.CustomScrollPane;
import com.exactprosystems.jf.tool.custom.treetable.MatrixContextMenu;
import com.exactprosystems.jf.tool.custom.treetable.MatrixParametersContextMenu;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.params.EmptyParameterGridPane;
import com.exactprosystems.jf.tool.matrix.params.ParameterGridPane;
import com.sun.javafx.css.PseudoClassState;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ParametersTable extends CustomScrollPane
{
	private static final int oneLineHeight = 34;
	private static final int twoLineHeight = 67;

	private GridPane mainGridPane;
	private MyTableView tableView;
	private Context context;
	private Parameters parameters;
	private MatrixItem matrixItem;
	private boolean oneLine;
	private FormulaGenerator generator;
	private EventHandler<ContextMenuEvent> contextMenuHandler;
	private Common.Function fnc;
	private static ContextMenu empty = new ContextMenu();

	public ParametersTable(MatrixItem matrixItem, Context context, boolean oneLine, Parameters parameters, FormulaGenerator generator, MatrixContextMenu rowContextMenu, MatrixParametersContextMenu parametersContextMenu, Common.Function fnc)
	{
		super(oneLine ? oneLineHeight : twoLineHeight);

		this.mainGridPane = new GridPane();
		this.mainGridPane.getStyleClass().add(CssVariables.PARAMETERS_PANE);
		this.mainGridPane.setMaxWidth(Double.MAX_VALUE);

		this.tableView = new MyTableView();
		this.tableView.visibleRowCountProperty().set(oneLine ? 1 : 2);
		this.tableView.setMaxWidth(Double.MAX_VALUE);
		this.tableView.getStyleClass().addAll(CssVariables.EMPTY_HEADER_COLUMN, CssVariables.PARAMETERS_TABLE);
		this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.tableView.setFixedCellSize((oneLine ? oneLineHeight : twoLineHeight)/2 - 3);
		DoubleBinding multiply = Bindings.size(this.tableView.getItems()).multiply(this.tableView.getFixedCellSize()).add(2);

		this.tableView.prefHeightProperty().bind(multiply);
		this.tableView.maxHeightProperty().bind(multiply);
		this.tableView.minHeightProperty().bind(multiply);
		this.tableView.setRowFactory(param -> new RowFactory());

		this.tableView.prefWidthProperty().bind(Bindings.size(this.tableView.getColumns()).multiply(150));
		this.fnc = fnc;
		this.mainGridPane.add(this.tableView, 0, 0);
		this.setContent(this.mainGridPane);
		this.setFitToWidth(true);
		this.matrixItem = matrixItem;
		this.context = context;
		this.oneLine = oneLine;
		this.parameters = parameters;
		this.generator = generator;

		this.contextMenuHandler = parametersContextMenu.createContextMenuHandler();

		super.setContextMenu(rowContextMenu);
		refreshParameters(-1);
	}

	public void refreshParameters(int selectedIndex)
	{
		this.tableView.getItems().clear();
		this.tableView.getColumns().clear();
		this.mainGridPane.getChildren().clear();
		Data colData = new Data();
		Data valData = new Data();
		for (int i = 0; i < parameters.size(); i++)
		{
			Parameter byIndex = parameters.getByIndex(i);

			TopCell topCell = new TopCell();
			topCell.isEditable = byIndex.getType() == TypeMandatory.Extra;
			topCell.columnName = byIndex.getName();
			topCell.value = byIndex.getName();
			colData.list.add(topCell);

			BottomCell bottomCell = new BottomCell();
			bottomCell.value = byIndex.getExpression();
			bottomCell.columnName = byIndex.getName();
			valData.list.add(bottomCell);

			TableColumn<Data, Col> column = new TableColumn<>(byIndex.getName());
			this.tableView.getColumns().add(column);
			column.setPrefWidth(75);
			column.setMaxWidth(500);
			column.setMinWidth(75);

			column.setCellValueFactory(new ColumnValueFactory());
			column.setCellFactory(p -> new CellFactory(byIndex));
			if (selectedIndex == i)
			{

			}
		}
		this.tableView.getItems().setAll(colData, valData);
		this.mainGridPane.add(this.tableView, 1, 0);
		this.mainGridPane.add(emptyBox(FXCollections.observableArrayList(), this.contextMenuHandler), 0, 0);

//		ObservableList<Node> children = FXCollections.observableArrayList(this.mainGridPane.getChildren());
//		this.mainGridPane.getChildren().clear();
//
//		for (int i = 0; i < this.parameters.size(); i++)
//		{
//			Parameter par = this.parameters.getByIndex(i);
//			ParameterGridPane exist = findAndRemovePane(children, par);
//
//			if (exist == null)
//			{
//				exist = parameterBox(par, this.contextMenuHandler);
//			}
//			exist.updateIndex(this.parameters.getIndex(par));
//			if (i == selectedIndex)
//			{
//				exist.focusParameter();
//			}
//
//			this.mainGridPane.add(exist, i + 1, 0, 1, this.oneLine ? 1 : 2);
//		}
//		this.mainGridPane.add(emptyBox(FXCollections.observableArrayList(this.mainGridPane.getChildren()), this.contextMenuHandler), 0, 0, 1, 2);
//
//		if (!oneLine)
//		{
//			for (Node child : children)
//			{
//				if (child instanceof ParameterGridPane)
//				{
//					((ParameterGridPane) child).getExpressionField().clearlListener();
//				}
//			}
//		}
	}

	private ParameterGridPane findAndRemovePane(ObservableList<Node> children, Parameter par)
	{
		Iterator<Node> iter = children.iterator();
		while (iter.hasNext())
		{
			Node node = iter.next();
			if (node instanceof ParameterGridPane)
			{
				if (((ParameterGridPane) node).getParameter() == par)
				{
					iter.remove();
					return (ParameterGridPane) node;
				}
			}
		}
		return null;
	}

	private Pane emptyBox(ObservableList<Node> children, EventHandler<ContextMenuEvent> contextMenuHandler)
	{
		GridPane pane = new EmptyParameterGridPane();
		pane.getStyleClass().add(CssVariables.EMPTY_GRID);

		Label emptyLabel = new Label();
		emptyLabel.getStyleClass().add(CssVariables.INVISIBLE_FIELD);
		emptyLabel.setPrefWidth(12);
		emptyLabel.setMaxWidth(12);
		emptyLabel.setMinWidth(12);
		GridPane.setMargin(pane, new Insets(0, 5, 0, 0));
		pane.add(emptyLabel, 0, 0);

		pane.focusedProperty().addListener((observableValue, aBoolean, aBoolean2) ->
		{
			if (!aBoolean)
			{
				pane.getStyleClass().removeAll(pane.getStyleClass());
				pane.getStyleClass().add(CssVariables.FOCUSED_EMPTY_GRID);
			}
			if (!aBoolean2)
			{
				pane.getStyleClass().removeAll(pane.getStyleClass());
				pane.getStyleClass().add(CssVariables.EMPTY_GRID);
			}
		});

		pane.setOnDragDetected(new DragDetector(() -> Arrays.toString(children.stream().filter(node -> node instanceof ParameterGridPane).map(node -> ((ParameterGridPane) node).getParameter()).filter(Objects::nonNull).map(Parameter::getName).collect(Collectors.toList()).toArray()))::onDragDetected);

		pane.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean b = false;
			if (dragboard.hasString())
			{
				String str = dragboard.getString();
				if (str != null && str.startsWith("[") && str.endsWith("]"))
				{
					String[] fields = str.substring(1, str.length() - 1).split(",");
					Common.tryCatch(() -> getMatrix().parameterInsert(this.matrixItem, -1, Arrays.stream(fields).map(i -> new Pair<>(new ReadableValue(i.trim()), TypeMandatory.Extra)).collect(Collectors.toList())), "Error on change parameters");
				}

				b = true;
			}
			event.setDropCompleted(b);
			event.consume();
		});

		pane.setOnDragOver(event ->
		{
			if (event.getGestureSource() != pane && event.getDragboard().hasString())
			{
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});

		pane.setOnContextMenuRequested(contextMenuHandler);

		return pane;
	}

	private ParameterGridPane parameterBox(Parameter par, EventHandler<ContextMenuEvent> contextMenuHandler)
	{
		ParameterGridPane tempGrid = new ParameterGridPane(this.parameters.getIndex(par), par);
		Control key = new TextField(par.getName());
		((TextField) key).setPromptText("Key");
		Common.sizeTextField((TextField) key);
		//		key.focusedProperty().addListener(createKeyChangeListener(par, finalKey, parameters.getIndex(par)));
		switch (par.getType())
		{
			case Mandatory:
			case NotMandatory:
				key = new Label(par.getName());
				Common.sizeLabel((Label) key);
		}
		tempGrid.setKey(key);
		tempGrid.setKeyListener(this.fnc, (index, text) -> getMatrix().parameterSetName(this.matrixItem, index, text));
		key.setContextMenu(empty);
		key.setOnContextMenuRequested(contextMenuHandler);
		GridPane.setMargin(key, Common.INSETS_NODE);
		focusedParent(key);
		key.setStyle(Common.FONT_SIZE);

		if (this.generator != null)
		{
			key.setOnDragDetected(new DragDetector(() -> this.generator.generate() + par.getName())::onDragDetected);
		}

		if (!this.oneLine)
		{
			ExpressionField expressionField = createExpressionField(par);
			expressionField.setContextMenu(empty);
			expressionField.setOnContextMenuRequested(contextMenuHandler);
			expressionField.setNotifierForErrorHandler();
			expressionField.setHelperForExpressionField(par.getName(), this.matrixItem.getMatrix());
			tempGrid.setValue(expressionField);
			tempGrid.setValueListener(this.fnc, (index, text) -> getMatrix().parameterSetValue(this.matrixItem, index, text));
			GridPane.setMargin(expressionField, Common.INSETS_NODE);
			focusedParent(expressionField);
		}
		tempGrid.setOnContextMenuRequested(contextMenuHandler);

		return tempGrid;
	}

	private ExpressionField createExpressionField(Parameter par)
	{
		ExpressionField expressionField = new ExpressionField(this.context.getEvaluator(), par.getExpression());
		if (this.matrixItem instanceof ActionItem)
		{
			ActionItem actionItem = (ActionItem) this.matrixItem;
			HelpKind howHelp = null;

			try
			{
				howHelp = actionItem.howHelpWithParameter(this.context, par.getName());
			}
			catch (Exception e)
			{
			}

			AbstractEvaluator evaluator = this.context.getEvaluator();
			if (howHelp != null)
			{
				expressionField.setNameFirst(howHelp.getLabel());
				switch (howHelp)
				{
					case BuildQuery:
						break;

					case BuildLayoutExpression:
						expressionField.setFirstActionListener(str ->
						{
							String expression = this.parameters.getExpression(DialogFill.dialogName);
							String dialogName = null;
							try
							{
								dialogName = String.valueOf(evaluator.evaluate(expression));
							}
							catch (Exception e)
							{
							}
							try
							{
								LayoutExpressionBuilder viewer = new LayoutExpressionBuilder(par.getName(), expressionField.getText(), this.matrixItem.getParent().getMatrix().getDefaultApplicationConnection(), dialogName, evaluator);
								return viewer.show("Layout expression for " + par.getName(), false);
							}
							catch (Exception e)
							{
								DialogsHelper.showError(e.getMessage());
							}
							return expressionField.getText();
						});
						break;

					case ChooseDateTime:
						expressionField.setFirstActionListener(str ->
						{
							Date date = null;
							if (expressionField.getText() != null)
							{
								try
								{
									date = (Date) evaluator.evaluate(expressionField.getText());
								}
								catch (Exception e)
								{
									date = DateTime.current();
								}
							}


							Date res = DialogsHelper.showDateTimePicker(date);
							if (res != null)
							{
								LocalDateTime ldt = Common.convert(res);
								return String.format("DateTime.date(%d, %d, %d,  %d, %d, %d)",
										//				because localDateTime begin month from 1, not 0
										ldt.getYear(), ldt.getMonthValue() - 1, ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond());
							}
							return expressionField.getText();
						});
						break;

					case ChooseOpenFile:
						expressionField.setFirstActionListener(str ->
						{
							File file = DialogsHelper.showOpenSaveDialog("Choose file to open", "All files", "*.*", DialogsHelper.OpenSaveMode.OpenFile);
							if (file != null)
							{
								return this.context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
							}
							return str;
						});
						break;

					case ChooseSaveFile:
						expressionField.setFirstActionListener(str ->
						{
							File file = DialogsHelper.showOpenSaveDialog("Choose file to save", "All files", "*.*", DialogsHelper.OpenSaveMode.SaveFile);
							if (file != null)
							{
								return this.context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
							}
							return str;
						});
						break;

					case ChooseFolder:
						expressionField.setFirstActionListener(str ->
						{
							File file = DialogsHelper.showDirChooseDialog("Choose directory");
							if (file != null)
							{
								return this.context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
							}
							return str;
						});
						break;

					case ChooseFromList:
						expressionField.setChooserForExpressionField(par.getName(), () -> actionItem.listToFillParameter(this.context, par.getName()));
						break;

					case BuildXPath:
						expressionField.setFirstActionListener(str ->
						{
							for (int i = 0; i < this.parameters.size(); i++)
							{
								Parameter next = this.parameters.getByIndex(i);
								Object obj = evaluator.tryEvaluate(next.getExpression());
								if (obj instanceof Xml)
								{
									Xml xml = (Xml) obj;
									Object value = evaluator.tryEvaluate(par.getExpression());
									String initial = value == null ? null : String.valueOf(value);
									XpathViewer viewer = new XpathViewer(null, xml.getDocument(), null);
									String res = viewer.show(initial, "Xpath for " + par.getName(), Common.currentThemesPaths(), false);
									if (res != null)
									{
										res = evaluator.createString(res);
									}

									return res;
								}
							}
							return str;
						});
						break;

					default:
						break;
				}
			}
		}
		return expressionField;
	}

	private void focusedParent(final Node node)
	{
		ChangeListener<Boolean> changeListener = (observableValue, aBoolean, aBoolean2) ->
		{
			node.getParent().getStyleClass().removeAll(CssVariables.UNFOCUSED_GRID, CssVariables.FOCUSED_FIELD);
			if (!aBoolean)
			{
				node.getParent().getStyleClass().add(CssVariables.FOCUSED_FIELD);
			}
			if (!aBoolean2)
			{
				node.getParent().getStyleClass().add(CssVariables.UNFOCUSED_GRID);
			}
		};
		if (node instanceof ExpressionField)
		{
			((ExpressionField) node).setChangingFocusListener(changeListener);
		}
		else if (node instanceof TextField)
		{
			node.focusedProperty().addListener(changeListener);
		}
		else if (node instanceof ComboBox)
		{
			((ComboBox<?>) node).getEditor().focusedProperty().addListener(changeListener);
		}
	}

	private MatrixFx getMatrix()
	{
		return ((MatrixFx) this.matrixItem.getMatrix());
	}


	private class Data
	{
		ArrayList<Col> list = new ArrayList<>();
	}

	private abstract class Col
	{
		String columnName;
		String value;

		abstract boolean isEditable();

		@Override
		public String toString()
		{
			return value;
		}
	}

	private class TopCell extends Col
	{
		boolean isEditable;

		@Override
		boolean isEditable()
		{
			return isEditable;
		}
	}

	private class BottomCell extends Col
	{

		@Override
		boolean isEditable()
		{
			return true;
		}
	}

	private class MyTableView extends TableView<Data>
	{
		private IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 2);


		public IntegerProperty visibleRowCountProperty() {
			return visibleRowCount;
		}

		@Override
		protected Skin<?> createDefaultSkin() {
			return new TableViewSkinX(this);
		}

		public class TableViewSkinX extends TableViewSkin<Data>
		{

			public TableViewSkinX(MyTableView tableView) {
				super(tableView);
				registerChangeListener(tableView.visibleRowCountProperty(), "VISIBLE_ROW_COUNT");
				handleControlPropertyChanged("VISIBLE_ROW_COUNT");
			}

			@Override
			protected void handleControlPropertyChanged(String p) {
				super.handleControlPropertyChanged(p);
				if ("VISIBLE_ROW_COUNT".equals(p)) {
					needCellsReconfigured = true;
					getSkinnable().requestFocus();
				}
			}

			/**
			 * Returns the visibleRowCount value of the table.
			 */
			private int getVisibleRowCount() {
				return ((MyTableView) getSkinnable()).visibleRowCountProperty().get();
			}

			/**
			 * Calculates and returns the pref height of the
			 * for the given number of rows.
			 *
			 * If flow is of type MyFlow, queries the flow directly
			 * otherwise invokes the method.
			 */
			protected double getFlowPrefHeight(int rows) {
				double height = 0;
				if (flow instanceof MyFlow) {
					height = ((MyFlow) flow).getPrefLength(rows);
				}
				else {
					for (int i = 0; i < rows && i < getItemCount(); i++) {
						height += invokeFlowCellLength(i);
					}
				}
				return height + snappedTopInset() + snappedBottomInset();

			}

			/**
			 * Overridden to compute the sum of the flow height and header prefHeight.
			 */
			@Override
			protected double computePrefHeight(double width, double topInset,
											   double rightInset, double bottomInset, double leftInset) {
				// super hard-codes to 400 .. doooh
				double prefHeight = getFlowPrefHeight(getVisibleRowCount());
				return prefHeight + getTableHeaderRow().prefHeight(width);
			}

			/**
			 * Reflectively invokes protected getCellLength(i) of flow.
			 * @param index the index of the cell.
			 * @return the cell height of the cell at index.
			 */
			protected double invokeFlowCellLength(int index) {
				double height = 1.0;
				Class<?> clazz = VirtualFlow.class;
				try {
					Method method = clazz.getDeclaredMethod("getCellLength", Integer.TYPE);
					method.setAccessible(true);
					return ((double) method.invoke(flow, index));
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
				return height;
			}

			/**
			 * Overridden to return custom flow.
			 */
			@Override
			protected VirtualFlow createVirtualFlow() {
				return new MyFlow();
			}

			/**
			 * Extended to expose length calculation per a given # of rows.
			 */
			public class MyFlow extends VirtualFlow {

				protected double getPrefLength(int rowsPerPage) {
					double sum = 0.0;
					int rows = rowsPerPage; //Math.min(rowsPerPage, getCellCount());
					for (int i = 0; i < rows; i++) {
						sum += getCellLength(i);
					}
					return sum;
				}

			}

		}
	}

	private class CellFactory extends TableCell<Data, Col>
	{
		private Parameter parameter;

		public CellFactory(Parameter par)
		{
			this.parameter = par;
		}

		@Override
		protected void updateItem(Col item, boolean empty)
		{
			super.updateItem(item, empty);
			this.setAlignment(Pos.CENTER_LEFT);
			if (item != null && !empty)
			{
				this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				if (item.isEditable())
				{
					if (item instanceof TopCell)
					{
						TextField value = new TextField(item.toString());
						value.getStyleClass().setAll(CssVariables.EDITABLE_PARAMETER);
						setGraphic(value);
					}
					else
					{
						ExpressionField expressionField = createExpressionField(this.parameter);
						expressionField.setContextMenu(ParametersTable.empty);
						expressionField.setOnContextMenuRequested(contextMenuHandler);
						expressionField.setNotifierForErrorHandler();
						expressionField.setHelperForExpressionField(item.columnName, ParametersTable.this.matrixItem.getMatrix());
						setGraphic(expressionField);
					}
				}
				else
				{
					Label value = new Label(item.toString());
					value.getStyleClass().addAll(CssVariables.NOT_EDITABLE_PARAMETER);
					setGraphic(value);
				}
			}
			else
			{
				setGraphic(null);
			}
		}
	}

	private class ColumnValueFactory implements Callback<TableColumn.CellDataFeatures<Data, Col>, ObservableValue<Col>>
	{
		@Override
		public ObservableValue<Col> call(TableColumn.CellDataFeatures<Data, Col> param)
		{
			SimpleObjectProperty<Col> property = new SimpleObjectProperty<>();
			Optional<Col> c = param.getValue().list.stream().filter(col -> col.columnName.equals(param.getTableColumn().getText())).findFirst();
			c.ifPresent(property::set);
			return property;
		}
	}

	private class RowFactory extends TableRow<Data>
	{
		private final PseudoClass customSelected = PseudoClassState.getPseudoClass("customSelectedState");
		private final PseudoClass selected = PseudoClassState.getPseudoClass("selected");

		public RowFactory()
		{
			this.selectedProperty().addListener((observable, oldValue, newValue) -> {
				this.pseudoClassStateChanged(customSelected, newValue);
				this.pseudoClassStateChanged(selected, false); // remove selected pseudostate, cause this state change text color
			});
		}
	}
}
