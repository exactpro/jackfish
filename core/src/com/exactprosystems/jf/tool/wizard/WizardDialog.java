////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.common.documentation.DocumentationBuilder;
import com.exactprosystems.jf.common.report.ContextHelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.util.Date;

public class WizardDialog extends Dialog<Boolean>
{
	private Wizard     wizard;
	private BorderPane borderPane;
    private static final Logger logger = Logger.getLogger(WizardDialog.class);

	public WizardDialog(Wizard wizard, Context context)
	{
		super();
		this.initModality(Modality.WINDOW_MODAL);
		this.wizard = wizard;
		expandTitle(null);
		this.setResizable(true);
		this.initOwner(Common.node);
		this.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		Stage stage = ((Stage) this.getDialogPane().getScene().getWindow());
		Common.addIcons(stage);
        stage.setOnCloseRequest(event ->
        {
            this.setResult(false);
            this.close();
        });
		stage.setMinHeight(200.0);
		stage.setMinWidth(200.0);

		this.getDialogPane().setMinSize(200.0, 200.0);

		createContent(context);
	}

	public void expandTitle(String str)
	{
	    String title = this.wizard.manager().nameOf(this.wizard.getClass());
	    if (!Str.IsNullOrEmpty(str))
	    {
	        title += ": " + str;
	    }
        this.setTitle(title);
	}
	
	public BorderPane getPane()
	{
		return this.borderPane;
	}
	
	// region private methods
	private void createContent(Context context)
	{
		this.borderPane = new BorderPane();

		BorderPane borderPane = new BorderPane();
		ScrollPane scrollPane = new ScrollPane(this.borderPane);
		scrollPane.setStyle("-fx-background-color:transparent;");
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		this.getDialogPane().setContent(borderPane);
		borderPane.setCenter(scrollPane);

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

		RowConstraints r1 = new RowConstraints();
		r1.setMaxHeight(30.0);
		r1.setMinHeight(30.0);
		r1.setPrefHeight(30.0);

		gridPane.getRowConstraints().addAll(r1);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setFillWidth(true);
		c0.setPercentWidth(30);
		c0.setMinWidth(40);
		c0.setHalignment(HPos.LEFT);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(70);
		c1.setHalignment(HPos.RIGHT);

		gridPane.getColumnConstraints().addAll(c0, c1);

		Button wizardHelp = new Button(R.WIZARD_HELP.get());
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
			    logger.error(e.getMessage(), e);
				DialogsHelper.showError(e.getMessage());
			}
		});

		gridPane.add(wizardHelp, 0, 1);
		borderPane.setBottom(gridPane);

		Button wizardOk = new Button(R.WIZARD_ACCEPT.get());
		wizardOk.setOnAction(e ->
		{
			this.setResult(true);
			this.close();
		});
		Button wizardRefuse = new Button(R.WIZARD_REFUSE.get());
		wizardRefuse.setOnAction(e ->
		{
			this.setResult(false);
			this.close();
		});
        this.borderPane.setOnKeyPressed(event ->
        {
            if (event.getCode() == KeyCode.ESCAPE)
            {
                wizardRefuse.fire();
            }
        });
		
		HBox box = new HBox();
		box.setAlignment(Pos.CENTER_RIGHT);
		GridPane.setHgrow(box, Priority.ALWAYS);
		box.getChildren().addAll(wizardRefuse, Common.createSpacer(Common.SpacerEnum.HorizontalMid), wizardOk);
		gridPane.add(box, 1, 1);

	}
	// endregion
}
