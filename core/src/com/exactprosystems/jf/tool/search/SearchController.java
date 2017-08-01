package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SearchController implements Initializable, ContainingParent
{
	private final Object lock = new Object();

	private Parent parent;

	public GridPane               mainGridPane;
	public ComboBox<String>       cbFileMask;
	public ComboBox<String>       cbFind;
	public CheckBox               cbCaseSensitive;
	public CheckBox               cbRegexp;
	public CheckBox               cbWholeWord;
	public CheckBox               cbMatrix;
	public CheckBox               cbLibs;
	public CheckBox               cbGuiDic;
	public CheckBox               cbClientDic;
	public CheckBox               cbVariables;
	public CheckBox               cbInsideProject;
	public Label                  lblMatches;
	public Button                 btnFind;
	public ListView<SearchResult> lvResult;
	public HBox                   hBoxSearching;

	public VBox     textPane;
	public VBox     resultsPane;
	public VBox     maskPane;

	private Search model;
	private Alert  alert;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		Node fileMask = BorderWrapper.wrap(this.maskPane).title("File mask and scope").color(Common.currentTheme().getReverseColor()).outerPadding(4).innerPadding(4).build();
		this.mainGridPane.add(fileMask, 0, 0);

		Node textPane = BorderWrapper.wrap(this.textPane).title("Containing text").color(Common.currentTheme().getReverseColor()).outerPadding(4).innerPadding(4).build();
		this.mainGridPane.add(textPane, 0, 1);

		Node results = BorderWrapper.wrap(this.resultsPane).title("Results").color(Common.currentTheme().getReverseColor()).outerPadding(4).innerPadding(4).build();
		this.mainGridPane.add(results, 0, 2);

		this.cbFileMask.getItems().add(Search.ALL_FILES);
		this.cbFileMask.getSelectionModel().selectFirst();

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
		this.alert.initModality(Modality.WINDOW_MODAL);
		Common.addIcons(((Stage) this.alert.getDialogPane().getScene().getWindow()));
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
		List<DocumentKind> kinds = new ArrayList<>();
		add(this.cbMatrix, DocumentKind.MATRIX, kinds);
		add(this.cbLibs, DocumentKind.LIBRARY, kinds);
		add(this.cbGuiDic, DocumentKind.GUI_DICTIONARY, kinds);
		add(this.cbClientDic, DocumentKind.MESSAGE_DICIONARY, kinds);
		add(this.cbVariables, DocumentKind.SYSTEM_VARS, kinds);
		add(this.cbInsideProject, DocumentKind.PLAIN_TEXT, kinds);

		this.model.find(this.cbFileMask.getEditor().getText(), this.cbFind.getEditor().getText()
				, this.cbCaseSensitive.isSelected(), this.cbWholeWord.isSelected(), this.cbRegexp.isSelected()
				, kinds.toArray(new DocumentKind[kinds.size()])
		);
//		this.model.find(this.cbFileMask.getEditor().getText(), this.cbFind.getEditor().getText(), this.cbCaseSensitive.isSelected(), this.cbWholeWord.isSelected(), this.cbRegexp.isSelected(),
//				this.cbMatrix.isSelected(), this.cbLibs.isSelected(), this.cbGuiDic.isSelected(), this.cbClientDic.isSelected(), this.cbVariables.isSelected(), this.cbInsideProject.isSelected(),
//				this.cfDirectory.getText());
	}

	private void add(CheckBox cb, DocumentKind kind, List<DocumentKind> kinds)
	{
		if (cb.isSelected())
		{
			kinds.add(kind);
		}
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
		this.lblMatches.setText("");
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
		Arrays.asList(this.cbCaseSensitive, this.cbRegexp, this.cbWholeWord, this.cbMatrix, this.cbLibs, this.cbGuiDic, this.cbClientDic, this.cbVariables, this.cbInsideProject)
				.forEach(cb -> cb.setOnKeyPressed(e -> this.consumeEvent(e, evt -> {})));

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
				BorderPane.setAlignment(text, Pos.CENTER_LEFT);

				HBox box = new HBox();
				box.setAlignment(Pos.CENTER_RIGHT);
				File file = item.getFile();
				if (file != null)
				{
					Button btnShowInTree = new Button();
					btnShowInTree.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					btnShowInTree.setId("dictionaryBtnXpathHelper");
					btnShowInTree.setTooltip(new Tooltip("Scroll from configuration"));
					btnShowInTree.setOnAction(e -> this.model.scrollFromConfig(file));

					Button btnOpenAsPlainText = new Button();
					btnOpenAsPlainText.setId("btnOpenAsPlainText");
					btnOpenAsPlainText.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);
					btnOpenAsPlainText.setTooltip(new Tooltip("Open as plain text"));
					btnOpenAsPlainText.setOnAction(e -> this.model.openAsPlainText(file));

					boolean needAdd = true;
					Consumer<File> consumer = null;

					switch (item.getKind())
					{
						case MATRIX:
						case LIBRARY:
							consumer = this.model::openAsMatrix;
							break;
						case GUI_DICTIONARY:
							consumer = this.model::openAsGuiDic;
							break;
						case SYSTEM_VARS:
							consumer = this.model::openAsVars;
							break;
						default:
							if (file.getName().endsWith(".html"))
							{
								consumer = this.model::openAsHtml;
							}
							else
							{
								needAdd = false;
							}
					}
					if (needAdd)
					{
						Button btnOpenAsDocument = new Button();
						btnOpenAsDocument.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);
						btnOpenAsDocument.setId("btnOpenAsDocument");
						btnOpenAsDocument.setTooltip(new Tooltip("Open as document"));
						Consumer<File> finalConsumer = consumer;
						btnOpenAsDocument.setOnAction(e -> finalConsumer.accept(file));
						box.getChildren().addAll(btnOpenAsDocument, Common.createSpacer(Common.SpacerEnum.HorizontalMin));
					}
					box.getChildren().addAll(btnOpenAsPlainText, new Separator(Orientation.VERTICAL), btnShowInTree);
				}

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