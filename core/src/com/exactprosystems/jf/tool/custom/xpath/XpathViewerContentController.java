package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.ImageViewWithScale;
import com.exactprosystems.jf.tool.custom.TreeTableViewWithRectangles;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class XpathViewerContentController implements Initializable, ContainingParent
{
    //region TreeView
    private TreeTableViewWithRectangles        treeTableViewWithRectangles;
    public  FindPanel<TreeItem<XpathTreeItem>> findPanel;
    //endregion

    private ImageViewWithScale imageViewWithScale;

    //region Xpath helper
    public Label                 labelXpath1Count;
    public Label                 labelXpath2Count;
    public Label                 labelXpath3Count;
    public Label                 labelXpath4Count;
    public Button                btnXpath1;
    public Button                btnXpath2;
    public Button                btnXpath3;
    public Button                btnXpath4;
    public Button                btnSaveXpath1;
    public Button                btnSaveXpath2;
    public Button                btnSaveXpath3;
    public Button                btnSaveXpath4;
    public CheckBox              useText;
    public HBox                  hBoxCheckboxes;
    public CustomFieldWithButton cfRelativeFrom;
    public TitledPane            tpHelper;
    public GridPane              gridPaneHelper;
    //endregion

    public  GridPane              gridPaneTreeView;
    public  SplitPane             splitPane;
    @FXML
    private CustomFieldWithButton cfMainExpression;
    @FXML
    private Label                 lblFound;

    private Parent      parent;
    private XpathViewer model;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        this.treeTableViewWithRectangles = new TreeTableViewWithRectangles(v->{}, v->{});
        this.imageViewWithScale = new ImageViewWithScale();
        this.imageViewWithScale.hideIds();

        this.imageViewWithScale.setClickConsumer(this.treeTableViewWithRectangles::selectItem);

        this.treeTableViewWithRectangles.hideFirstColumn();
        this.gridPaneTreeView.add(this.treeTableViewWithRectangles, 0, 0);
        this.splitPane.getItems().add(0, this.imageViewWithScale);

        this.findPanel.getStyleClass().remove(CssVariables.FIND_PANEL);
        this.findPanel.setListener(new IFind<TreeItem<XpathTreeItem>>()
        {
            @Override
            public void find(TreeItem<XpathTreeItem> xpathItemTreeItem)
            {
                treeTableViewWithRectangles.selectAndScroll(xpathItemTreeItem);
            }

            @Override
            public List<TreeItem<XpathTreeItem>> findItem(String what, boolean matchCase, boolean wholeWord)
            {
                return treeTableViewWithRectangles.findItem(what, matchCase, wholeWord);
            }
        });

        listeners();

        this.btnSaveXpath1.setUserData(btnXpath1);
        this.btnSaveXpath2.setUserData(btnXpath2);
        this.btnSaveXpath3.setUserData(btnXpath3);
        this.btnSaveXpath4.setUserData(btnXpath4);
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

    public String show(String title, List<String> themePaths, boolean fullScreen)
    {
        Alert dialog = createAlert(title, themePaths);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDefaultButton(false);

        dialog.getDialogPane().setContent(parent);
        Label headerLabel = new Label();
        headerLabel.setPrefHeight(0);
        headerLabel.setMinHeight(0);
        headerLabel.setMaxHeight(0);
        dialog.getDialogPane().setHeader(headerLabel);
        dialog.setOnShowing(event -> this.model.displayImageAndTree());
        if (fullScreen)
        {
            dialog.setOnShown(event -> ((Stage) dialog.getDialogPane().getScene().getWindow()).setFullScreen(true));
        }
        dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
        Optional<ButtonType> optional = dialog.showAndWait();
        return optional.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).map(bt -> this.cfMainExpression.getText()).orElse(null);
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
    void displayDocument(Document document, int xOffset, int yOffset)
    {
        if (document != null)
        {
            this.treeTableViewWithRectangles.displayDocument(document, xOffset, yOffset);
            BufferedImage image = this.imageViewWithScale.getImage();
            if (image != null)
            {
                this.imageViewWithScale.setListRectangles(this.treeTableViewWithRectangles.buildMap(image.getWidth(), image.getHeight(), new Dimension(image.getWidth() / 16, image.getHeight() / 16)));
            }
            String oldText = this.cfMainExpression.getText();
            this.cfMainExpression.clear();
            this.cfMainExpression.setText(oldText);
        }
    }

    void displayImage(BufferedImage image)
    {
        this.splitPane.setDividerPositions(0.0);
        if (image != null)
        {
            this.imageViewWithScale.displayImage(image);
            this.splitPane.setDividerPositions(0.5);
        }
        else
        {
            this.splitPane.getItems().remove(0);
        }
    }

    void displayDocumentFailing(String message)
    {
        Text node = new Text();
        node.setText("Exception :\n" + message);
        node.setFill(javafx.scene.paint.Color.RED);
        this.treeTableViewWithRectangles.replaceWaitingPane(node);
    }

    void displayImageFailing(String message)
    {
        Text node = new Text();
        node.setText("Exception :\n" + message);
        node.setFill(javafx.scene.paint.Color.RED);
        this.imageViewWithScale.replaceWaitingPane(node);
    }

    void deselectItems()
    {
        Platform.runLater(() -> this.treeTableViewWithRectangles.forEach(treeItem ->
                Optional.ofNullable(treeItem)
                    .map(XpathTreeItem::getBox)
                    .ifPresent(box -> box.getStyleClass().remove(CssVariables.XPATH_FIND_TREE_ITEM))
        ));
    }

    void displayResults(List<Node> nodes)
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
                List<TreeItem<XpathTreeItem>> items = this.treeTableViewWithRectangles.findByNodes(nodes);
                items.stream().filter(Objects::nonNull)
                        .map(TreeItem::getValue)
                        .filter(Objects::nonNull)
                        .map(XpathTreeItem::getBox)
                        .forEach(box -> box.getStyleClass().add(CssVariables.XPATH_FIND_TREE_ITEM));
                if (!items.isEmpty())
                {
                    this.treeTableViewWithRectangles.selectAndScroll(items.get(0));
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
            this.hBoxCheckboxes.getChildren().addAll(params.stream().map(p ->
            {
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

    public void displayRectangle(Rectangle rectangle)
    {
        this.imageViewWithScale.displayRectangle(rectangle);
    }

    // ============================================================
    // private methods
    // ============================================================

    private Alert createAlert(String title, List<String> themePaths)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Common.addIcons(((Stage) alert.getDialogPane().getScene().getWindow()));
        alert.getDialogPane().getStylesheets().addAll(themePaths);
        alert.setTitle(title);
        alert.setResizable(true);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setPrefHeight(1000);
        alert.getDialogPane().setPrefWidth(1000);
        return alert;
    }

    private List<String> getParams()
    {
        return this.hBoxCheckboxes.getChildren().stream().filter(node -> ((CheckBox) node).isSelected()).map(node -> (((CheckBox) node).getText())).collect(Collectors.toList());
    }

    private void listeners()
    {
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

        this.treeTableViewWithRectangles.addSelectionConsumer(xpathTreeItem -> {
            if (xpathTreeItem != null)
            {
                this.model.updateNode(xpathTreeItem.getNode());
            }
        });

        Arrays.asList(this.labelXpath1Count, this.labelXpath2Count, this.labelXpath3Count, this.labelXpath4Count).forEach(lbl -> lbl.textProperty().addListener((observable, oldValue, newValue) ->
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
