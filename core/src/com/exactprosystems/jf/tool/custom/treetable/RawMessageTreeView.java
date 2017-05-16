package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.api.client.MapMessage;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.Map;

public class RawMessageTreeView extends TreeView<RawMessageTreeView.MessageBean>
{
	private MapMessage message;

	public RawMessageTreeView(MapMessage message)
	{
		this.message = message;
		TreeItem<MessageBean> root = new TreeItem<>(new MessageBean("Message", ""));
		for (Map.Entry<String, Object> entry : message.entrySet())
		{
			add(root, entry.getKey(), entry.getValue());
		}

		this.setCellFactory(p -> new TreeCell<MessageBean>()
		{
			@Override
			protected void updateItem(MessageBean bean, boolean empty)
			{
				super.updateItem(bean, empty);
				if (bean != null && !empty)
				{
					HBox box = new HBox();
					box.setAlignment(Pos.CENTER_LEFT);

					box.getChildren().add(new Label(bean.name + (bean.value.length() != 0 ? " : " : "")));

					if (bean.value != null && bean.value.length() != 0)
					{
						TextField field = new TextField(bean.value);
						field.textProperty().addListener((observable, oldValue, newValue) ->
						{
							bean.value = newValue;
							updateMessage(bean.name, newValue, message);
						});
						box.getChildren().add(field);
					}
					box.setSpacing(10);
					setGraphic(box);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		this.setRoot(root);
		this.setContextMenu(contextMenu());
		this.setShowRoot(false);
	}

	private void add(TreeItem<MessageBean> treeItem, String name, Object value)
	{
		TreeItem<MessageBean> item = new TreeItem<>();
		item.setValue(new MessageBean(name, !(value.getClass().isArray() || value instanceof Map) ? String.valueOf(value) : ""));

		if (value.getClass().isArray())
		{
			Object[] value1 = (Object[]) value;
			for (int i = 0; i < value1.length; i++)
			{
				item.setValue(new MessageBean(name + " [" + value1.length + "]", ""));
				TreeItem<MessageBean> item2 = new TreeItem<>(new MessageBean(name + " #" + i, ""));
				item.getChildren().add(item2);

				if (value1[i] instanceof Map)
				{
					for (Map.Entry<String, Object> entry : ((Map<String, Object>) value1[i]).entrySet())
					{
						add(item2, entry.getKey(), entry.getValue());
					}

				}
			}
		}
		else if (value instanceof Map)
		{
			Map<String, Object> newMap = (Map<String, Object>) value;
			for (Map.Entry<String, Object> entry : newMap.entrySet())
			{
				add(item, entry.getKey(), entry.getValue());
			}
		}

		treeItem.getChildren().add(item);
	}

	private void updateMessage(String name, String value, MapMessage message)
	{
		for (Map.Entry<String, Object> entry : message.entrySet())
		{
			String oldName = entry.getKey();
			Object oldValue = entry.getValue();

			if (oldValue.getClass().isArray())
			{
				Object[] array = (Object[]) oldValue;

				for (Object group : array)
				{
					if (group instanceof MapMessage)
					{
						if (((MapMessage) group).containsKey(name))
						{
							((MapMessage) group).put(name, value);
						}
					}
				}
			}
			else
			{
				if (oldName.equals(name))
				{
					message.put(name, value);
				}
			}
		}


	}

	private ContextMenu contextMenu() {
		ContextMenu contextMenu = new ContextMenu();

		MenuItem addNodeItem = new MenuItem("Add item");
		addNodeItem.setOnAction(e -> {
			TreeItem<MessageBean> selectedItem = this.getSelectionModel().getSelectedItem();
			add(selectedItem,"new item", " ");
		});


		MenuItem addGroup = new MenuItem("Add group");
		addGroup.setOnAction(e -> {
			TreeItem<MessageBean> selectedItem = this.getSelectionModel().getSelectedItem();
			add(selectedItem, "new group", new Map[0]);
		});
		contextMenu.getItems().addAll(addNodeItem,addGroup);

		return contextMenu;
	}


	class MessageBean
	{
		String name;
		String value;

		MessageBean(String name, String value)
		{
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString()
		{
			return "Name= '" + name + '\'' + (this.value.length() > 0 ? ", Value= '" + value + '\'' : "");
		}
	}
}
