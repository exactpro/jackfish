package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColorsTabController implements Initializable, ContainingParent, ITabHeight
{
	public Parent parent;
	private SettingsPanel model;

	public TreeView<NameAndColor> treeViewColors;
	public BorderPane colorsPane;

	private Map<String, Color> colorMatrixMap = new HashMap<>();

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		initialColorItems();
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	public void init(SettingsPanel model)
	{
		this.model = model;
	}

	public void displayInfo(Map<String, String> collect)
	{
		this.colorMatrixMap.putAll(collect.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Common.stringToColor(entry.getValue()))));
		workWithTree(this.treeViewColors.getRoot(), item -> {
			NameAndColor value = item.getValue();
			Optional.ofNullable(this.colorMatrixMap.get(value.name)).ifPresent(color -> {
				value.setColor(color);
				if (item.getParent() != null && !color.equals(Color.TRANSPARENT) && !item.getParent().isExpanded())
				{
					item.getParent().setExpanded(true);
				}
			});
		});
	}

	public void displayInto(Tab tab)
	{
		tab.setContent(this.parent);
		tab.setUserData(this);
	}

	@Override
	public double getHeight()
	{
		//TODO implement
		return -1;
	}

	public void save()
	{
		this.colorMatrixMap.entrySet().forEach(entry -> this.model.updateSettingsValue(entry.getKey(), SettingsPanel.MATRIX_COLORS, Common.colorToString(entry.getValue())));
	}

	//region private methods
	private void initialColorItems()
	{
		this.treeViewColors = new TreeView<>();
		TreeItem<NameAndColor> root = new TreeItem<>(new NameAndColor("root"));
		this.treeViewColors.setRoot(root);
		this.treeViewColors.setShowRoot(false);
		this.treeViewColors.setCellFactory(param -> new TreeCell<NameAndColor>()
		{
			@Override
			protected void updateItem(NameAndColor item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					BorderPane pane = new BorderPane();
					Label value = new Label(item.name);
					BorderPane.setAlignment(value, Pos.CENTER_LEFT);
					pane.setCenter(value);
					if (item.isChangeable)
					{
						ColorPicker color = new ColorPicker(item.color);
						color.setOnAction(e -> {
							item.color = e instanceof ClearAction ? Color.TRANSPARENT : color.getValue();
							colorMatrixMap.remove(item.name);
							if (!item.color.equals(Color.TRANSPARENT))
							{
								colorMatrixMap.put(item.name, item.color);
							}
							Optional.ofNullable(this.getTreeItem().getChildren()).ifPresent(child -> child.forEach(c -> {
								NameAndColor value1 = c.getValue();
								value1.color = item.color;
								c.setValue(null);
								c.setValue(value1);
								colorMatrixMap.put(value1.name, item.color);
							}));
							this.updateItem(item, false);
						});
						HBox box = new HBox();
						box.setAlignment(Pos.CENTER);
						box.setSpacing(10);
						box.getChildren().add(color);
						Button reset = new Button("Reset");
						reset.setOnAction(e -> color.getOnAction().handle(new ClearAction()));
						box.getChildren().add(reset);
						pane.setRight(box);
					}
					setGraphic(pane);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		NameAndColor actions = new NameAndColor("Actions");
		actions.isChangeable = false;
		TreeItem<NameAndColor> actionItem = createItem(actions);
		root.getChildren().addAll(actionItem,
				createItem(new NameAndColor(Tokens.NameSpace.name())),
				createItem(new NameAndColor(Tokens.Step.name())),
				createItem(new NameAndColor(Tokens.TestCase.name())),
				createItem(new NameAndColor(Tokens.SubCase.name())),
				createItem(new NameAndColor(Tokens.Return.name())),
				createItem(new NameAndColor(Tokens.Call.name())),
				createItem(new NameAndColor(Tokens.If.name())),
				createItem(new NameAndColor(Tokens.Else.name())),
				createItem(new NameAndColor(Tokens.For.name())),
				createItem(new NameAndColor(Tokens.ForEach.name())),
				createItem(new NameAndColor(Tokens.While.name())),
				createItem(new NameAndColor(Tokens.Continue.name())),
				createItem(new NameAndColor(Tokens.Break.name())),
				createItem(new NameAndColor(Tokens.OnError.name())),
				createItem(new NameAndColor(Tokens.Switch.name())),
				createItem(new NameAndColor(Tokens.Case.name())),
				createItem(new NameAndColor(Tokens.Default.name())),
				createItem(new NameAndColor(Tokens.ReportOn.name())),
				createItem(new NameAndColor(Tokens.ReportOff.name())),
				createItem(new NameAndColor(Tokens.Fail.name())),
				createItem(new NameAndColor(Tokens.Let.name())),
				createItem(new NameAndColor(Tokens.Assert.name())),
				createItem(new NameAndColor(Tokens.RawTable.name())),
				createItem(new NameAndColor(Tokens.RawText.name())),
				createItem(new NameAndColor(Tokens.RawMessage.name())),
				createItem(new NameAndColor(Tokens.SetHandler.name()))
		);

		Map<ActionGroups, TreeItem<NameAndColor>> map = new HashMap<>();
		Arrays.stream(ActionGroups.values()).forEach(group -> {
			NameAndColor tmp = new NameAndColor(group.name());
			TreeItem<NameAndColor> item = createItem(tmp);
			actionItem.getChildren().add(item);
			map.put(group, item);
		});
		Arrays.asList(ActionsList.actions).forEach(clazz -> {
			NameAndColor nameAndColor = new NameAndColor(clazz.getSimpleName());
			ActionGroups aClazz = clazz.getAnnotation(ActionAttribute.class).group();
			map.get(aClazz).getChildren().add(createItem(nameAndColor));
		});

		HBox bar = new HBox();
		bar.setSpacing(20);
		Button expandAll = new Button("Expand all");
		Button collapseAll = new Button("Collapse all");
		Button clearAll = new Button("Clear all");
		expandAll.setOnAction(e -> workWithTree(actionItem, item -> item.setExpanded(true)));
		collapseAll.setOnAction(e -> workWithTree(actionItem, item -> item.setExpanded(false)));
		clearAll.setOnAction(e -> workWithTree(this.treeViewColors.getRoot(), item -> {
			NameAndColor value = item.getValue();
			value.setColor(Color.TRANSPARENT);
			item.setValue(null);
			item.setValue(value);
			colorMatrixMap.clear();
		}));
		bar.getChildren().addAll(expandAll, collapseAll, clearAll);
		this.colorsPane.setTop(bar);
		this.colorsPane.setCenter(this.treeViewColors);
	}

	private void workWithTree(TreeItem<NameAndColor> root, Consumer<TreeItem<NameAndColor>> fnc)
	{
		fnc.accept(root);
		root.getChildren().forEach(child -> workWithTree(child, fnc));
	}

	private TreeItem<NameAndColor> createItem(NameAndColor name)
	{
		return new TreeItem<>(name);
	}
	//endregion

	//region private classes
	private class NameAndColor
	{
		private Color color;
		private String name;
		private boolean isChangeable = true;

		public NameAndColor(Color color, String name)
		{
			this.color = color;
			this.name = name;
		}

		public NameAndColor(String name)
		{
			this.name = name;
			this.color = Color.TRANSPARENT;
		}

		public void setChangeable(boolean changeable)
		{
			isChangeable = changeable;
		}

		public void setColor(Color color)
		{
			this.color = color;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}

	private class ClearAction extends ActionEvent
	{

	}
	//endregion
}