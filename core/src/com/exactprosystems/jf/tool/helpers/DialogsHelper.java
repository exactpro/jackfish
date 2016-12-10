////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.helpers;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.api.common.ApiVersionInfo;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.HelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.*;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.Notifications;
import com.exactprosystems.jf.tool.custom.UserInputDialog;
import com.exactprosystems.jf.tool.custom.browser.ReportBrowser;
import com.exactprosystems.jf.tool.custom.date.DateTimePicker;
import com.exactprosystems.jf.tool.custom.date.DateTimePickerSkin;
import com.exactprosystems.jf.tool.custom.helper.HelperFx;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public abstract class DialogsHelper
{
	private static int timeNotification = 10;

	@FunctionalInterface
	public interface ListViewPanel<T>
	{
		void select(T t);
	}

	public enum OpenSaveMode
	{
		OpenFile, SaveFile
	}
	
	public static ButtonType showParametersDialog(String title, final Map<String, String> parameters, AbstractEvaluator evaluator)
	{
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.getDialogPane().setPrefHeight(500);
		dialog.getDialogPane().setPrefWidth(500);
		dialog.setTitle("Parameters");
		ButtonType btnYes = new ButtonType(title, ButtonBar.ButtonData.YES);
		dialog.getDialogPane().getButtonTypes().addAll(btnYes);
		ListView<ExpressionFieldsPane> listView = new ListView<>();
		dialog.getDialogPane().setContent(listView);
		parameters.entrySet().forEach(entry -> listView.getItems().addAll(new ExpressionFieldsPane(entry.getKey(), entry.getValue(), evaluator)));
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			ObservableList<ExpressionFieldsPane> items = listView.getItems();
			items.forEach(item -> parameters.put(item.getKey().getText(), item.getValue().getText()));
			return buttonType.get();
		}
		return ButtonType.CANCEL;
	}

	public static ButtonType showFileChangedDialog(String fileName)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.setTitle("Warning");
		dialog.getDialogPane().setHeaderText("File " + fileName + " was changed by another process");
		dialog.getDialogPane().setContentText("Reload it?");
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			return buttonType.get();
		}
		return ButtonType.CANCEL;
	}

	public static Date showDateTimePicker(Date initialValue)
	{
		DateTimePicker picker = new DateTimePicker(initialValue);
		DateTimePickerSkin skin = new DateTimePickerSkin(picker);
		Node popupContent = skin.getPopupContent();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.getDialogPane().setContent(popupContent);
		alert.setTitle("Select date");
		alert.setHeaderText("Choose date");
		alert.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = alert.showAndWait();
		Optional<ButtonType> btnOk = buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE));
		if (btnOk.isPresent())
		{
			return picker.getDate();
		}
		return initialValue;
	}

	
	public static ButtonType showQuestionDialog(String header, String body)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.setTitle("Warning");
		dialog.getDialogPane().setHeaderText(header);
		dialog.getDialogPane().setContentText(body);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			return buttonType.get();
		}
		return ButtonType.CANCEL;
	}

	public static ButtonType showSaveFileDialog(String fileName)
	{
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Save");
		dialog.getDialogPane().setHeaderText("File " + fileName + " was changed.");
		dialog.getDialogPane().setContentText("Do you want to save?");
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> res = dialog.showAndWait();
		if (res.isPresent())
		{
			return res.get();
		}
		return ButtonType.CANCEL;
	}

	public static <T> void showFindListView(final List<T> list, String title, final ListViewPanel<T> listener)
	{
		if (list == null || list.isEmpty())
		{
			showInfo("Nothing to show");
			return;
		}
		if (list.size() == 1)
		{
			listener.select(list.get(0));
			return;
		}
		ArrayList<T> tempList = new ArrayList<>(list);
		BorderPane pane = new BorderPane();
		ListView<T> listView = new ListView<>(FXCollections.observableList(tempList));
		pane.setCenter(listView);
		BorderPane.setAlignment(listView, Pos.CENTER);
		TextField tf = new TextField();
		Platform.runLater(tf::requestFocus);
		pane.setTop(tf);
		BorderPane.setAlignment(tf, Pos.CENTER);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.setHeaderText(title);
		dialog.getDialogPane().setContent(pane);
		dialog.setResizable(true);
		dialog.getDialogPane().getContent().autosize();
		listView.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getClickCount() == 2)
			{
				T selectedItem = listView.getSelectionModel().getSelectedItem();
				listener.select(selectedItem);
				dialog.close();
			}
		});

		listView.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t -> {
					listener.select(t);
					dialog.close();
				});
			}
		});

		tf.textProperty().addListener((observableValue, s, t1) -> {
			if (t1.isEmpty())
			{
				listView.getItems().addAll(list);
			}
			listView.getItems().clear();
			list.stream().filter(t -> t.toString().toUpperCase().contains(t1.toUpperCase())).forEach(listView.getItems()::add);
		});

		tf.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER && listView.getItems().size() == 1)
			{
				listener.select(listView.getItems().get(0));
				dialog.close();
			}
			if (keyEvent.getCode() == KeyCode.DOWN)
			{
				listView.requestFocus();
				listView.getFocusModel().focus(0);
			}
		});
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> optional = dialog.showAndWait();
		optional.ifPresent(o -> {
			if (o.getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(listener::select);
			}
		});
	}

	public static <T> T selectFromList(String title, T initValue, final List<T> list)
	{
		@SuppressWarnings("unchecked")
		T[] result = (T[]) new Object[] { initValue };

		if (list == null || list.isEmpty())
		{
			showInfo("Nothing to show");
			return result[0];
		}
		if (list.size() == 1)
		{
			return list.get(0);
		}
		ArrayList<T> tempList = new ArrayList<>(list);
		BorderPane pane = new BorderPane();
		ListView<T> listView = new ListView<>(FXCollections.observableList(tempList));
		pane.setCenter(listView);
		TextField tf = new TextField();
		pane.setTop(tf);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.getDialogPane().setPrefWidth(500);
		dialog.setHeaderText(title);
		dialog.getDialogPane().setContent(pane);
		dialog.setResizable(true);
		dialog.getDialogPane().getContent().autosize();

		listView.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getClickCount() == 2)
			{
				T selectedItem = listView.getSelectionModel().getSelectedItem();
				result[0] = selectedItem;
				dialog.close();
			}
		});

		listView.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t -> {
					result[0] = t;
					dialog.close();
				});
			}
		});

		tf.textProperty().addListener((observableValue, s, t1) -> {
			if (t1.isEmpty())
			{
				listView.getItems().addAll(list);
			}
			listView.getItems().clear();
			list.stream().filter(t -> t.toString().toUpperCase().contains(t1.toUpperCase())).forEach(t -> listView.getItems().add(t));
		});

		tf.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER && listView.getItems().size() == 1)
			{
				result[0] = listView.getItems().get(0);
				dialog.close();
			}
			if (keyEvent.getCode() == KeyCode.DOWN)
			{
				listView.requestFocus();
				listView.getFocusModel().focus(0);
			}
		});
		Platform.runLater(tf::requestFocus);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		if (buttonType.isPresent())
		{
			if (buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				Optional.ofNullable(listView.getSelectionModel().getSelectedItem()).ifPresent(t -> result[0] = t);
			}
			return result[0];
		}
		return result[0];
	}

	public static File showOpenSaveDialog(String title, String filter, String extension, OpenSaveMode mode)
	{
		String pathToParentDir = getCurrentDir();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(pathToParentDir));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filter, extension);
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle(title);
		switch (mode)
		{
			case SaveFile:
				return fileChooser.showSaveDialog(Common.node);

			case OpenFile:
				return fileChooser.showOpenDialog(Common.node);

			default:
				throw new RuntimeException("Unsupported mode" + mode);
		}
	}

	public static List<File> showMultipleDialog(String title, String filter, String extension)
	{
		String pathToParentDir = getCurrentDir();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(pathToParentDir));
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filter, extension);
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle(title);
		return fileChooser.showOpenMultipleDialog(Common.node);
	}

	public static File showDirChooseDialog(String title)
	{
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File(getCurrentDir()));
		chooser.setTitle(title);
		return chooser.showDialog(Common.node);
	}

	public static File showDirChooseDialog(String title, String initialDirectory)
	{
		DirectoryChooser chooser = new DirectoryChooser();
		File value = new File(initialDirectory);
		if (!value.exists())
		{
			value = new File(getCurrentDir());
		}
		chooser.setInitialDirectory(value);
		chooser.setTitle(title);
		return chooser.showDialog(Common.node);
	}

	public static File showSaveAsDialog(Document doc) throws Exception
	{
		Class<?> docClass = doc.getClass();
		DocumentInfo annotation;
		while ((annotation = docClass.getAnnotation(DocumentInfo.class)) == null && docClass.getSuperclass() != null)
		{
			docClass = docClass.getSuperclass();
		}
		if (annotation == null)
		{
			throw new Exception("Unknown type of document: " + docClass);
		}
		String title = "Save " + docClass.getSimpleName().toLowerCase();
		String filter = annotation.extentioin() + " files(*."+annotation.extentioin()+")";
		String extension = "*." + annotation.extentioin();
		String ext = "." + annotation.extentioin();
		File file = showOpenSaveDialog(title, filter, extension, OpenSaveMode.SaveFile);
		if (file != null)
		{
			String path = file.getPath();
			if (!path.endsWith(ext))
			{
				file = new File(path + ext);
			}
		}
		return file;
	}

	public static String showHelperDialog(String title, AbstractEvaluator evaluator, String value, Matrix matrix)
	{
		try
		{
			HelperFx helper = new HelperFx(title, evaluator, matrix);
			return helper.showAndWait(value);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			showError(e.getMessage());
		}
		return value;
	}

	public static String showUserInput(AbstractEvaluator evaluator, String title, Object defaultValue, Integer timeout, ActionItem.HelpKind helpKind, Collection<?> dataSource)
	{
		final Dialog<String>[] dialog = new Dialog[1];
		Task<String> task = new Task<String>()
		{
			@Override
			protected String call() throws Exception
			{
				String literal = Common.createLiteral(defaultValue, evaluator);
				dialog[0] = new UserInputDialog(literal, evaluator, helpKind, dataSource);
				dialog[0].setTitle("Input");
				dialog[0].setHeaderText(title);
				Optional<String> s = dialog[0].showAndWait();
				return s.orElse(literal);
			}
		};
		task.setOnCancelled(e -> dialog[0].hide());
		Platform.runLater(task);
		String res = Common.createLiteral(defaultValue, evaluator);
		try
		{
			res = task.get(timeout, TimeUnit.MILLISECONDS);
		}
		catch (Exception e)
		{
			task.cancel();
		}
		return res;
	}

	public static void setTimeNotification(int timeNotification)
	{
		DialogsHelper.timeNotification = timeNotification;
	}

	public static void showError(final String message)
	{
		showNotifier(message, Notifier.Error);
	}

	public static void showSuccess(final String message)
	{
		showNotifier(message, Notifier.Success);
	}
	
	public static void showInfo(final String message)
	{
		showNotifier(message, Notifier.Info);
	}

	public static void showAboutProgram()
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setTitle("About program");
		dialog.getDialogPane().setPrefWidth(600);
		dialog.getDialogPane().setPrefHeight(250);
		dialog.setResizable(true);
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		String format = String.format("Version : %25s %nApi version : %5s", VersionInfo.getVersion(), ApiVersionInfo.majorVersion() + "." + ApiVersionInfo.minorVersion());
		Text text = new Text(format);
		text.setFont(javafx.scene.text.Font.font(15));
		box.getChildren().addAll(text, new Button("Copy version"));
		dialog.getDialogPane().setHeaderText(format);
		dialog.getDialogPane().setContentText("Copyright (c) 2009-2015, Exactpro Systems, LLC\n" +
				"Quality Assurance & Related Development for Innovative Trading Systems.\n" +
				"All rights reserved.\n" +
				"This is unpublished, licensed software, confidential and proprietary\n" +
				"information which is the property of Exactpro Systems, LLC or its licensors.");
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.show();
	}

	public static void showActionsHelp(DocumentFactory factory)
	{
		try(Context context = factory.createContext())
		{
			Matrix matrix = factory.createMatrix("helpMatrix");

			MatrixItem syntax = new HelpChapter("Matrix syntax");
			syntax.init(matrix);
			int ch = 0;
			matrix.insert(null, ch++, syntax);
			int i = 0;
			syntax.insert(i++, new NameSpace());
			syntax.insert(i++, new TestCase());
			syntax.insert(i++, new Step());
			syntax.insert(i++, new Let());
			syntax.insert(i++, new Assert());
			syntax.insert(i++, new SubCase());
			syntax.insert(i++, new Return());
			syntax.insert(i++, new Call());
			syntax.insert(i++, new If());
			syntax.insert(i++, new Else());
			syntax.insert(i++, new For());
			syntax.insert(i++, new ForEach());
			syntax.insert(i++, new While());
			syntax.insert(i++, new Continue());
			syntax.insert(i++, new Break());
			syntax.insert(i++, new OnError());
			syntax.insert(i++, new Switch());
			syntax.insert(i++, new Case());
			syntax.insert(i++, new Default());
			syntax.insert(i++, new ReportOn());
			syntax.insert(i++, new ReportOff());
			syntax.insert(i++, new Fail());
			syntax.insert(i++, new RawTable());
			syntax.insert(i++, new RawMessage());
			syntax.insert(i++, new RawText());

			MatrixItem actions = new HelpChapter("All actions by groups");
			matrix.insert(null, ch++, actions);

			i = 0;
			Map<ActionGroups, MatrixItem> map = new HashMap<>();
			for (ActionGroups actionGroups : ActionGroups.values())
			{
				MatrixItem chapter = new HelpChapter(actionGroups.name());
				chapter.init(matrix);
				actions.insert(actions.count(), chapter);
				map.put(actionGroups, chapter);
			}

			i = 0;
			for (Class<?> action : ActionsList.actions)
			{
				ActionGroups actionGroups = action.getAnnotation(ActionAttribute.class).group();
				MatrixItem chapter = map.get(actionGroups);
				ActionItem actionItem = new ActionItem(action.getSimpleName());
				actionItem.init(matrix);
				chapter.insert(chapter.count(), actionItem);
			}
			ReportBuilder report = new HelpFactory().createBuilder(null, null, new Date());
			matrix.documentation(context, report);
			displayHelp(report.getContent());
		}
		catch (Exception e)
		{
			String message = "Error on .\n" + e.getMessage();
			logger.error(message);
			logger.error(e.getMessage(), e);
			DialogsHelper.showError(message);
		}
	}

	public static void showAppHelp(String help)
	{
		WebView browser = new WebView();
		WebEngine engine = browser.getEngine();
		engine.loadContent(help);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setResizable(true);
		dialog.getDialogPane().setPrefWidth(1024);
		dialog.getDialogPane().setPrefHeight(768);
		dialog.getDialogPane().setContent(browser);
		dialog.initModality(Modality.NONE);
		dialog.getDialogPane().setHeader(new Label());
		dialog.setTitle("Help");
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.show();
	}

	public static void displayReport(File file, String matrixName, DocumentFactory factory)
	{
	    Platform.runLater(() -> 
	    {
    		final String[] matrName = {matrixName};
    		tryCatch(() -> {
    			Configuration configuration = factory.getConfiguration();
    			boolean addButton = configuration != null;
    			ReportBrowser reportBrowser = new ReportBrowser(file);
    			Dialog<ButtonType> dialog = new Dialog<>();
    			if (addButton)
    			{
    				dialog.getDialogPane().getButtonTypes().add(new ButtonType("Open", ButtonBar.ButtonData.OTHER));
    			}
    			dialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));
    			dialog.setResizable(true);
    			dialog.getDialogPane().setPrefWidth(1024);
    			dialog.getDialogPane().setPrefHeight(768);
    			dialog.getDialogPane().setContent(reportBrowser);
    			dialog.initModality(Modality.NONE);
    			dialog.setTitle("Report");
    			if (matrName[0] == null)
    			{
    				Matcher matcher = Pattern.compile("\\d+_\\d+_(.+?)_(FAILED|PASSED|RUNNING)\\.html").matcher(file.getAbsolutePath());
    				if (matcher.find())
    				{
    					matrName[0] = matcher.group(1);
    				}
    				else
    				{
    					matrName[0] = "Unknown matrix";
    				}
    			}
    			dialog.setHeaderText("Report for " + matrName[0]);
    			dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
    			Optional<ButtonType> buttonType = dialog.showAndWait();
    			buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OTHER)).ifPresent(type -> Common.tryCatch(() -> {
    				String name = reportBrowser.getMatrix();
    				if (name != null && !name.isEmpty())
    				{
    					Matrix matrix = factory.createMatrix(matrName[0]);
    					matrix.load(new StringReader(name));
    					matrix.display();
    				}
    			}, "Error on open matrix from report"));
    		}, "Error on show report");
	    } );
	}

	public static void showNotifier(final String message, final Notifier notifier)
	{
		Platform.runLater(() -> Notifications
				.create()
				.msg(message)
				.hideAfter(Duration.seconds(timeNotification))
				.state(notifier)
				.title(notifier.name())
				.show());
	}

	public static Alert createGitDialog(String title, Parent parent)
	{
		Alert dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setResult(new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE));
		dialog.setResizable(true);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.setTitle(title);
		Label header = new Label();
		header.setMinHeight(0.0);
		header.setPrefHeight(0.0);
		header.setMaxHeight(0.0);
		dialog.getDialogPane().setHeader(header);
		dialog.getDialogPane().setContent(parent);
		ButtonType buttonCreate = new ButtonType("", ButtonBar.ButtonData.OTHER);
		dialog.getButtonTypes().setAll(buttonCreate);
		Button button = (Button) dialog.getDialogPane().lookupButton(buttonCreate);
		button.setPrefHeight(0.0);
		button.setMaxHeight(0.0);
		button.setMinHeight(0.0);
		button.setVisible(false);
		return dialog;
	}

	public static boolean showYesNoDialog(String message, String question)
	{
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.setHeaderText(question);
		dialog.getDialogPane().setPrefWidth(1000);
		dialog.setContentText(message);
		((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
		return dialog.showAndWait().filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).isPresent();
	}

	private static void displayHelp(String content)
	{
		WebView browser = new WebView();
		WebEngine engine = browser.getEngine();
		engine.loadContent(content);

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(browser);
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setHeaderText("Help");
		GridPane grid = (GridPane)dialog.getDialogPane().lookup(".header-panel");
		grid.setStyle("-fx-font-size: 30;");
		dialog.setTitle("Actions help");
		dialog.setResizable(true);
		dialog.getDialogPane().setContent(borderPane);
		dialog.getDialogPane().setPrefWidth(1024);
		dialog.getDialogPane().setPrefHeight(768);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.initModality(Modality.NONE);
		dialog.show();
	}

	private static String getCurrentDir()
	{
		return Paths.get("").toAbsolutePath().toString();
	}


	private static final Logger logger = Logger.getLogger(DialogsHelper.class);
}
