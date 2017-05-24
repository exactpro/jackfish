////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import java.util.Date;

import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.common.documentation.DocumentationBuilder;
import com.exactprosystems.jf.common.report.ContextHelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class WizardDialog extends Dialog<Boolean>
{
    private Wizard     wizard;
    private BorderPane borderPane;

    public WizardDialog(Wizard wizard, Context context)
    {
        super();
        this.wizard = wizard;
        this.setTitle(wizard.manager().nameOf(wizard.getClass()));
        this.setResizable(true);
        this.initOwner(Common.node);
        this.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
        Common.addIcons((Stage) this.getDialogPane().getScene().getWindow());
        this.getDialogPane().setMinHeight(300.0);
        this.getDialogPane().setMinWidth(300.0);

        createContent(context);
    }

    public BorderPane getPane()
    {
        return this.borderPane;
    }

    // region private methods
    private void createContent(Context context)
    {
        this.borderPane = new BorderPane();
        BorderPane mainPane = new BorderPane();
        this.getDialogPane().setContent(mainPane);
        mainPane.setCenter(this.borderPane);

        // region hide button bar
        Node lookup = this.getDialogPane().lookup(".button-bar");
        if (lookup instanceof ButtonBar)
        {
            ButtonBar buttonBar = (ButtonBar) lookup;
            buttonBar.setPrefHeight(0.0);
            buttonBar.setMinHeight(0.0);
            buttonBar.setMaxHeight(0.0);
            buttonBar.setVisible(false);
        }
        // endregion

        GridPane gridPane = new GridPane();
        RowConstraints r0 = new RowConstraints();
        r0.setMaxHeight(30.0);
        r0.setMinHeight(30.0);
        r0.setPrefHeight(30.0);

        gridPane.getRowConstraints().add(r0);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(30);
        c0.setMinWidth(40);
        c0.setHalignment(HPos.LEFT);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(70);
        c1.setHalignment(HPos.RIGHT);

        gridPane.getColumnConstraints().addAll(c0, c1);

        Button wizardHelp = new Button("Help");
        Common.customizeLabeled(wizardHelp, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.ACTIONS_HELP_ICON);
        wizardHelp.setOnAction(event ->
        {
            try
            {
                String name = this.wizard.manager().nameOf(this.wizard.getClass());
                ReportBuilder report = new ContextHelpFactory().createReportBuilder(null, null, new Date());
                MatrixItem help = DocumentationBuilder.createHelpForWizard(report, context, this.wizard.getClass());
                DialogsHelper.showHelpDialog(context, name, report, help);
            }
            catch (Exception e)
            {
                DialogsHelper.showError(e.getMessage());
            }
        });

        gridPane.add(wizardHelp, 0, 0);
        mainPane.setBottom(gridPane);

        Button wizardOk = new Button("Accept");
        wizardOk.setOnAction(e ->
        {
            this.setResult(true);
            this.close();
        });
        Button wizardClose = new Button("Close");
        wizardClose.setOnAction(e ->
        {
            this.setResult(false);
            this.close();
        });

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setHgrow(box, Priority.ALWAYS);
        box.getChildren().addAll(wizardClose, Common.createSpacer(Common.SpacerEnum.HorizontalMid), wizardOk);
        gridPane.add(box, 1, 0);

    }
    // endregion
}
