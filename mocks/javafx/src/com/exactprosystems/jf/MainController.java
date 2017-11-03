package com.exactprosystems.jf;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable
{
    @FXML private CheckBox checkBox;
    @FXML private RadioButton green;
    @FXML private RadioButton yellow;
    @FXML private RadioButton orange;
    @FXML private RadioButton blue;
    @FXML private ComboBox comboBox;
    @FXML private Spinner spinner;
    @FXML private SplitPane splitter;
    @FXML private TabPane tabPanel;
    @FXML private ScrollBar scrollBar;
    @FXML private ProgressBar progressBar;
    @FXML private Pane panel;
    @FXML private TextField textEdit;
    @FXML private ToggleButton toggleButton;
    @FXML private Button any;
    @FXML private Label centralLabel;
    @FXML private ImageView image;
    @FXML private TableView table;
    @FXML private Button button;

    private static final String CLICK = "_click";
    private static final String DOUBLE_CLICK = "_double_click";
    private MainModel mainModel;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        mainModel = new MainModel();
        panel.getStyleClass().add("pane");
        comboBox.getItems().addAll(mainModel.getData());
        table.getColumns().addAll(mainModel.getHeaders());
        table.setItems(mainModel.getTableData());
    }

    public void clickHandler(MouseEvent mouseEvent)
    {
        Node node = (Node) mouseEvent.getSource();
        String formatName = node.getId().substring(0,1).toUpperCase() + node.getId().substring(1);
        centralLabel.setText(formatName + (mouseEvent.getClickCount() == 1 ? CLICK : DOUBLE_CLICK));
    }

}
