package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SearchController implements Initializable, ContainingParent
{
	private final Object lock = new Object();

	private Parent parent;

	public GridPane              scopePane;
	public GridPane              mainGridPane;
	public ComboBox<String>      cbFileMask;
	public ComboBox<String>      cbFind;
	public CheckBox              cbCaseSensitive;
	public CheckBox              cbRegexp;
	public CheckBox              cbWholeWord;
	public CheckBox              cbMatrix;
	public CheckBox              cbLibs;
	public CheckBox              cbGuiDic;
	public CheckBox              cbClientDic;
	public CheckBox              cbVariables;
	public CheckBox              cbFiles;
	public CustomFieldWithButton cfDirectory;
	public Label                 lblMatches;
	public Button                btnFind;
	public ListView<SearchResult> lvResult;
	public HBox hBoxSearching;

	private Search model;
	private Alert  alert;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		Node scope = BorderWrapper.wrap(this.scopePane).title("Scope").color(Common.currentTheme().getReverseColor()).outerPadding(4).innerPadding(4).build();
		this.mainGridPane.add(scope, 0, 4);

		this.cbFileMask.getItems().add(Search.ALL_FILES);
		this.cbFileMask.getSelectionModel().selectFirst();

		this.cfDirectory.setButtonText("...");
		this.cfDirectory.setHandler(str -> {
			File file = DialogsHelper.showDirChooseDialog("Choose start directory");
			Optional.ofNullable(file).map(File::getAbsolutePath).map(Common::getRelativePath).ifPresent(this.cfDirectory::setText);
		});

		this.listeners();
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	void init(Search model)
	{
		this.model = model;
		this.lvResult.setCellFactory(e -> new SearchResultCell(model));
	}

	void updateFromSettings(List<String> masks, List<String> texts)
	{
		String oldMask = this.cbFileMask.getSelectionModel().getSelectedItem();
		this.cbFileMask.getItems().setAll(masks);
		this.cbFileMask.getSelectionModel().select(oldMask);

		String oldText = this.cbFind.getSelectionModel().getSelectedItem();
		this.cbFind.getItems().setAll(texts);
		this.cbFind.getSelectionModel().select(oldText);
	}

	void show()
	{
		this.alert = new Alert(Alert.AlertType.INFORMATION);
		this.alert.getDialogPane().getScene().getStylesheets().addAll(Common.currentThemesPaths());
		this.alert.getDialogPane().setHeader(new Label());
		Common.addIcons(((Stage) this.alert.getDialogPane().getScene().getWindow()));
		this.alert.getDialogPane().setBorder(new Border(new BorderStroke(Common.currentTheme().getReverseColor(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)));
		this.alert.initModality(Modality.APPLICATION_MODAL);
		this.alert.initStyle(StageStyle.UNDECORATED);
		DialogPane dp = this.alert.getDialogPane();
		dp.setContent(this.parent);
		this.alert.setOnHiding(event -> this.model.alertClose());
		this.alert.setTitle("Search");
		this.alert.show();
		Node node = this.alert.getDialogPane().lookupButton(ButtonType.OK);
		if (node != null)
		{
			((Button) node).setDefaultButton(false);
		}
		Common.setFocused(this.cbFind.getEditor());
	}

	public void find(ActionEvent actionEvent)
	{
		this.model.find(
				this.cbFileMask.getEditor().getText()
				, this.cbFind.getEditor().getText()
				, this.cbCaseSensitive.isSelected()
				, this.cbWholeWord.isSelected()
				, this.cbRegexp.isSelected()
				, this.cbMatrix.isSelected()
				, this.cbLibs.isSelected()
				, this.cbGuiDic.isSelected()
				, this.cbClientDic.isSelected()
				, this.cbVariables.isSelected()
				, this.cbFiles.isSelected()
				, this.cfDirectory.getText()
		);
	}

	void displayResult(List<SearchResult> list)
	{
		if (list == null || list.isEmpty())
		{
			return;
		}
		synchronized (this.lock)
		{
			this.lvResult.getItems().addAll(list);
		}
	}

	void startFind()
	{
		this.btnFind.setDisable(true);
		this.lblMatches.setText("Searching...");
		this.hBoxSearching.setVisible(true);
		this.lvResult.getItems().clear();
	}

	void finishFind()
	{
		this.hBoxSearching.setVisible(false);
		this.btnFind.setDisable(false);
	}

	void displayMatches()
	{
		int matches = this.lvResult.getItems().size();
		long files = this.lvResult.getItems().stream().map(SearchResult::getFile).distinct().count();
		this.lblMatches.setText(matches + " matches in " + files + " files");
	}

	//region private methods
	private void listeners()
	{
		Arrays.asList(this.cbCaseSensitive, this.cbRegexp, this.cbWholeWord, this.cbMatrix, this.cbLibs, this.cbGuiDic,this.cbClientDic,this.cbVariables, this.cbFiles)
				.forEach(cb -> cb.setOnKeyPressed(e -> this.consumeEvent(e, evt -> {})));

		this.cfDirectory.disableProperty().bind(this.cbFiles.selectedProperty().not());

		this.lvResult.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ESCAPE)
			{
				this.alert.hide();
			}
		});

		this.cbFind.getEditor().setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));
		this.cbFind.setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));

		this.cbFileMask.getEditor().setOnKeyPressed(e -> this.consumeEvent(e, ev -> this.find(null)));
		this.cbFileMask.setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));

		this.cfDirectory.setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));
	}

	private void consumeEvent(KeyEvent event, Consumer<KeyEvent> consumer)
	{
		if (event.getCode() == KeyCode.ENTER)
		{
			event.consume();
			consumer.accept(event);
		}
	}

	private class SearchResultCell extends ListCell<SearchResult>
	{
		private Search model;
		SearchResultCell(Search model)
		{
			this.model = model;
		}

		@Override
		protected void updateItem(SearchResult item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				BorderPane pane = new BorderPane();

				Label text = new Label(item.toString());
				text.setAlignment(Pos.CENTER_LEFT);
				text.setTextAlignment(TextAlignment.LEFT);
				HBox.setHgrow(text, Priority.ALWAYS);

				HBox box = new HBox();
				box.setAlignment(Pos.CENTER_RIGHT);

				File file = item.getFile();

				Button btnShowInTree = new Button();
				btnShowInTree.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
				btnShowInTree.setId("dictionaryBtnXpathHelper");
				btnShowInTree.setTooltip(new Tooltip("Scroll from configuration"));
				btnShowInTree.setOnAction(e -> this.model.scrollFromConfig(file));


				SplitMenuButton btnOpenAs = new SplitMenuButton();
				btnOpenAs.setText("Open");
				BorderPane.setAlignment(text, Pos.CENTER_LEFT);
				MenuItem asPlainText = new MenuItem("As plain text");
				asPlainText.setOnAction(e -> this.model.openAsPlainText(file));
				btnOpenAs.getItems().add(asPlainText);

				if (file.getName().endsWith("." + Matrix.class.getAnnotation(DocumentInfo.class).extentioin()))
				{
					MenuItem asMatrix = new MenuItem("As matrix");
					asMatrix.setOnAction(e -> this.model.openAsMatrix(file));
					btnOpenAs.getItems().add(asMatrix);
				}
				else if (file.getName().endsWith("." + GuiDictionary.class.getAnnotation(DocumentInfo.class).extentioin()))
				{
					MenuItem asGuiDic = new MenuItem("As gui dic");
					asGuiDic.setOnAction(e -> this.model.openAsGuiDic(file));
					btnOpenAs.getItems().add(asGuiDic);
				}
				else if (file.getName().endsWith("." + SystemVars.class.getAnnotation(DocumentInfo.class).extentioin()))
				{
					MenuItem asVars = new MenuItem("As vars");
					asVars.setOnAction(e -> this.model.openAsVars(file));
					btnOpenAs.getItems().add(asVars);
				}
				else if (file.getName().endsWith(".html"))
				{
					MenuItem asReport = new MenuItem("As report");
					asReport.setOnAction(e -> this.model.openAsHtml(file));
					btnOpenAs.getItems().add(asReport);
				}

				box.getChildren().addAll(btnOpenAs, btnShowInTree);
				pane.setCenter(text);
				pane.setRight(box);
				setGraphic(pane);

			}
			else
			{
				setGraphic(null);
			}
		}
	}
	//endregion
}