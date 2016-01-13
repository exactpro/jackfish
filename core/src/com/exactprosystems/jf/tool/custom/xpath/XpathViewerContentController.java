package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.parser.SearchHelper;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.custom.layout.CustomRectangle;
import com.exactprosystems.jf.tool.custom.layout.LayoutExpressionBuilderController;
import com.exactprosystems.jf.tool.custom.scale.IScaleListener;
import com.exactprosystems.jf.tool.custom.scale.ScalePane;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XpathViewerContentController implements Initializable, ContainingParent, IScaleListener
{
	public TreeView<XpathItem>							treeView;
	public Label										labelXpath1Count;
	public Label										labelXpath2Count;
	public Label										labelXpath3Count;
	public Label										labelXpath4Count;
	public Button										btnXpath1;
	public Button										btnXpath2;
	public Button										btnXpath3;
	public Button										btnXpath4;
	public Button										btnSaveXpath1;
	public Button										btnSaveXpath2;
	public Button										btnSaveXpath3;
	public Button										btnSaveXpath4;
	public CheckBox										useText;
	public HBox											hBoxCheckboxes;
	public CustomFieldWithButton						cfRelativeFrom;
	public CustomFieldWithButton						cfMainExpression;
	public SplitPane									splitPane;
	public Group										group;
	public AnchorPane									anchorTree;
	public CheckBox										cbShowImage;
	public ScrollPane									scrollPaneImage;
	public TitledPane									tpHelper;
	public GridPane										gridPaneHelper;
	public AnchorPane									anchorImage;
	public HBox											hBoxUtil;

	public ScalePane									scalePane;
	public ToggleButton									btnInspect;
	public Label										lblInspectRectangle;

	private ImageView									imageView;
	private FindPanel<TreeItem<XpathItem>>				findPanel;
	@FXML
	private Label										lblFound;

	private Parent										parent;
	private XpathViewer									model;

	private CustomRectangle								rectangle;
	private boolean										needInspect	= false;
	private CustomRectangle								inspectRectangle;

	private double										scale		= 1.0;
	private Dimension									initial		= new Dimension(0, 0);
	private Point										offset		= new Point(0, 0);
	private Map<Rectangle, Set<TreeItem<XpathItem>>>	map			= new HashMap<>();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		createTreeView();
		this.findPanel = new FindPanel<>(new IFind<TreeItem<XpathItem>>()
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
		this.splitPane.getStyleClass().add(CssVariables.SPLIT_PANE_HIDE_DIVIDER);
		listeners();
		ImageView imageView = new ImageView(new Image(CssVariables.Icons.FIND_ICON_SMALL));
		StackPane imagePane = new StackPane(imageView);
		this.anchorTree.getChildren().add(imagePane);
		imagePane.setOpacity(0.5);
		imagePane.setOnMouseEntered(event -> imagePane.setOpacity(1.0));
		imagePane.setOnMouseExited(event -> imagePane.setOpacity(0.5));
		this.anchorTree.getChildren().add(this.findPanel);
		this.findPanel.setVisible(false);
		AnchorPane.setBottomAnchor(this.findPanel, 0.0);
		AnchorPane.setLeftAnchor(this.findPanel, 25.0);

		AnchorPane.setBottomAnchor(imagePane, 5.0);
		AnchorPane.setLeftAnchor(imagePane, 0.0);
		imageView.setOnMouseClicked(event -> this.findPanel.setVisible(!this.findPanel.isVisible()));

		btnSaveXpath1.setUserData(btnXpath1);
		btnSaveXpath2.setUserData(btnXpath2);
		btnSaveXpath3.setUserData(btnXpath3);
		btnSaveXpath4.setUserData(btnXpath4);

		this.scalePane.setListener(this);

		Common.customizeLabeled(this.btnInspect, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.INSPECT_ICON);
		this.inspectRectangle = new CustomRectangle();
		this.inspectRectangle.setWidthLine(LayoutExpressionBuilderController.BORDER_WIDTH);
		this.inspectRectangle.addStyleClass(CssVariables.XPATH_INSPECT_RECTNAGLE);
		this.inspectRectangle.setVisible(false);
	}

	@Override
	public void changeScale(double scale)
	{
		this.scale = scale;
		this.imageView.setFitHeight(this.scale * this.initial.height);
		this.imageView.setFitWidth(this.scale * this.initial.width);
		hideRectangle();
		hideInspectRectangle();
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(XpathViewer model, String initial)
	{
		this.model = model;
		this.cfMainExpression.setText(initial);
	}

	public String show(String title, String themePath, boolean fullScreen)
	{
		Alert dialog = createAlert(title, themePath);
		dialog.getDialogPane().setContent(parent);
		Label headerLabel = new Label();
		headerLabel.setPrefHeight(0);
		headerLabel.setMinHeight(0);
		headerLabel.setMaxHeight(0);
		dialog.getDialogPane().setHeader(headerLabel);
		dialog.setOnShowing(event -> this.model.applyXpath(this.cfMainExpression.getText()));
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
				return this.cfMainExpression.getText();
			}
		}
		return null;
	}

	// ============================================================
	// events methods
	// ============================================================
	public void setRelativeXpath(ActionEvent actionEvent)
	{
		String text = ((Button) ((Button) actionEvent.getSource()).getUserData()).getText();
		this.cfRelativeFrom.setText(text);
		this.model.setRelativeXpath(this.cfRelativeFrom.getText());
	}

	public void copyXpath(Event event)
	{
		this.cfMainExpression.setText(((Button) event.getSource()).getText());
	}

	public void onUseText(ActionEvent actionEvent)
	{
		this.model.createXpaths(this.useText.isSelected(), getParams());
	}

	// ============================================================
	// display methods
	// ============================================================
	public void displayTree(Document document)
	{
		Platform.runLater(() ->
		{
			this.treeView.setRoot(new TreeItem<XpathItem>());
			displayTree(document, this.treeView.getRoot());
			expand(this.treeView.getRoot());
		});
	}

	public void deselectItems()
	{
		Platform.runLater(() -> deselectItems(this.treeView.getRoot()));
	}

	public void displayResults(List<Node> nodes)
	{
		Platform.runLater(() ->
		{
			if (nodes == null)
			{
				this.cfMainExpression.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
				if (!Str.IsNullOrEmpty(this.cfMainExpression.getText()))
				{
					this.cfMainExpression.getStyleClass().add(CssVariables.INCORRECT_FIELD);
				}
				this.lblFound.setText("0");
			}
			else
			{
				ArrayList<TreeItem<XpathItem>> items = new ArrayList<>();
				selectItems(this.treeView.getRoot(), nodes, items);
				if (!items.isEmpty())
				{
					TreeItem<XpathItem> xpathItem = items.get(0);
					int index = this.treeView.getTreeItemLevel(xpathItem);
					this.treeView.scrollTo(index);
				}
				this.lblFound.setText(String.valueOf(nodes.size()));
			}
		});
	}

	public void displayParams(ArrayList<String> params)
	{
		Platform.runLater(() ->
		{
			this.hBoxCheckboxes.getChildren().clear();
			this.hBoxCheckboxes.getChildren().addAll(params.stream().map(p -> {
				CheckBox box = new CheckBox(p);
				box.setSelected(true);
				box.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.createXpaths(this.useText.isSelected(), getParams()));
				return box;
			}).collect(Collectors.toList()));
			this.model.createXpaths(this.useText.isSelected(), getParams());
		});
	}

	public void displayXpaths(String xpath1, String xpath2, String xpath3, String xpath4)
	{
		Platform.runLater(() ->
		{
			this.btnXpath1.setText(xpath1);
			this.btnXpath2.setText(xpath2);
			this.btnXpath3.setText(xpath3);
			this.btnXpath4.setText(xpath4);
		});
	}

	public void displayCounters(List<Node> nodes1, List<Node> nodes2, List<Node> nodes3, List<Node> nodes4)
	{
		Platform.runLater(() ->
		{
			this.labelXpath1Count.setText(String.valueOf(nodes1 == null ? "" : nodes1.size()));
			this.labelXpath2Count.setText(String.valueOf(nodes2 == null ? "" : nodes2.size()));
			this.labelXpath3Count.setText(String.valueOf(nodes3 == null ? "" : nodes3.size()));
			this.labelXpath4Count.setText(String.valueOf(nodes4 == null ? "" : nodes4.size()));
		});
	}

	public void displayImage(BufferedImage image, int offsetX, int offsetY) throws IOException
	{
		Platform.runLater(() ->
		{
			if (image != null)
			{
				this.initial = new Dimension(image.getWidth(), image.getHeight());
				this.offset = new Point(offsetX, offsetY);
				
				this.splitPane.getStyleClass().remove(CssVariables.SPLIT_PANE_HIDE_DIVIDER);
				scrollPaneImage.setMaxHeight(Region.USE_COMPUTED_SIZE);
				scrollPaneImage.setPrefHeight(Region.USE_COMPUTED_SIZE);
				this.splitPane.setDividerPositions(0.5);
				Common.tryCatch(() -> createCanvas(image), "Error on displaying an image.");
				this.hBoxUtil.setVisible(true);
				this.cbShowImage.setSelected(true);
				
				buildMap(image.getWidth(), image.getHeight(), new Dimension(image.getWidth() / 16, image.getHeight() / 16));
			}
		});
	}
	
	public void displayRectangle(Rectangle rectangle)
	{
		if (this.rectangle != null)
		{
			this.rectangle.updateRectangle(rectangle, this.scale);
			this.rectangle.setVisible(true);
		}
	}

	public void hideRectangle()
	{
		if (this.rectangle != null)
		{
			this.rectangle.setVisible(false);
		}
	}

	// ============================================================
	// private methods
	// ============================================================
	private void displayInspectRectangle(Rectangle rectangle, String tooltipText)
	{
		this.inspectRectangle.updateRectangle(rectangle, this.scale);
		this.inspectRectangle.setVisible(true);
		this.lblInspectRectangle.setText(tooltipText);
	}

	private void hideInspectRectangle()
	{
		this.inspectRectangle.setVisible(false);
		this.lblInspectRectangle.setText("");
	}

	private void buildMap(int width, int height, Dimension cellSize)
	{
		int x = 0;
		while (x < width)
		{
			int y = 0;
			while (y < height)
			{
				Rectangle key = new Rectangle(new Point(x, y), cellSize);
				Set<TreeItem<XpathItem>> set = new HashSet<TreeItem<XpathItem>>();
				passTree(key, set, this.treeView.getRoot());
				if (set.size() > 0)
				{
					this.map.put(key, set);
				}
				
				y += cellSize.height;
			}
			x += cellSize.width;
		}
	}

	private void passTree(Rectangle keyRectangle, Set<TreeItem<XpathItem>> set, TreeItem<XpathItem> item)
	{
		XpathItem xpath = item.getValue();
		if (xpath != null)
		{
			Rectangle rec = xpath.getRectangle();
			
			if (rec != null && rec.intersects(keyRectangle))
			{
				set.add(item);
			}
		}
		
		for (TreeItem<XpathItem> child : item.getChildren())
		{
			passTree(keyRectangle, set, child);
		}
	}

	private void createCanvas(BufferedImage bufferedImage) throws IOException
	{
		this.imageView = new ImageView();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", outputStream);
		Image image = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
		this.imageView.setImage(image);
		this.group.getChildren().add(imageView);
		this.imageView.setPreserveRatio(true);
		this.rectangle = new CustomRectangle();
		this.rectangle.addStyleClass(CssVariables.XPATH_RECTANGLE);
		this.rectangle.setGroup(this.group);
		this.rectangle.setWidthLine(LayoutExpressionBuilderController.BORDER_WIDTH);
		this.rectangle.setVisible(false);
		this.inspectRectangle.setGroup(this.group);
	}

	private void deselectItems(TreeItem<XpathItem> item)
	{
		Optional.ofNullable(item.getValue()).ifPresent(v -> v.getBox().getStyleClass().removeAll(CssVariables.XPATH_FIND_TREE_ITEM));
		item.getChildren().forEach(this::deselectItems);
	}

	private void selectItems(TreeItem<XpathItem> root, List<Node> nodes, ArrayList<TreeItem<XpathItem>> items)
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
		alert.getDialogPane().setPrefHeight(1000);
		alert.getDialogPane().setPrefWidth(1000);
		return alert;
	}

	private void createTreeView()
	{
		this.treeView.setCellFactory(p -> new XpathCell());
		this.treeView.getStyleClass().add(CssVariables.XPATH_TREE_VIEW);
		TreeItem<XpathItem> rootItem = new TreeItem<>();
		this.treeView.setRoot(rootItem);
		this.treeView.setShowRoot(false);
	}

	private void displayTree(Node node, TreeItem<XpathItem> parent)
	{
		boolean isDocument = node.getNodeType() == Node.DOCUMENT_NODE;

		TreeItem<XpathItem> treeItem = isDocument ? parent : new TreeItem<>();
		IntStream.range(0, node.getChildNodes().getLength())
				.mapToObj(node.getChildNodes()::item)
				.filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
				.forEach(item -> displayTree(item, treeItem));
		if (!isDocument)
		{
			treeItem.setValue(new XpathItem(stringNode(node, XpathViewer.text(node)), node));
			parent.getChildren().add(treeItem);
		}
	}

	private HBox stringNode(Node node, String text)
	{
		HBox box = new HBox();

		box.getChildren().add(createText("<" + node.getNodeName() + " ", CssVariables.XPATH_NODE, true));
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes).ifPresent(atrs -> {
			int length = atrs.getLength();
			IntStream.range(0, length)
					.mapToObj(atrs::item)
					.forEach(item -> box.getChildren().addAll(
							createText(item.getNodeName(), CssVariables.XPATH_ATTRIBUTE_NAME, false),
							createText("=", CssVariables.XPATH_TEXT, false),
							createText("\"" + item.getNodeValue() + "\" ", CssVariables.XPATH_ATTRIBUTE_VALUE, true)
					));
		});
		if (Str.IsNullOrEmpty(text))
		{
			box.getChildren().add(createText("/>", CssVariables.XPATH_NODE, true));
		}
		else
		{
			box.getChildren().addAll(
					createText(">", CssVariables.XPATH_NODE, true),
					createText(text, CssVariables.XPATH_TEXT, true),
					createText("</" + node.getNodeName() + ">", CssVariables.XPATH_NODE, true));
		}
		return box;
	}

	private void expand(TreeItem<XpathItem> item)
	{
		item.setExpanded(true);
		item.getChildren().forEach(this::expand);
	}

	private Text createText(String text, String cssClass, boolean useContextMenu)
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
		t.getStyleClass().add(cssClass);
		return t;
	}

	private List<String> getParams()
	{
		return this.hBoxCheckboxes.getChildren().stream()
				.filter(node -> ((CheckBox) node).isSelected())
				.map(node -> (((CheckBox) node).getText()))
				.collect(Collectors.toList());
	}

	private void moveOnImage(double x, double y)
	{
		TreeItem<XpathItem> item = findOnScreen(x, y); 
		if (item != null)
		{
			displayInspectRectangle(item.getValue().getRectangle(), "");
		}
	}

	private void clickOnImage(double x, double y)
	{
		TreeItem<XpathItem> item = findOnScreen(x, y); 
		if (item != null)
		{
			this.treeView.getSelectionModel().select(item);
			
		}
		this.btnInspect.setSelected(false);
	}

	private TreeItem<XpathItem> findOnScreen(double x, double y)
	{
		int intX = (int)(x / this.scale);
		int intY = (int)(y / this.scale);
		
		int sizeX = this.initial.getSize().width / 16;
		int sizeY = this.initial.getSize().height / 16;
		Rectangle key = new Rectangle((intX / sizeX) * sizeX, (intY / sizeY) * sizeY, sizeX, sizeY);
		Set<TreeItem<XpathItem>> set = this.map.get(key);
		if (set == null)
		{
			return null;
		}
		Point mousePoint = new Point(intX, intY);

		Optional<TreeItem<XpathItem>> inspected = set.stream()
				.filter(item -> item.getValue() != null && item.getValue().getRectangle() != null)
				.filter(item -> item.getValue().getRectangle().contains(mousePoint))
				.sorted((r1, r2) -> Double.compare(square(r1.getValue().getRectangle()), square(r2.getValue().getRectangle())))
				.findFirst(); 

		return inspected.isPresent() ? inspected.get() : null;
	}
	
	private static double square(Rectangle rec)
	{
		if (rec == null)
		{
			return 0.0;
		}
		return rec.width * rec.height;
	}
	
	private void listeners()
	{
		this.group.setOnMouseMoved(event ->
		{
			if (needInspect)
			{
				moveOnImage(event.getX(), event.getY()); 
			}
		});

		this.group.setOnMouseClicked(event ->
		{
			if (needInspect)
			{
				clickOnImage(event.getX(), event.getY());
			}
		});

		this.btnInspect.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			this.btnInspect.setOpacity(newValue ? 1.0 : 0.5);
			this.needInspect = newValue;
			if (!newValue)
			{
				hideInspectRectangle();
			}
			if (newValue)
			{
//				startInspect();
			}
		});

		this.cbShowImage.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			this.splitPane.setDividerPositions(newValue ? 0.5 : 0.0);
			this.scalePane.setVisible(newValue);
			this.btnInspect.setVisible(newValue);
		});
		gridPaneHelper.visibleProperty().addListener((observable, oldValue, newValue) ->
		{
			// workaround
				if (!newValue && ((int) splitPane.getDividerPositions()[0]) == 0 && !cbShowImage.isSelected())
				{
					splitPane.setDividerPositions(0.0);
				}
			});
		this.cfRelativeFrom.setHandler(event ->
		{
			this.model.setRelativeXpath(null);
			this.cfRelativeFrom.setText("");
			this.model.createXpaths(this.useText.isSelected(), getParams());
		});

		this.cfMainExpression.textProperty().addListener((obs, oldValue, newValue) ->
		{
			this.cfMainExpression.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
			this.model.applyXpath(newValue);
		});

		this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				this.model.updateNode(newValue.getValue().getNode());
			}
			else
			{
				this.hideRectangle();
			}
		});

		Arrays.asList(this.labelXpath1Count, this.labelXpath2Count, this.labelXpath3Count, this.labelXpath4Count).forEach(
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
					else if (lbl == this.labelXpath4Count)
					{
						btnSaveXpath4.setDisable(!foundOneElement);
					}
				}));
	}
}
