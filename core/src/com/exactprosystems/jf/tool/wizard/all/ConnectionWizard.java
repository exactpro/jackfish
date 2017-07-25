package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
        name = "Connection wizard",
        pictureName = "ConnectionWizard.jpg",
        category = WizardCategory.OTHER,
        shortDescription = "This wizard helps to move SubCases between NameSpaces",
        experimental = false,
        strongCriteries = true,
        criteries = {ApplicationPool.class, AppEntry.class},
        detailedDescription = "When it's need to create and storeGlobal connection"
)
public class ConnectionWizard extends AbstractWizard {

    private ApplicationConnector connector;
    private boolean isConnected;
    private Configuration configuration;
    private TextField name;
    private Label status;

    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.setPrefWidth(400);
        borderPane.setMinWidth(400);

        this.status = new Label();
        this.name = new TextField();
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

        Label nameCheck = new Label();
        name.textProperty().addListener(event -> configuration.getStoreMap().forEach((s, o) -> {
            if (s.equals(name.getText()))
            {
                nameCheck.setTextFill(Color.RED);
                nameCheck.setText("Already exist");
            }
            else
            {
                nameCheck.setText("Ok");
                nameCheck.setTextFill(Color.GREEN);
            }
        }));

        GridPane grid = new GridPane();
        grid.add(start, 0, 0);
        grid.add(connect, 1, 0);
        grid.add(stop, 2, 0);
        grid.add(new Label("Connection name: "), 0, 1);
        grid.add(name, 1, 1);
        grid.add(nameCheck, 2, 1);
        grid.add(new Label("Status: "), 0, 2);
        grid.add(status, 1, 2);
        grid.setHgap(5);
        grid.setVgap(5);

        borderPane.setCenter(grid);
        BorderPane.setMargin(grid, new Insets(90, 0, 0, 5));

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

        AppEntry appEntry = get(AppEntry.class, parameters);
        this.configuration = ((Context) context).getFactory().getConfiguration();
        this.connector = new ApplicationConnector(((Context) context).getFactory());
        this.connector.setIdAppEntry(appEntry.toString());
    }

    @Override
    protected void onRefused() {
        if (isConnected)
        {
            this.connector.notify();
            Common.tryCatch(this.connector::stopApplication, "Error on close wizard");
        }
    }
}