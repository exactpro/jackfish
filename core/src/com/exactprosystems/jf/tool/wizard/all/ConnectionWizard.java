/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.i18n.R;
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
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
        name = R.CONNECTION_WIZARD_NAME,
        pictureName = "ConnectionWizard.png",
        category = WizardCategory.OTHER,
        shortDescription = R.CONNECTION_WIZARD_SHORT_DESCRIPTION,
        experimental = false,
        strongCriteries = true,
        criteries = {ApplicationPool.class, AppEntry.class},
        detailedDescription = R.CONNECTION_WIZARD_DETAILED_DESCRIPTION

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
        Tooltip nameOfVar = new Tooltip(R.WIZARD_TOOLTIP_NAME_OF_VAR.get());
        this.name.tooltipProperty().set(nameOfVar);
        Button start = new Button(R.WIZARD_START_CONNECTION.get());
        start.setOnAction(e -> {
            try
            {
                connector.startApplication();
            } catch (Exception e1)
            {
                this.isConnected = false;
                this.status.setText(R.WIZARD_STATUS_FAILED.get());
                this.status.setTextFill(Color.RED);
                DialogsHelper.showError(e1.getMessage());
            }
        });
        Button connect = new Button(R.WIZARD_CONNECT.get());
        connect.setOnAction(e -> {
            try
            {
                connector.connectApplication();

            } catch (Exception e1)
            {
                this.isConnected = false;
                this.status.setText(R.WIZARD_STATUS_FAILED.get());
                this.status.setTextFill(Color.RED);
                DialogsHelper.showError(e1.getMessage());
            }
        });

        Button stop = new Button(R.WIZARD_STOP_APPLICATION.get());
        stop.setOnAction(e -> Common.tryCatch(() ->
                {
                    connector.stopApplication();
                    this.isConnected = false;
                },R.WIZARD_ERROR_ON_APPLICATION_STOP.get()));
        name.textProperty().addListener(event -> configuration.getStoreMap().forEach((s, o) -> {
            if (s.equals(name.getText()))
            {
                name.setStyle("-fx-text-fill: red");
                name.setTooltip(new Tooltip(MessageFormat.format(R.WIZARD_VARIABLE_WITH_NAME.get(), name.getText())));
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
        grid.add(new Label(R.WIZARD_LABEL_STATUS.get()), 0, 1);
        grid.add(status, 1, 1);
        grid.add(new Label(R.WIZARD_LABEL_STORE_AS.get()), 0, 2);
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
                    this.status.setText(R.WIZARD_STATUS_SUCCESS.get());
                    this.status.setTextFill(Color.GREEN);
                    break;
                case Connecting:
                    Common.runLater(() -> this.status.setText(R.WIZARD_STATUS_LOADING.get()));
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
    public void init(Context context, WizardManager wizardManager, Object... parameters) {
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
            Common.tryCatch(this.connector::stopApplication, R.WIZARD_ERROR_ON_CLOSE_WIZARD.get());
        }
    }
}