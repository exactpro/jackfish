package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.parser.SearchHelper;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class XpathViewerContentController implements Initializable, ContainingParent
{
	private static final Color	nodeColor		= Color.LIGHTSEAGREEN;
	private static final Color	attrNameColor	= Color.BLUEVIOLET;
	private static final Color	attrValueColor	= Color.BLUE;
	private static final Color	textColor		= Color.BLACK;

	// content
	public TreeView<XpathItem>	treeView;
	public Label				labelXpath1Count;
	public Button				btnXpath1;
	public Label				labelXpath2Count;
	public Button				btnXpath2;
	public Label				labelXpath3Count;
	public Button				btnXpath3;
	public HBox					hBoxCheckboxes;
	public BorderPane			parentPane;
	public Button				btnSaveXpath1;
	public Button				btnSaveXpath2;
	public Button				btnSaveXpath3;
	public TextField			tfRelativeFrom;

	// header
	private TextField			textField;
	private Label				lblFound;
	private BorderPane			headerPane;

	private Parent				parent;
	private XpathViewer			model;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		createTreeView();
		this.headerPane = createHeader();
		FindPanel<TreeItem<XpathItem>> findPanel = new FindPanel<>(new IFind<TreeItem<XpathItem>>()
		{
			@Override
			public void find(TreeItem<XpathItem> xpathItemTreeItem)
			{
				treeView.getSelectionModel().clearSelection();
				treeView.getSelectionModel().select(xpathItemTreeItem);
				treeView.scrollTo(treeView.getRow(xpathItemTreeItem));
			}

			@Override
			public List<TreeItem<XpathItem>> findItem(String what, boolean matchCase, boolean wholeWord)
			{
				ArrayList<TreeItem<XpathItem>> res = new ArrayList<>();
				TreeItem<XpathItem> root = treeView.getRoot();
				addItems(res, root, what, matchCase, wholeWord);
				return res;
			}

			private void addItems(List<TreeItem<XpathItem>> list, TreeItem<XpathItem> current, String what, boolean matchCase, boolean wholeWord)
			{
				Optional.ofNullable(current.getValue()).ifPresent(value ->
				{
					if (matches(value.getText(), what, matchCase, wholeWord))
					{
						list.add(current);
					}
				});
				Optional.ofNullable(current.getChildren()).ifPresent(childer -> childer.forEach(item -> addItems(list, item, what, matchCase, wholeWord)));
			}

			private boolean matches(String text, String what, boolean matchCase, boolean wholeWord)
			{
				return Arrays.stream(what.split("\\s")).filter(s -> !SearchHelper.matches(text, s, matchCase, wholeWord)).count() == 0;
			}
		});
		this.parentPane.setBottom(findPanel);
		listeners();
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	// ============================================================
	// events methods
	// ============================================================
	public void saveXpath(ActionEvent actionEvent)
	{
		switch (((Button) actionEvent.getSource()).getId())
		{
			case "btnSave1":
				tfRelativeFrom.setText(btnXpath1.getText());
				break;
			case "btnSave2":
				tfRelativeFrom.setText(btnXpath2.getText());
				break;
			case "btnSave3":
				tfRelativeFrom.setText(btnXpath3.getText());
				break;
		}
		this.model.saveXpath(this.tfRelativeFrom.getText());
	}

	public void clearRelativeFrom(ActionEvent actionEvent)
	{
		this.model.clearSavedNode();
		tfRelativeFrom.setText("");
		this.model.createXpath(getParams());
	}

	public void copyXpath(Event event)
	{
		this.textField.setText(((Button) event.getSource()).getText());
	}

	public void init(XpathViewer model, String initial)
	{
		this.model = model;
		this.textField.setText(initial);
	}

	public String show(String title, String themePath, boolean fullScreen)
	{
		Alert dialog = createAlert(title, themePath);
		dialog.getDialogPane().setContent(parent);
		dialog.getDialogPane().setHeader(this.headerPane);
		dialog.setOnShowing(event ->
		{
			expand(treeView.getRoot());
			this.model.evaluate(this.textField.getText());
		});
		if (fullScreen)
		{
			dialog.setOnShown(event -> ((Stage) dialog.getDialogPane().getScene().getWindow()).setFullScreen(true));
		}
		dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		Optional<ButtonType> optional = dialog.showAndWait();
		if (optional.isPresent())
		{
			if (optional.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
			{
				return this.textField.getText();
			}
		}
		return null;
	}

	public void clearTree()
	{
		deselectItems(treeView.getRoot());
	}

	public void updateTextField()
	{
		textField.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
	}

	public void incorrectXpath()
	{
		if (!textField.getStyleClass().contains(CssVariables.INCORRECT_FIELD))
		{
			textField.getStyleClass().add(CssVariables.INCORRECT_FIELD);
		}
	}

	public void displayTree(Document document)
	{
		this.treeView.setRoot(new TreeItem<XpathItem>());
		displayTree(document, this.treeView.getRoot());
	}

	public void findNode(ArrayList<Node> nodes)
	{
		ArrayList<TreeItem<XpathItem>> items = new ArrayList<>();
		selectItems(this.treeView.getRoot(), nodes, items);
		if (!items.isEmpty())
		{
			TreeItem<XpathItem> xpathItem = items.get(0);
			int index = this.treeView.getTreeItemLevel(xpathItem);
			this.treeView.scrollTo(index);
		}
	}

	public void updateFoundLabel(String text)
	{
		this.lblFound.setText(text);
	}

	public void displayParams(ArrayList<String> params)
	{
		this.hBoxCheckboxes.getChildren().clear();
		params.forEach(p ->
		{
			CheckBox box = new CheckBox(p);
			box.setSelected(true);
			box.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.createXpath(getParams()));
			this.hBoxCheckboxes.getChildren().add(box);
		});
		this.model.createXpath(getParams());
	}

	public void displayXpaths(String xpath1, String xpath2, String xpath3)
	{
		Platform.runLater(() ->
		{
			this.btnXpath1.setText(xpath1);
			this.btnXpath2.setText(xpath2);
			this.btnXpath3.setText(xpath3);
		});
	}

	public void updateXpathLabel(XpathViewer.XpathType type, int newValue)
	{
		switch (type)
		{
			case Absolute:
				this.labelXpath1Count.setText(String.valueOf(newValue));
				break;
			case WithArgs:
				this.labelXpath2Count.setText(String.valueOf(newValue));
				break;
			case WithoutArgs:
				this.labelXpath3Count.setText(String.valueOf(newValue));
				break;
			default:
		}
	}

	// ============================================================
	// private methods
	// ============================================================
	private BorderPane createHeader()
	{
		BorderPane pane = new BorderPane();
		this.textField = new TextField();
		this.lblFound = new Label("Found 0");
		pane.setCenter(textField);
		pane.setRight(lblFound);
		BorderPane.setAlignment(lblFound, Pos.CENTER);
		BorderPane.setAlignment(textField, Pos.CENTER);
		this.textField.textProperty().addListener((obs, oldValue, newValue) -> this.model.evaluate(newValue));
		return pane;
	}

	private void deselectItems(TreeItem<XpathItem> root)
	{
		Optional.ofNullable(root.getValue()).ifPresent(v -> v.getBox().getStyleClass().removeAll(CssVariables.XPATH_FIND_TREE_ITEM));
		root.getChildren().forEach(this::deselectItems);
	}

	private void selectItems(TreeItem<XpathItem> root, ArrayList<Node> nodes, ArrayList<TreeItem<XpathItem>> items)
	{
		Optional.ofNullable(root.getValue()).ifPresent(v -> nodes.stream().filter(node -> v.getNode() == node).forEach(n ->
		{
			items.add(root);
			v.getBox().getStyleClass().add(CssVariables.XPATH_FIND_TREE_ITEM);
		}));
		root.getChildren().forEach(child -> selectItems(child, nodes, items));
	}

	private Alert createAlert(String title, String themePath)
	{
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().add(themePath);
		alert.setTitle(title);
		alert.setResizable(true);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.getDialogPane().setPrefHeight(600);
		alert.getDialogPane().setPrefWidth(800);
		return alert;
	}

	private void createTreeView()
	{
		this.treeView.setCellFactory(p -> new XpathCell());
		this.treeView.getStyleClass().add(CssVariables.XPATH_TREE_VIEW);
		TreeItem<XpathItem> rootItem = new TreeItem<>();
		this.treeView.setRoot(rootItem);
		treeView.setShowRoot(false);
	}

	private void displayTree(Node node, TreeItem<XpathItem> parent)
	{
		boolean isDocument = node.getNodeType() == Node.DOCUMENT_NODE;

		TreeItem<XpathItem> root = isDocument ? parent : new TreeItem<>();

		boolean hasChild = false;
		for (int i = 0; i < node.getChildNodes().getLength(); i++)
		{
			Node item = node.getChildNodes().item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE)
			{
				hasChild = true;
				displayTree(item, root);
			}
		}

		if (!isDocument)
		{
			root.setValue(new XpathItem(stringNode(node, hasChild ? null : node.getTextContent()), node));
			parent.getChildren().add(root);
		}
	}

	private HBox stringNode(Node node, String text)
	{
		HBox box = new HBox();

		box.getChildren().add(createText("<" + node.getNodeName() + " ", nodeColor, true));
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null)
		{
			int length = attributes.getLength();
			for (int i = 0; i < length; i++)
			{
				Node item = attributes.item(i);
				box.getChildren().addAll(createText(item.getNodeName(), attrNameColor, false), createText("=", textColor, false),
						createText("\"" + item.getNodeValue() + "\" ", attrValueColor, true));
			}
		}

		if (Str.IsNullOrEmpty(text))
		{
			box.getChildren().add(createText("/>", nodeColor, true));
		}
		else
		{
			box.getChildren().addAll(createText(">", nodeColor, true), createText(text, textColor, true),
					createText("</" + node.getNodeName() + ">", nodeColor, true));

		}
		return box;
	}

	private void expand(TreeItem<XpathItem> item)
	{
		item.setExpanded(true);
		item.getChildren().forEach(this::expand);
	}

	private Text createText(String text, Color color, boolean useContextMenu)
	{
		Text t = new Text(text);
		if (useContextMenu && !text.isEmpty())
		{
			t.setOnContextMenuRequested(event ->
			{

				MenuItem item = new MenuItem("Copy " + text);
				item.setOnAction(e -> Common.copyText(text));
				if (t.getParent().getParent() instanceof XpathCell)
				{
					XpathCell parent = (XpathCell) t.getParent().getParent();
					SeparatorMenuItem separator = new SeparatorMenuItem();
					ContextMenu treeMenu = parent.getContextMenu();
					treeMenu.getItems().add(0, item);
					treeMenu.getItems().add(1, separator);
					treeMenu.setOnHidden(e -> treeMenu.getItems().removeAll(item, separator));
				}
				else
				{
					ContextMenu menu = new ContextMenu();
					menu.setAutoHide(true);
					menu.getItems().add(item);
					menu.show(t, MouseInfo.getPointerInfo().getLocation().getX(), MouseInfo.getPointerInfo().getLocation().getY());
				}
			});
		}
		t.setFill(color);
		return t;
	}

	private List<String> getParams()
	{
		return this.hBoxCheckboxes.getChildren().stream().filter(node -> ((CheckBox) node).isSelected()).map(node -> (((CheckBox) node).getText()))
				.collect(Collectors.toList());
	}

	private void listeners()
	{
		this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			Optional.ofNullable(newValue).ifPresent(v -> this.model.updateNode(v.getValue().getNode(), v.getValue().getText()));
		});

		Arrays.asList(this.labelXpath1Count, this.labelXpath2Count, this.labelXpath3Count).forEach(
				lbl -> lbl.textProperty().addListener((observable, oldValue, newValue) ->
				{
					lbl.getStyleClass().remove(CssVariables.FOUND_ONE_ELEMENT);
					boolean foundOneElement = newValue.equals("1");
					if (foundOneElement & !lbl.getStyleClass().contains(CssVariables.FOUND_ONE_ELEMENT))
					{
						lbl.getStyleClass().add(CssVariables.FOUND_ONE_ELEMENT);
					}
					if (lbl == this.labelXpath1Count)
					{
						btnSaveXpath1.setDisable(!foundOneElement);
					}
					else if (lbl == this.labelXpath2Count)
					{
						btnSaveXpath2.setDisable(!foundOneElement);
					}
					else if (lbl == this.labelXpath3Count)
					{
						btnSaveXpath3.setDisable(!foundOneElement);
					}
				}));
	}
}
