package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.client.IAttribute;
import com.exactprosystems.jf.api.client.IMessageDictionary;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;

import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RawMessageTreeView extends TreeView<RawMessageTreeView.MessageBean> {
    private MapMessage message;
    private IMessageDictionary dictionary;

    public RawMessageTreeView(MapMessage message, AbstractEvaluator evaluator) {
        this.message = message;
        TreeItem<MessageBean> root = new TreeItem<>(new MessageBean("Message", ""));
        for (Map.Entry<String, Object> entry : message.entrySet())
        {
            add(root, entry.getKey(), entry.getValue());
        }

        this.setCellFactory(p -> new TreeCell<MessageBean>() {
            @Override
            protected void updateItem(MessageBean bean, boolean empty) {
                super.updateItem(bean, empty);
                if (bean != null && !empty)
                {
                    String msgType = message.getMessageType();

                    HBox box = new HBox();
                    box.setAlignment(Pos.CENTER_LEFT);

                    ExpressionField name = new ExpressionField(evaluator);
                    ExpressionField value = new ExpressionField(evaluator);

                    name.setText(bean.name);
                    value.setText(bean.value);

                    if (msgType != null && !msgType.equals("none"))
                    {
                        name.setChooserForExpressionField(msgType, () -> dictionary.getMessage(msgType).getFields().stream().map(f -> new ReadableValue(f.getName())).collect(Collectors.toList()));
                        box.getChildren().add(name);

                    }

                    name.textProperty().addListener((observable,oldValue, newValue) -> {
                        List<IAttribute> values = dictionary.getMessage(msgType).getField(newValue).getValues();
                        if (newValue != null && !newValue.isEmpty() && values.size() != 0)
                        {
                            value.setChooserForExpressionField(newValue, () -> values.stream().map(v -> new ReadableValue(v.getValue(), v.getName())).collect(Collectors.toList()));
                            box.getChildren().add(value);

                        }else
                        {
                            if (box.getChildren().size() == 2)
                            {
                                box.getChildren().remove(1);
                            }
                        }

                        updateMessage(bean.name, newValue, message);
                        bean.name = newValue;

                    });

                    value.textProperty().addListener((observable,oldValue, newValue) -> {
//                                updateMessage(bean.name, value.getText(), message);
                        bean.value = newValue;
                    });


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
        root.setExpanded(true);
    }

    public void displayTree(MapMessage message, IMessageDictionary dictionary) {
        this.message = message;
        this.dictionary = dictionary;
        TreeItem<MessageBean> root = this.getRoot();
        root.getChildren().clear();
        for (Map.Entry<String, Object> entry : message.entrySet())
        {
            add(root, entry.getKey(), entry.getValue());
        }
    }

    private void add(TreeItem<MessageBean> treeItem, String name, Object value) {
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

    private void updateMessage(String name, String value, MapMessage message) {
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
                }else
                {
                    if (oldName.equals(name))
                    {
                        message.put(name, value);
                    }
                }

            }
        }


    }

    private ContextMenu contextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem addNodeItem = new MenuItem("Add item");
        addNodeItem.setOnAction(e ->
        {
            TreeItem<MessageBean> selectedItem = this.getSelectionModel().getSelectedItem();
            add(selectedItem, "", "");
        });


        MenuItem addGroup = new MenuItem("Add group");
        addGroup.setOnAction(e ->
        {
            TreeItem<MessageBean> selectedItem = this.getSelectionModel().getSelectedItem();
            add(selectedItem, "", new Map[0]);
        });
        contextMenu.getItems().addAll(addNodeItem, addGroup);

        return contextMenu;
    }


    class MessageBean {
        String name;
        String value;

        MessageBean(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Name= '" + name + '\'' + (this.value.length() > 0 ? ", Value= '" + value + '\'' : "");
        }
    }
}
