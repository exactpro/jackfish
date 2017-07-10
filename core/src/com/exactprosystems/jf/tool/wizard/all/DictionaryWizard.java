////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.guidic.Window;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.custom.scaledimage.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.xmltree.XmlTreeView;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.dictionary.dialog.ElementWizardBean;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import com.exactprosystems.jf.tool.wizard.related.WizardHelper;
import com.sun.javafx.css.PseudoClassState;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@WizardAttribute(
        name 				= "Test dictionary wizard",
        pictureName 		= "DictionaryWizard.png",
        category 			= WizardCategory.GUI_DICTIONARY,
        shortDescription 	= "This wizard is only for test purpose.",
        detailedDescription = "Here you descrioption might be",
        experimental 		= true,
        strongCriteries 	= true,
        criteries 			= { DictionaryFx.class, Window.class }
    )
public class DictionaryWizard extends AbstractWizard
{
	private AppConnection currentConnection = null;
	private DictionaryFx  currentDictionary = null;
	private Window        currentWindow     = null;

	private volatile Document document    = null;
	private volatile Node     currentNode = null;

	private SplitPane                   centralSplitPane   = null;
	private ImageViewWithScale          imageViewWithScale = null;
	private XmlTreeView                 xmlTreeView        = null;
	private FindPanel<org.w3c.dom.Node> findPanel          = null;

	private TextField                    tfDialogName;
	private Label                        lblSelfId;
	private javafx.scene.control.Button  btnGenerateOnOpen;
	private javafx.scene.control.Button  btnGenerateOnClose;
	private TableView<ElementWizardBean> tableView;
	private Button btnWizard;
	private Button btnNextMark;
	private Button btnPrevMark;
	private CheckBox cbUpdate;
	private CheckBox cbAdd;
	private CheckBox cbMark;
	private CheckBox cbQuestion;
	

	public DictionaryWizard()
	{
	}

	@Override
	public void init(IContext context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);

		this.currentConnection = super.get(AppConnection.class, parameters);
		this.currentDictionary = super.get(DictionaryFx.class, parameters);
		this.currentWindow = super.get(Window.class, parameters);
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		borderPane.setPrefHeight(1000.0);
		borderPane.setPrefWidth(1500.0);

		this.centralSplitPane = new SplitPane();
		this.centralSplitPane.setOrientation(Orientation.VERTICAL);
		this.centralSplitPane.setDividerPositions(0.5);
		this.centralSplitPane.setPrefSize(160.0, 200.0);
		BorderPane.setAlignment(this.centralSplitPane, Pos.CENTER);

		this.xmlTreeView = new XmlTreeView();

		this.imageViewWithScale = new ImageViewWithScale();
		this.imageViewWithScale.setOnRectangleClick(rectangle -> this.xmlTreeView.selectItem(rectangle));

		this.findPanel = new FindPanel<>();
		this.findPanel.getStyleClass().remove(CssVariables.FIND_PANEL);

		//region top
		SplitPane topSplitPane = new SplitPane();
		topSplitPane.setPrefSize(200.0, 200.0);
		topSplitPane.setDividerPositions(0.5);

		topSplitPane.getItems().add(this.imageViewWithScale);

		BorderPane bp = new BorderPane();
		bp.setPrefSize(200.0, 200.0);
		bp.setMinWidth(538.0);
		topSplitPane.getItems().add(bp);

		VBox vBox = new VBox();
		BorderPane.setAlignment(vBox, Pos.CENTER);
		bp.setTop(vBox);

		GridPane gridPane = new GridPane();
		VBox.setVgrow(gridPane, Priority.ALWAYS);

		Function<Double, ColumnConstraints> createColumn = percent ->
		{
			ColumnConstraints c = new ColumnConstraints();
			c.setHalignment(HPos.CENTER);
			c.setHgrow(Priority.SOMETIMES);
			c.setMinWidth(10.0);
			c.setPercentWidth(percent);
			c.setPrefWidth(100.0);
			return c;
		};
		gridPane.getColumnConstraints().addAll(
				createColumn.apply(15.0)
				, createColumn.apply(25.0)
				, createColumn.apply(10.0)
				, createColumn.apply(15.0)
				, createColumn.apply(35.0)
		);

		RowConstraints r0 = new RowConstraints();
		r0.setMinHeight(10.0);
		r0.setPrefHeight(30.0);
		r0.setVgrow(Priority.SOMETIMES);
		gridPane.getRowConstraints().addAll(r0);

		Label lblDialog = new Label("Dialog : ");
		gridPane.add(lblDialog, 0, 0);
		GridPane.setHalignment(lblDialog, HPos.RIGHT);

		this.tfDialogName = new TextField();
		this.tfDialogName.setText(this.currentWindow.getName());
		gridPane.add(this.tfDialogName, 1, 0);

		Label lblSelf = new Label("Self id : ");
		gridPane.add(lblSelf, 2, 0);
		GridPane.setHalignment(lblSelf, HPos.RIGHT);

		this.lblSelfId = new Label();
		Common.tryCatch(() -> this.lblSelfId.setText(this.currentWindow.getSelfControl().getID()), "Error on set self id");
		gridPane.add(this.lblSelfId, 3, 0);
		GridPane.setHalignment(this.lblSelfId, HPos.LEFT);
		this.lblSelfId.getStyleClass().add(CssVariables.BOLD_LABEL);

		HBox hBox = new HBox();
		gridPane.add(hBox, 4, 0);
		GridPane.setHalignment(hBox, HPos.CENTER);
		GridPane.setValignment(hBox, VPos.CENTER);
		hBox.setAlignment(Pos.CENTER_RIGHT);

		this.btnGenerateOnOpen = new Button("On open");
		this.btnGenerateOnClose = new Button("On close");
		hBox.getChildren().addAll(this.btnGenerateOnOpen, Common.createSpacer(Common.SpacerEnum.HorizontalMid), this.btnGenerateOnClose);

		vBox.getChildren().addAll(Common.createSpacer(Common.SpacerEnum.VerticalMid), gridPane, Common.createSpacer(Common.SpacerEnum.VerticalMid));

		this.tableView = new TableView<>();
		initTable();
		BorderPane.setAlignment(this.tableView, Pos.CENTER);
		bp.setCenter(this.tableView);

		//endregion

		//region bottom
		BorderPane paneTreeView = new BorderPane();
		paneTreeView.setPrefSize(200.0, 200.0);
		paneTreeView.setCenter(this.xmlTreeView);
		
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPrefSize(1498.0, 30.0);

		this.btnWizard = new Button();
		this.btnWizard.setId("btnWizard");
		this.btnWizard.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		this.btnNextMark = new Button();
		this.btnNextMark.setId("btnNextMark");
		this.btnNextMark.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		this.btnPrevMark = new Button();
		this.btnPrevMark.setId("btnPrevMark");
		this.btnPrevMark.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		this.cbUpdate = new CheckBox("0");
		this.cbUpdate.setId("cbUpdate");
		this.cbUpdate.setMinWidth(75.0);
		this.cbUpdate.setPrefWidth(75.0);
		this.cbUpdate.setMaxWidth(75.0);
		this.cbUpdate.setSelected(true);

		this.cbAdd = new CheckBox("0");
		this.cbAdd.setId("cbAdd");
		this.cbAdd.setMinWidth(75.0);
		this.cbAdd.setPrefWidth(75.0);
		this.cbAdd.setMaxWidth(75.0);
		this.cbAdd.setSelected(true);

		this.cbMark = new CheckBox("0");
		this.cbMark.setId("cbMark");
		this.cbMark.setMinWidth(75.0);
		this.cbMark.setPrefWidth(75.0);
		this.cbMark.setMaxWidth(75.0);
		this.cbMark.setSelected(true);

		this.cbQuestion = new CheckBox("0");
		this.cbQuestion.setId("cbQuestion");
		this.cbQuestion.setMinWidth(75.0);
		this.cbQuestion.setPrefWidth(75.0);
		this.cbQuestion.setMaxWidth(75.0);
		this.cbQuestion.setSelected(true);

		GridPane gp = new GridPane();
		HBox.setHgrow(gp, Priority.ALWAYS);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setHgrow(Priority.SOMETIMES);
		c0.setMinWidth(10.0);
		c0.setPrefWidth(100.0);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setHgrow(Priority.SOMETIMES);
		c1.setMinWidth(10.0);
		c1.setPercentWidth(65.0);
		c1.setPrefWidth(100.0);

		gp.getColumnConstraints().addAll(c0, c1);

		RowConstraints r00 = new RowConstraints();
		r00.setMinHeight(10.0);
		r00.setPrefHeight(30.0);
		r00.setVgrow(Priority.SOMETIMES);
		gp.getRowConstraints().addAll(r00);

		gp.add(this.findPanel, 1, 0);
		box.getChildren().addAll(
				  this.btnWizard
				, new Separator(Orientation.VERTICAL)
				, this.btnNextMark
				, this.btnPrevMark
				, new Separator(Orientation.VERTICAL)
				, this.cbUpdate
				, new Separator(Orientation.VERTICAL)
				, this.cbAdd
				, new Separator(Orientation.VERTICAL)
				, this.cbMark
				, new Separator(Orientation.VERTICAL)
				, this.cbQuestion
				, new Separator(Orientation.VERTICAL)
				, gp
		);
		paneTreeView.setTop(box);
		//endregion

		this.centralSplitPane.getItems().addAll(topSplitPane, paneTreeView);
		borderPane.setCenter(this.centralSplitPane);
		initListeners();
		updateOnButtons();
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		return () ->
		{
			List<WizardCommand> commands = CommandBuilder.start().build();

			return commands;
		};
	}

	@Override
	public boolean beforeRun()
	{
		try
		{
			if (this.currentConnection == null || !this.currentConnection.isGood())
			{
				DialogsHelper.showError("Application is not started.\nStart it before call the wizard.");
				return false;
			}

			IControl self = this.currentWindow.getSelfControl();
			WizardHelper.gainImageAndDocument(this.currentConnection, self, (image, doc) ->
			{
				this.imageViewWithScale.displayImage(image);

				this.document = doc;
				this.xmlTreeView.displayDocument(this.document);
				List<Rectangle> list = XpathUtils.collectAllRectangles(this.document);
				this.imageViewWithScale.setListForSearch(list);
			}, ex ->
			{
				String message = ex.getMessage();
				if (ex.getCause() instanceof JFRemoteException)
				{
					message = ((JFRemoteException) ex.getCause()).getErrorKind().toString();
				}
				DialogsHelper.showError(message);
			});
		}
		catch (Exception e)
		{
			DialogsHelper.showError(e.getMessage());
			return false;
		}

		return true;
	}

	private void initListeners()
	{
		this.findPanel.setListener(new IFind<org.w3c.dom.Node>()
		{
			@Override
			public void find(org.w3c.dom.Node item)
			{
				xmlTreeView.select(item);
			}

			@Override
			public List<org.w3c.dom.Node> findItem(String what, boolean matchCase, boolean wholeWord)
			{
				return xmlTreeView.findItem(what, matchCase, wholeWord);
			}
		});

		this.xmlTreeView.setOnSelectionChanged((oldItem, oldMarker, newItem, newMarker) ->
		{
			if (oldItem != null)
			{
				this.imageViewWithScale.hideRectangle(oldItem.getRectangle(), oldMarker);
				if (oldItem.getStyle() != null)
				{
					this.imageViewWithScale.showRectangle(oldItem.getRectangle(), oldItem.getStyle(), oldItem.getText(), false);
				}
			}

			if (newItem != null)
			{
				if (newMarker != null)
				{
					this.imageViewWithScale.showRectangle(newItem.getRectangle(), newMarker, newItem.getText(), true);
				}
			}
		});

		this.xmlTreeView.setOnMarkerChanged((item, oldMarker, newMarker, selected) ->
		{
			if (item != null)
			{
				this.imageViewWithScale.hideRectangle(item.getRectangle(), oldMarker);
				this.imageViewWithScale.showRectangle(item.getRectangle(), newMarker, item.getText(), selected);
			}
		});

		//TODO add listeners to :
		this.btnNextMark.setOnAction(e ->
		{
		});
		this.btnPrevMark.setOnAction(e ->
		{
		});
		this.btnWizard.setOnAction(e ->
		{
		});
		this.btnGenerateOnClose.setOnAction(e ->
		{
		});
		this.btnGenerateOnOpen.setOnAction(e ->
		{
		});
	}

	private void initTable()
	{
		this.tableView.setEditable(true);
		this.tableView.setRowFactory(row -> new CustomRowFactory());
		TableColumn<ElementWizardBean, Integer> columnNumber = new TableColumn<>("#");
		columnNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
		columnNumber.setCellFactory(e -> new TableCell<ElementWizardBean, Integer>()
		{
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(String.valueOf(item));
					this.setAlignment(Pos.CENTER);
				}
				else
				{
					setText(null);
				}
			}
		});
		columnNumber.setPrefWidth(35);
		columnNumber.setMaxWidth(35);
		columnNumber.setMinWidth(35);

		TableColumn<ElementWizardBean, String> columnId = new TableColumn<>("Id");
		columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		columnId.setEditable(true);
		columnId.setCellFactory(e -> new TableCell<ElementWizardBean, String>()
		{
			private TextField textField;

			@Override
			public void startEdit()
			{
				super.startEdit();
				if (textField == null)
				{
					createTextField();
				}
				textField.setText(getString());
				setGraphic(textField);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				Platform.runLater(textField::requestFocus);
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				setText(Str.asString(getItem()));
				setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			@Override
			protected void updateItem(String s, boolean b)
			{
				super.updateItem(s, b);
				if (b || s == null)
				{
					setText(null);
					setGraphic(null);
				}
				else
				{
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}

			private String getString()
			{
				return Str.asString(getItem());
			}

			private void createTextField()
			{
				textField = new TextField(getString());
				textField.getStyleClass().add(CssVariables.TEXT_FIELD_VARIABLES);
				textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
				textField.setOnKeyPressed(t ->
				{
					if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB)
					{
						commitEdit(textField.getText());
					}
					else if (t.getCode() == KeyCode.ESCAPE)
					{
						cancelEdit();
					}
				});
				textField.focusedProperty().addListener((observable, oldValue, newValue) ->
				{
					if (!newValue && textField != null)
					{
						commitEdit(textField.getText());
					}
				});
			}
		});

		columnId.setOnEditCommit(e ->
		{
			ElementWizardBean elementWizardBean = e.getRowValue();
			if (elementWizardBean != null)
			{
//				TODO
//				Common.tryCatch(() -> this.model.updateId(elementWizardBean, e.getNewValue()), "Error on update id");
			}
		});
		columnId.setMinWidth(100.0);

		TableColumn<ElementWizardBean, ControlKind> columnKind = new TableColumn<>("Kind");
		columnKind.setCellValueFactory(new PropertyValueFactory<>("controlKind"));
		columnKind.setOnEditCommit(e ->
		{
			ElementWizardBean rowValue = e.getRowValue();
			if (rowValue != null)
			{
				//				TODO
//				Common.tryCatch(() -> this.model.updateControlKind(rowValue, e.getNewValue()), "Error on update control kind");
			}
		});
		columnKind.setCellFactory(e -> new TableCell<ElementWizardBean, ControlKind>()
		{
			ChoiceBox<ControlKind> comboBox;

			@Override
			protected void updateItem(ControlKind item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
				else
				{
					setGraphic(null);
					setText(null);
				}
			}

			@Override
			public void startEdit()
			{
				super.startEdit();
				createCB();
				setGraphic(this.comboBox);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				this.comboBox.show();
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				setText(getString());
				setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			private void createCB()
			{
				if (this.comboBox == null)
				{
					this.comboBox = new ChoiceBox<>(FXCollections.observableArrayList(ControlKind.values()));
					this.comboBox.getSelectionModel().select(getItem());
					this.comboBox.setOnAction(e -> commitEdit(this.comboBox.getSelectionModel().getSelectedItem()));
					this.comboBox.showingProperty().addListener((observable, oldValue, newValue) ->
					{
						if (!newValue)
						{
							cancelEdit();
						}
					});
				}
			}

			private String getString()
			{
				return String.valueOf(getItem() == null ? "" : getItem().name());
			}
		});
		columnKind.setPrefWidth(135);
		columnKind.setMaxWidth(135);
		columnKind.setMinWidth(135);

		int value = 50;

		TableColumn<ElementWizardBean, Boolean> columnIsXpath = new TableColumn<>("Xpath");
		columnIsXpath.setCellValueFactory(new PropertyValueFactory<>("xpath"));
		columnIsXpath.setCellFactory(e -> new TableCell<ElementWizardBean, Boolean>()
		{
			@Override
			protected void updateItem(Boolean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					this.setAlignment(Pos.CENTER);
					setGraphic(item ? new ImageView(new javafx.scene.image.Image(CssVariables.Icons.MARK_ICON)) : null);
				}
				else
				{
					setGraphic(null);
				}
			}
		});
		columnIsXpath.setPrefWidth(value);
		columnIsXpath.setMaxWidth(value);
		columnIsXpath.setMinWidth(value);

		TableColumn<ElementWizardBean, Boolean> columnIsNew = new TableColumn<>("New");
		columnIsNew.setCellValueFactory(new PropertyValueFactory<>("isNew"));
		columnIsNew.setCellFactory(e -> new TableCell<ElementWizardBean, Boolean>()
		{
			@Override
			protected void updateItem(Boolean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					this.setAlignment(Pos.CENTER);
					setGraphic(item ? new ImageView(new javafx.scene.image.Image(CssVariables.Icons.MARK_ICON)) : null);
				}
				else
				{
					setGraphic(null);
				}
			}
		});
		columnIsNew.setPrefWidth(value);
		columnIsNew.setMaxWidth(value);
		columnIsNew.setMinWidth(value);

		TableColumn<ElementWizardBean, Integer> columnCount = new TableColumn<>("Count");
		columnCount.setCellValueFactory(new PropertyValueFactory<>("count"));
		columnCount.setCellFactory(e -> new TableCell<ElementWizardBean, Integer>()
		{
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				this.setAlignment(Pos.CENTER);
				if (item != null && !empty)
				{
					setText(item.toString());
				}
				else
				{
					setText(null);
				}
			}
		});

		columnCount.setPrefWidth(value);
		columnCount.setMaxWidth(value);
		columnCount.setMinWidth(value);

		TableColumn<ElementWizardBean, ElementWizardBean> columnOption = new TableColumn<>("Option");
		columnOption.setCellValueFactory(new PropertyValueFactory<>("option"));
		columnOption.setPrefWidth(100);
		columnOption.setMaxWidth(100);
		columnOption.setMinWidth(100);
		columnOption.setCellFactory(e -> new TableCell<ElementWizardBean, ElementWizardBean>()
		{
			@Override
			protected void updateItem(ElementWizardBean item, boolean empty)
			{
				super.updateItem(item, empty);
				this.setAlignment(Pos.CENTER);
				if (item != null && !empty)
				{
					HBox box = new HBox();
					box.setAlignment(Pos.CENTER);

					Button btnEdit = new Button();
					btnEdit.setId("btnEdit");
					btnEdit.setTooltip(new Tooltip("Edit element"));
					btnEdit.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					//TODO
//					btnEdit.setOnAction(e -> Common.tryCatch(() -> model.changeElement(item), "Error on change element"));

					Button btnRemove = new Button();
					btnRemove.setId("btnRemove");
					btnRemove.setTooltip(new Tooltip("Remove element"));
					btnRemove.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					//TODO
//					btnRemove.setOnAction(e -> model.removeElement(item));

					Button btnRelation = new Button();
					btnRelation.setId("btnRelation");
					btnRelation.setTooltip(new Tooltip("Set relation"));
					btnRelation.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					//TODO
//					btnRelation.setOnAction(e -> model.updateRelation(item));
					box.getChildren().addAll(btnEdit, btnRelation, btnRemove);
					setGraphic(box);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		columnId.prefWidthProperty().bind(this.tableView.widthProperty().subtract(35 + 135 + value * 3 + 100 + 2 + 16));

		this.tableView.getColumns().addAll(columnNumber, columnId, columnKind, columnIsXpath, columnIsNew, columnCount, columnOption);
	}

	private class CustomRowFactory extends TableRow<ElementWizardBean>
	{
		private final PseudoClass customSelected = PseudoClassState.getPseudoClass("customSelectedState");
		private final PseudoClass selected       = PseudoClassState.getPseudoClass("selected");

		public CustomRowFactory()
		{
			this.getStyleClass().addAll(CssVariables.CUSTOM_TABLE_ROW);
			this.selectedProperty().addListener((observable, oldValue, newValue) -> {
				this.pseudoClassStateChanged(customSelected, newValue);
				this.pseudoClassStateChanged(selected, false); // remove selected pseudostate, cause this state change text color
			});
		}

		@Override
		protected void updateItem(ElementWizardBean item, boolean empty)
		{
			super.updateItem(item, empty);
			this.getStyleClass().removeAll(
					CssVariables.COLOR_MARK,
					CssVariables.COLOR_QUESTION,
					CssVariables.COLOR_NOT_FOUND,
					CssVariables.COLOR_NOT_FINDING,
					CssVariables.COLOR_ADD,
					CssVariables.COLOR_UPDATE
			);
			if (item != null && !empty && item.getStyleClass() != null)
			{
				this.getStyleClass().add(item.getStyleClass());
			}
		}
	}

	private void updateOnButtons()
	{
		boolean onOpenEmpty = this.currentWindow.getSection(IWindow.SectionKind.OnOpen).getControls().isEmpty();
		boolean onCloseEmpty = this.currentWindow.getSection(IWindow.SectionKind.OnClose).getControls().isEmpty();

		this.btnGenerateOnOpen.setStyle("-fx-background-color : " + (!onOpenEmpty ? "rgba(0,255,0, 0.1)" : "rgba(255,0,0, 0.1)"));
		this.btnGenerateOnClose.setStyle("-fx-background-color : " + (!onCloseEmpty ? "rgba(0,255,0, 0.1)" : "rgba(255,0,0, 0.1)"));
	}
}
