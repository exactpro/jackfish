package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api2.wizard.WizardAttribute;
import com.exactprosystems.jf.api2.wizard.WizardCategory;
import com.exactprosystems.jf.api2.wizard.WizardCommand;
import com.exactprosystems.jf.api2.wizard.WizardManager;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.documents.config.AppEntry;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
        name = "Connection wizard",
        pictureName = "ConnectionWizard.png",
        category = WizardCategory.OTHER,
        shortDescription = "Wizard creates a connection and saves it into the Store (View->Store).",
        experimental = false,
        strongCriteries = true,
        criteries = {ApplicationPool.class, AppEntry.class},
        detailedDescription = "{{`You can use saved connection in dictionary and matrices.`}}"
                + "{{`On the top side of the Wizard are located known button for start, connect and stop an application.`}}"
                + "{{`Under the buttons is the connection status indicator.`}}"
                + "{{`Under the indicator you can see the textfield for name of variable for the created connection.`}}"

)
public class ConnectionWizard extends AbstractWizard {

    private ApplicationConnector connector;
    private boolean isConnected;
    private Configuration configuration;
    private TextField name;
    private Label status;
    private AppEntry appEntry;

    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.setPrefWidth(345);
        borderPane.setMinWidth(345);
        borderPane.setPrefHeight(150);
        borderPane.setMinHeight(150);
        borderPane.setMaxHeight(150);

        this.status = new Label();
        this.name = new TextField();
        Tooltip nameOfVar = new Tooltip("Enter name of var here");
        this.name.tooltipProperty().set(nameOfVar);
        Button start = new Button("Start");
        start.setOnAction(e -> {
            try
            {
                connector.startApplication();
            } catch (Exception e1)
            {
                this.isConnected = false;
                this.status.setText("Failed");
                this.status.setTextFill(Color.RED);
                DialogsHelper.showError(e1.getMessage());
            }
        });
        Button connect = new Button("Connect");
        connect.setOnAction(e -> {
            try
            {
                connector.connectApplication();

            } catch (Exception e1)
            {
                this.isConnected = false;
                this.status.setText("Failed");
                this.status.setTextFill(Color.RED);
                DialogsHelper.showError(e1.getMessage());
            }
        });

        Button stop = new Button("Stop");
        stop.setOnAction(e -> Common.tryCatch(() ->
                {
                    connector.stopApplication();
                    this.isConnected = false;
                },"Error on application stop"));
        name.textProperty().addListener(event -> configuration.getStoreMap().forEach((s, o) -> {
            if (s.equals(name.getText()))
            {
                name.setStyle("-fx-text-fill: red");
                name.setTooltip(new Tooltip("Variable with name '" + name.getText() + "' already exist"));
            }
            else
            {
                name.setStyle("-fx-text-fill: green");
                name.setTooltip(nameOfVar);
            }
        }));

        start.setId("dictionaryBtnStartApplication");
        start.getStyleClass().add("transparentBackground");
        connect.setId("dictionaryBtnConnectApplication");
        connect.getStyleClass().add("transparentBackground");
        stop.getStyleClass().add("transparentBackground");
        stop.setId("dictionaryBtnStopApplication");

        ColumnConstraints colContr1 = new ColumnConstraints(50,100,150,Priority.ALWAYS, HPos.CENTER, true);
        ColumnConstraints colContr2 = new ColumnConstraints(50,100,150,Priority.ALWAYS, HPos.CENTER, true);
        ColumnConstraints colContr3 = new ColumnConstraints(50,100,150,Priority.ALWAYS, HPos.CENTER, true);

        GridPane grid = new GridPane();
        grid.add(start, 0, 0);
        grid.add(connect, 1, 0);
        grid.add(stop, 2, 0);
        grid.add(new Label("Status: "), 0, 1);
        grid.add(status, 1, 1);
        grid.add(new Label("Store as: "), 0, 2);
        grid.add(name, 1, 2,2,1);
        grid.setHgap(5);
        grid.setVgap(15);
        grid.getColumnConstraints().addAll(colContr1, colContr2, colContr3);
        borderPane.setCenter(grid);

        connector.setApplicationListener((status1, connection, throwable) -> {
            switch (status1)
            {
                case Connected:
                    this.isConnected = true;
                    this.status.setText("Success");
                    this.status.setTextFill(Color.GREEN);
                    break;
                case Connecting:
                    Platform.runLater(() -> this.status.setText("Loading..."));
                    break;
                default:
                    this.status.setText("");
                    this.status.setTextFill(Color.BLACK);
                    break;
            }
        });
    }

    @Override
    public String getTitle()
    {
        return this.appEntry.toString();
    }
    
    @Override
    protected Supplier<List<WizardCommand>> getCommands() {

        return () -> {
            CommandBuilder builder = CommandBuilder.start();
            return builder.storeGlobal(this.name.getText(), this.connector.getAppConnection(), this.configuration).build();
        };
    }

    @Override
    public boolean beforeRun() {
        return true;
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);

        this.appEntry= get(AppEntry.class, parameters);
        this.configuration = ((Context) context).getFactory().getConfiguration();
        this.connector = new ApplicationConnector(((Context) context).getFactory());
        this.connector.setIdAppEntry(appEntry.toString());
    }

    @Override
    protected void onRefused() {
        if (isConnected)
        {
            Common.tryCatch(this.connector::stopApplication, "Error on close wizard");
        }
    }
}