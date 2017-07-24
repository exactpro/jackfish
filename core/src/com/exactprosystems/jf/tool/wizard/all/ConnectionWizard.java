package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.AppConnection;
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
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@WizardAttribute(
        name = "Connection wizard",
        pictureName = "ConnectionWizard.jpg",
        category = WizardCategory.OTHER,
        shortDescription = "This wizard helps to move SubCases between NameSpaces",
        experimental = true,
        strongCriteries = true,
        criteries = {ApplicationPool.class, AppEntry.class},
        detailedDescription = "When it's need to create and store connection"
)
public class ConnectionWizard extends AbstractWizard {

    private ApplicationPool applicationPool;
    private AppEntry appEntry;
    private ApplicationConnector connector;
    private Optional<AppConnection> appConnection;
    private Configuration configuration;


    @Override
    protected void initDialog(BorderPane borderPane) {

        borderPane.setMinWidth(550);
        borderPane.setPrefWidth(600);
        borderPane.setPrefHeight(500);

        VBox vBox = new VBox();
        HBox hBox = new HBox();
        Button start = new Button("Start");
        start.setOnAction(e -> Common.tryCatch(() -> this.appConnection = connector.startApplication(), "Error on application start"));
        Button connect = new Button("Connect");
        connect.setOnAction(e -> Common.tryCatch(() -> this.appConnection = connector.connectApplication(), "Error on application connect"));
        Button stop = new Button("Stop");
        stop.setOnAction(e -> Common.tryCatch(() -> connector.stopApplication(), "Error on application stop"));
        hBox.getChildren().addAll(start, connect, stop);

        Label status = new Label();
        TextField field = new TextField();
        Button store = new Button("Store");
        store.setOnAction(e -> configuration.storeGlobal(field.getText(),appConnection.get()));

        vBox.getChildren().addAll(hBox, field, status, store);

        borderPane.setCenter(vBox);

    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        return null;
    }

    @Override
    public boolean beforeRun() {
        return true;
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);

        this.applicationPool = get(ApplicationPool.class, parameters);
        this.appEntry = get(AppEntry.class, parameters);
        this.configuration = ((Context) context).getFactory().getConfiguration();
        this.connector = new ApplicationConnector(((Context) context).getFactory());
        connector.setIdAppEntry(appEntry.toString());

    }

}
