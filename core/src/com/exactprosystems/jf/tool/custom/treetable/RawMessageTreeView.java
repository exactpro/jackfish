////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class RawMessageTreeView extends TreeView<RawMessageTreeView.MessageBean>
{
	private AbstractEvaluator  evaluator;
	private MapMessage         message;
	private IMessageDictionary dictionary;

	public RawMessageTreeView(AbstractEvaluator evaluator)
	{
		this.evaluator = evaluator;
		this.setRoot(new TreeItem<>(new MessageBean("Message", "")));
		this.getRoot().setExpanded(true);

		this.setCellFactory(p -> new TreeCell<MessageBean>()
		{
			@Override
			protected void updateItem(MessageBean bean, boolean empty)
			{
				super.updateItem(bean, empty);
				if (bean != null && !empty)
				{
					if (dictionary == null)
					{
						return;
					}
					String msgType = message.getMessageType();

					HBox box = new HBox();
					box.setAlignment(Pos.CENTER_LEFT);

					GridPane gridPane = new GridPane();
					ColumnConstraints c0 = new ColumnConstraints();
					c0.setPercentWidth(30.0);
					c0.setHalignment(HPos.LEFT);

					ColumnConstraints c1 = new ColumnConstraints();
					c1.setPercentWidth(70.0);
					c1.setHalignment(HPos.RIGHT);
					c1.setFillWidth(true);
					c1.setHgrow(Priority.ALWAYS);

					gridPane.getColumnConstraints().addAll(c0, c1);

					{
						Label labelName = new Label(bean.name);
						Control controlValue = createControl(bean.name);

						gridPane.add(labelName, 0, 0);
						gridPane.add(controlValue, 1, 0);
						setGraphic(gridPane);
					}

					if (2 < 1)
					{
						ExpressionField name = new ExpressionField(evaluator);
						ExpressionField value = new ExpressionField(evaluator);

						name.setText(bean.name);
						value.setText(bean.value);

						if (msgType != null && !msgType.equals("none"))
						{
							name.setChooserForExpressionField(msgType, () -> dictionary.getMessage(msgType).getFields().stream().map(f -> new ReadableValue(f.getName())).collect(Collectors.toList()));
							box.getChildren().add(name);

						}

						name.textProperty().addListener((observable, oldValue, newValue) ->
						{
							List<IAttribute> values = dictionary.getMessage(msgType).getField(newValue).getValues();
							if (newValue != null && !newValue.isEmpty() && values.size() != 0)
							{
								value.setChooserForExpressionField(newValue, () -> values.stream().map(v -> new ReadableValue(v.getValue(), v.getName())).collect(Collectors.toList()));
								box.getChildren().add(value);

							}
							else
							{
								if (box.getChildren().size() == 2)
								{
									box.getChildren().remove(1);
								}
							}

							updateMessage(bean.name, newValue, message);
							bean.name = newValue;

						});

						value.textProperty().addListener((observable, oldValue, newValue) ->
						{
							//                                updateMessage(bean.name, value.getText(), message);
							bean.value = newValue;
						});

						box.getChildren().add(value);
						box.setSpacing(10);
						setGraphic(box);
					}


				}
				else
				{
					setGraphic(null);
				}
			}
		});

		this.setContextMenu(contextMenu());
	}

	public void displayTree(MapMessage message, IMessageDictionary dictionary)
	{
		this.message = message;
		this.dictionary = dictionary;

		this.getRoot().getChildren().clear();
		this.getRoot().getChildren().setAll(FXCollections.emptyObservableList());
		this.getRoot().getValue().name = message.getMessageType();

		message.forEach((key, value) -> add(this.getRoot(), key, value));
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
		else
		{
			if (value instanceof Map)
			{
				Map<String, Object> newMap = (Map<String, Object>) value;
				for (Map.Entry<String, Object> entry : newMap.entrySet())
				{
					add(item, entry.getKey(), entry.getValue());
				}
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
				if (oldValue instanceof Map)
				{
					Map<String, Object> newMap = (Map<String, Object>) oldValue;

					for (Map.Entry<String, Object> entry1 : newMap.entrySet())
					{
						if (oldName.equals(entry1.getKey()))
						{
							message.put(name, value);
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
	}

	private ContextMenu contextMenu()
	{
		ContextMenu contextMenu = new ContextMenu();

		MenuItem addAllRequired = new MenuItem(R.RAW_MESSAGE_TV_ADD_ALL_REQUIRED.get());
		addAllRequired.setOnAction(e -> {
			TreeItem<MessageBean> selectedItem = this.getSelectionModel().getSelectedItem();
			IMessage message = this.dictionary.getMessage(selectedItem.getValue().name);
			System.out.println("message : " + message);
			if (message != null)
			{
				//save old filled fields

			}
			else
			{
				return;
			}
		});

		MenuItem addField = new MenuItem(R.RAW_MESSAGE_TV_ADD_FIELDS.get());
		addField.setOnAction(e -> {
			//TODO display dialog listView with checkboxes;

		});

		MenuItem addNodeItem = new MenuItem(R.RAW_MESSAGE_TV_ADD_ITEM.get());
		addNodeItem.setOnAction(e ->
		{
			TreeItem<MessageBean> selectedItem = this.getSelectionModel().getSelectedItem();
			add(selectedItem, "", "");
		});


		MenuItem addGroup = new MenuItem(R.RAW_MESSAGE_TV_ADD_GROUP.get());
		addGroup.setOnAction(e ->
		{
			TreeItem<MessageBean> selectedItem = this.getSelectionModel().getSelectedItem();
			add(selectedItem, "", new Map[0]);
		});
		contextMenu.getItems().addAll(addField, addAllRequired, addNodeItem, addGroup);

		return contextMenu;
	}

	private Control createControl(String fieldName)
	{
		IMessage message = this.dictionary.getMessage(this.message.getMessageType());
		IField field = message.getField(fieldName);
		if (field == null)
		{
			return new Label();
		}

		ExpressionField expressionField = new ExpressionField(this.evaluator);
		List<IAttribute> values = field.getValues();
		if (values != null && !values.isEmpty())
		{
			expressionField.setChooserForExpressionField(R.RAW_MESSAGE_TREE_VIEW_CHOOSE_FOR.get() + " " + fieldName, () -> values.stream()
					.map(iAttribute -> new ReadableValue(iAttribute.getValue(), iAttribute.getName()))
					.collect(Collectors.toList())
			);
		}
		return expressionField;
	}

	private void extractFields(MapMessage mapMessage, List<IField> fields, Function<String, Boolean> function)
	{
		fields.stream()
				.filter(IField::isRequired)
				.forEach(field -> {
					String name = field.getName();
					if (function.apply(name))
					{
						return;
					}
					IField reference = field.getReference();
					if (reference instanceof IMessage)
					{
						mapMessage.put(name, extractMapMessage((IMessage) reference, function));
					}
					else
					{
						mapMessage.put(name, field.getDefaultvalue() == null ? "" : field.getDefaultvalue());
					}
				});
	}

	private MapMessage extractMapMessage(IMessage reference, Function<String, Boolean> function)
	{
		MapMessage mapMessage = new MapMessage((String)null);
		extractFields(mapMessage, reference.getMessageField(), function);
		return mapMessage;
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
			return this.value.length() > 0 ? String.format(R.MESSAGE_BEAN_TO_STRING_WITH_VALUE.get(), name, value) : String.format(R.MESSAGE_BEAN_TO_STRING.get(), name);
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (o == null || getClass() != o.getClass())
			{
				return false;
			}

			MessageBean that = (MessageBean) o;

			return name != null ? name.equals(that.name) : that.name == null;
		}

		@Override
		public int hashCode()
		{
			return name != null ? name.hashCode() : 0;
		}
	}


}
