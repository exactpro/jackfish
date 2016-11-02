////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.settings;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.main.Main;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SettingsPanel
{
	public static final String	FONT			= "Font";
	public final static String	SETTINGS 		= "Main";
	public static final String	LOGS_NAME		= "Logs";
	public static final String	SHORTCUTS_NAME	= "Shortcuts";
	public static final String	MATRIX_COLORS	= "MatrixColors";
	public static final String	GIT				= "Git";

	//other shortcuts
	public static final String SHOW_ALL_TABS	= "ShowAllTabs";

	public static final List<String> otherList = createList(SHOW_ALL_TABS);

	//document shortcuts
	public static final String SAVE_DOCUMENT	= "SaveDocument";
	public static final String SAVE_DOCUMENT_AS	= "SaveDocumentAs";
	public static final String UNDO				= "Undo";
	public static final String REDO				= "Redo";

	public static final List<String> docsList = createList(
			SAVE_DOCUMENT,
			SAVE_DOCUMENT_AS,
			UNDO,
			REDO
	);

	//matrix navigation shortcuts
	public static final String ADD_CHILD		= "AddChild";
	public static final String ADD_BEFORE		= "AddBefore";
	public static final String ADD_AFTER		= "AddAfter";
	public static final String BREAK_POINT		= "BreakPoint";
	public static final String ADD_PARAMETER	= "AddParameter";
	public static final String HELP				= "Help";
	public static final String GO_TO_LINE		= "GoToLine";
	public static final String SHOW_ALL			= "ShowAll";
	public static final String DELETE_ITEM		= "DeleteItem";
	public static final String COPY_ITEMS		= "CopyItems";
	public static final String PASTE_ITEMS_CHILD = "PasteItemsToChild";
	public static final String PASTE_ITEMS_AFTER = "PasteItemsAfter";
	public static final String PASTE_ITEMS_BEFORE = "PasteItemsBefore";
	public static final String COLLAPSE_ALL		= "CollapseAll";
	public static final String COLLAPSE_ONE		= "CollapseOne";
	public static final String EXPAND_ALL		= "ExpandAll";
	public static final String EXPAND_ONE		= "ExpandOne";

	public static final List<String> matrixNavigationList = createList(
			ADD_CHILD,
			ADD_BEFORE,
			ADD_AFTER,
			BREAK_POINT,
			ADD_PARAMETER,
			HELP,
			GO_TO_LINE,
			SHOW_ALL,
			DELETE_ITEM,
			COPY_ITEMS,
			PASTE_ITEMS_CHILD,
			PASTE_ITEMS_AFTER,
			PASTE_ITEMS_BEFORE,
			COLLAPSE_ALL,
			COLLAPSE_ONE,
			EXPAND_ALL,
			EXPAND_ONE
	);

	//matrix actions shortcuts
	public static final String START_MATRIX		= "StartMatrix";
	public static final String STOP_MATRIX		= "StopMatrix";
	public static final String PAUSE_MATRIX		= "PauseMatrix";
	public static final String SHOW_RESULT		= "ShowResult";
	public static final String SHOW_WATCH		= "ShowWatch";
	public static final String TRACING			= "Tracing";
	public static final String FIND_ON_MATRIX	= "FindOnMatrix";

	public static final List<String> matrixActionsList = createList(
			START_MATRIX,
			STOP_MATRIX,
			PAUSE_MATRIX,
			SHOW_RESULT,
			SHOW_WATCH,
			TRACING,
			FIND_ON_MATRIX
	);

	//git
	public static final String GIT_SSH_IDENTITY	= "gitSshIdentity";
	public static final String GIT_KNOWN_HOST	= "gitKnownHost";


	private Settings settings;
	private SettingsPanelController controller;

	public SettingsPanel(Settings settings) throws IOException
	{
		this.settings = settings;
		this.controller = Common.loadController(getClass().getResource("Settings.fxml"));
		this.controller.init(this);
	}

	public void show()
	{
		displayGit();
		displayColors();
		displayLogs();
		displayShortcuts();
		displayMain();
		this.controller.display("Settings");
	}

	private void displayGit()
	{
		List<SettingsValue> values = this.settings.getValues(Settings.GLOBAL_NS, GIT);
		this.controller.displayGit(values.stream().collect(Collectors.toMap(SettingsValue::getKey, SettingsValue::getValue)));
	}

	private void displayColors()
	{
		List<SettingsValue> values = this.settings.getValues(Settings.GLOBAL_NS, MATRIX_COLORS);
		this.controller.displayColors(values.stream().collect(Collectors.toMap(SettingsValue::getKey, SettingsValue::getValue)));
	}

	private void displayMain()
	{
		this.controller.displayMain(settings.getValues(Settings.GLOBAL_NS, SETTINGS)
				.stream()
				.collect(Collectors.toMap(SettingsValue::getKey, SettingsValue::getValue)));
	}

	private void displayLogs()
	{
		Collection<SettingsValue> values = settings.getValues(Settings.GLOBAL_NS, LOGS_NAME);
		Map<String, String> res = new LinkedHashMap<>();
		values.forEach(value -> res.put(value.getKey(), value.getValue()));
		this.controller.displayLogs(res);

	}

	private void displayShortcuts()
	{
		List<SettingsValue> values = settings.getValues(Settings.GLOBAL_NS, SHORTCUTS_NAME);

		Function<String, String> get = (key) -> values.stream()
				.filter(sv -> sv.getKey().equals(key))
				.map(SettingsValue::getValue)
				.findFirst()
				.orElse(Common.EMPTY);

		Function<List<String>, Map<String, String>> mapFunction = list -> list.stream().collect(Collectors.toMap(k -> k, get));

		Map<String, String> docs = mapFunction.apply(docsList);
		Map<String, String> matrixNav = mapFunction.apply(matrixNavigationList);
		Map<String, String> matrixAct = mapFunction.apply(matrixActionsList);
		Map<String, String> other = mapFunction.apply(otherList);
		this.controller.displayShortcuts(docs, matrixNav, matrixAct, other);
	}

	public void updateSettingsValue(String key, String dialog, String newValue)
	{
		if (newValue.equals(Common.EMPTY))
		{
			this.settings.remove(Settings.GLOBAL_NS, dialog, key);
		}
		else
		{
			this.settings.setValue(Settings.GLOBAL_NS, dialog, key, newValue);
		}
	}

	public void save() throws Exception
	{
		this.settings.saveIfNeeded();
		Settings.SettingsValue theme = this.settings.getValueOrDefault(Settings.GLOBAL_NS, SETTINGS, Main.THEME, Theme.WHITE.name());
		Common.setTheme(Theme.valueOf(theme.getValue().toUpperCase()));
	}

	public String nameOtherShortcut(String value, String currentKey)
	{
		return this.settings.getValues(Settings.GLOBAL_NS, SHORTCUTS_NAME).stream()
				.filter(sv -> sv.getValue().equals(value))
				.map(SettingsValue::getKey)
				.filter(key -> !key.equals(currentKey))
				.findFirst()
				.orElse(null);
	}

	public void removeAll(String dialog)
	{
		this.settings.removeAll(Settings.GLOBAL_NS, dialog);
	}

	public static boolean match(Settings settings, KeyEvent event, String shortcutName)
	{
		List<SettingsValue> values = settings.getValues(Settings.GLOBAL_NS, SHORTCUTS_NAME);
		Optional<SettingsValue> first = values.stream().filter(value -> value.getKey().equals(shortcutName)).findFirst();
		return first.isPresent() && KeyCodeCombination.valueOf(first.get().getValue()).match(event);
	}

	public static String getShortcutName(Settings settings, String shortcutName)
	{
		SettingsValue value = settings.getValue(Settings.GLOBAL_NS, SHORTCUTS_NAME, shortcutName);
		if (value == null)
		{
			return "";
		}
		return " <" + value.getValue() + ">";
	}

	private static List<String> createList(String... args)
	{
		List<String> list = new ArrayList<>();
		Arrays.stream(args).forEach(list::add);
		return list;
	}
}