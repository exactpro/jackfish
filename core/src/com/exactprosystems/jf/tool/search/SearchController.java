package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.search.results.AbstractResult;
import com.exactprosystems.jf.tool.search.results.AggregateResult;
import com.exactprosystems.jf.tool.search.results.FailedResult;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchController implements Initializable, ContainingParent
{
	private final Object lock = new Object();

	private Parent parent;

	public GridPane               mainGridPane;
	public ComboBox<String>       cbFileMask;
	public ComboBox<String>       cbFind;
	public CheckBox               cbCaseSensitive;
	public CheckBox               cbRegexp;
	public CheckBox               cbMultiLine;
	public CheckBox               cbWholeWord;
	public CheckBox               cbMatrix;
	public CheckBox               cbLibs;
	public CheckBox               cbGuiDic;
	public CheckBox               cbClientDic;
	public CheckBox               cbVariables;
	public CheckBox               cbOtherFiles;
	public CheckBox               cbReports;
	public Label                  lblMatches;
	public Button                 btnFind;
	public HBox                   hBoxSearching;

	public TreeView<AbstractResult> tvResults;
	public HBox                     hBoxResult;

	public VBox textPane;
	public VBox resultsPane;
	public VBox maskPane;

	private Search model;
	private Alert  alert;

	private Map<CheckBox, DocumentKind> map = new HashMap<>();

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		Node fileMask = BorderWrapper.wrap(this.maskPane).title(R.SEARCH_FILE_MASK_AND_SCOPE.get()).color(Theme.currentTheme().getReverseColor()).outerPadding(8).innerPadding(8).build();
		this.mainGridPane.add(fileMask, 0, 0);

		Node textPane = BorderWrapper.wrap(this.textPane).title(R.SEARCH_CONTAINING_TEXT.get()).color(Theme.currentTheme().getReverseColor()).outerPadding(8).innerPadding(8).build();
		this.mainGridPane.add(textPane, 0, 1);

		Node results = BorderWrapper.wrap(this.resultsPane).title(R.SEARCH_RESULTS.get()).color(Theme.currentTheme().getReverseColor()).outerPadding(8).innerPadding(8).build();
		this.mainGridPane.add(results, 0, 2);

		this.cbFileMask.getItems().add(Search.ALL_FILES);
		this.cbFileMask.getSelectionModel().selectFirst();

		this.map.put(this.cbMatrix, DocumentKind.MATRIX);
		this.map.put(this.cbLibs, DocumentKind.LIBRARY);
		this.map.put(this.cbGuiDic, DocumentKind.GUI_DICTIONARY);
		this.map.put(this.cbClientDic, DocumentKind.MESSAGE_DICTIONARY);
		this.map.put(this.cbVariables, DocumentKind.SYSTEM_VARS);
		this.map.put(this.cbReports, DocumentKind.REPORTS);
		this.map.put(this.cbOtherFiles, DocumentKind.PLAIN_TEXT);

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
		this.tvResults.setRoot(new TreeItem<>());
		this.tvResults.setShowRoot(false);
		this.tvResults.setCellFactory(param -> new SearchResultCellItem());
		this.tvResults.setId("searchResultsTree");
		this.tvResults.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			this.hBoxResult.getChildren().clear();
			if (newValue != null)
			{
				AbstractResult value = newValue.getValue();
				Node help;
				if (value != null && (help = value.help()) != null)
				{
					this.hBoxResult.getChildren().add(help);
				}
			}
		});
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
		this.alert.getDialogPane().getScene().getStylesheets().addAll(Theme.currentThemesPaths());
		this.alert.getDialogPane().setHeader(new Label());
		this.alert.initModality(Modality.WINDOW_MODAL);
		Common.addIcons(((Stage) this.alert.getDialogPane().getScene().getWindow()));
		DialogPane dp = this.alert.getDialogPane();
		dp.setContent(this.parent);
		this.alert.setOnHiding(event -> this.model.alertClose());
		this.alert.setTitle(R.SEARCH_SEARCH.get());
		this.alert.show();
		Node node = this.alert.getDialogPane().lookupButton(ButtonType.OK);
		if (node != null)
		{
			((Button) node).setDefaultButton(false);
		}
		Common.setFocusedFast(this.cbFileMask.getEditor());
	}

	public void find(ActionEvent actionEvent)
	{
		this.model.find(this.cbFileMask.getEditor().getText(), this.cbFind.getEditor().getText()
				, this.cbCaseSensitive.isSelected(), this.cbWholeWord.isSelected(), this.cbRegexp.isSelected(), this.cbMultiLine.isSelected()
				, this.map.entrySet().stream()
						.filter(entry -> entry.getKey().isSelected())
						.map(Map.Entry::getValue)
						.collect(Collectors.toList()).toArray(new DocumentKind[0])
		);
	}

	void displayFailedResult(FailedResult result)
	{
		this.tvResults.getRoot().getChildren().add(new TreeItem<>(result));
	}

	void displayResult(AggregateResult result)
	{
		if (result == null || result.isEmpty())
		{
			return;
		}
		TreeItem<AbstractResult> resultItem = new TreeItem<>(result);
		resultItem.setExpanded(false);
		result.getList().stream().map(sr -> (AbstractResult) sr).map(TreeItem::new).forEach(item -> resultItem.getChildren().add(item));
		synchronized (this.lock)
		{
			this.tvResults.getRoot().getChildren().add(resultItem);
		}
	}

	void startFind()
	{
		this.btnFind.setDisable(true);
		this.lblMatches.setText(R.SEARCH_SEARCHING.get());
		this.hBoxSearching.setVisible(true);
		this.tvResults.getRoot().getChildren().clear();
	}

	void finishFind()
	{
		this.lblMatches.setText("");
		this.hBoxSearching.setVisible(false);
		this.btnFind.setDisable(false);
	}

	void displayMatches()
	{
		long files = this.tvResults.getRoot().getChildren().size();
		long matches = this.tvResults.getRoot().getChildren().stream()
				.mapToLong(child -> child.getChildren().size())
				.sum();

		this.lblMatches.setText(MessageFormat.format(R.SEARCH_MATCHES_2.get(), matches, files));
	}

	//region private methods
	private void listeners()
	{
		this.map.keySet().forEach(cb -> cb.setOnKeyPressed(e -> this.consumeEvent(e, evt -> {})));
		Arrays.asList(this.cbCaseSensitive, this.cbRegexp, this.cbWholeWord, this.cbMultiLine).forEach(cb -> cb.setOnKeyPressed(e -> this.consumeEvent(e, evt -> {})));

		this.tvResults.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ESCAPE)
			{
				this.alert.hide();
			}
		});

		this.cbFind.getEditor().setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));
		this.cbFind.setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));

		this.cbFileMask.getEditor().setOnKeyPressed(e -> this.consumeEvent(e, ev -> this.find(null)));
		this.cbFileMask.setOnKeyPressed(event -> this.consumeEvent(event, e -> this.find(null)));

		this.cbRegexp.selectedProperty().addListener((observable, oldValue, newValue) -> {
			this.cbWholeWord.setDisable(newValue);
			if (newValue)
			{
				this.cbWholeWord.setSelected(false);
			}
		});
		this.cbMultiLine.selectedProperty().addListener((observable, oldValue, newValue) -> {
			this.cbRegexp.setDisable(newValue);
			if (newValue)
			{
				this.cbRegexp.setSelected(true);
			}
		});
	}

	private void consumeEvent(KeyEvent event, Consumer<KeyEvent> consumer)
	{
		if (event.getCode() == KeyCode.ENTER)
		{
			event.consume();
			consumer.accept(event);
		}
	}

	private static class SearchResultCellItem extends TreeCell<AbstractResult>
	{
		public SearchResultCellItem()
		{
		}

		@Override
		protected void updateItem(AbstractResult item, boolean empty)
		{
			super.updateItem(item,empty);
			setGraphic(item != null && !empty ? item.toView() : null);

		}
	}
}