////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@WizardAttribute(
        name            = "Gherkin wizard", 
        pictureName         = "GherkinWizard.jpg", 
        category            = WizardCategory.MATRIX, 
        shortDescription    = "This wizard create matrix structure from Gherkin code", 
        detailedDescription = "This wizard create matrix structure from Gherkin code", 
        strongCriteries     = true, 
        criteries           = { TestCase.class, MatrixFx.class }
        )
public class GherkinWizard extends AbstractWizard
{
    private MatrixFx       currentMatrix  = null;
    private MatrixItem     currentItem    = null;
    private MatrixItem     parentItem     = null;
    private int            index          = 0;

    private StringProperty stringProperty = new SimpleStringProperty("");

    public GherkinWizard()
    {
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentItem = super.get(MatrixItem.class, parameters);
        this.parentItem = this.currentItem.getParent();
        this.index = this.parentItem.index(this.currentItem);
    }

    @Override
    public boolean beforeRun()
    {
        return true;
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return () ->
        {
            CommandBuilder builder = CommandBuilder.start();
            Exception[] exceptions = new Exception[1];
            Consumer<Exception> onError = e -> exceptions[0] = e;
            createStructure(onError)
                    .forEach(item -> builder.addMatrixItem(this.currentMatrix, this.parentItem, item, this.index++));
            if (exceptions[0] != null)
            {
                DialogsHelper.showError(exceptions[0].getMessage());
                return new ArrayList<>();
            }
            return builder.build();
        };
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {
        TreeView<String> treeView = new TreeView<>();
        treeView.setRoot(new TreeItem<>());
        treeView.setShowRoot(false);

        borderPane.setPrefSize(800.0, 800.0);
        borderPane.setMinSize(800.0, 800.0);

        StyleClassedTextArea textArea = new StyleClassedTextArea();
        textArea.setStyleSpans(0, Common.convertFromList(Highlighter.Gherkin.getStyles(textArea.getText())));
        textArea.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change -> textArea
                .setStyleSpans(0, Common.convertFromList(Highlighter.Gherkin.getStyles(textArea.getText()))));

        textArea.textProperty().addListener((observable, oldValue, newValue) -> this.stringProperty.set(newValue));
        VBox vBox = new VBox();
        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.getChildren().addAll(new Label("Enter or paste Gherkin code below :"),
                Common.createSpacer(Common.SpacerEnum.VerticalMax), Common.createSpacer(Common.SpacerEnum.VerticalMin));
        vBox.getChildren().add(textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        borderPane.setCenter(vBox);

        HBox hBox = new HBox();

        VBox box = new VBox();
        box.setPrefWidth(350.0);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinWidth(350.0);

        box.setAlignment(Pos.TOP_LEFT);
        Button preview = new Button("Preview");
        preview.setOnAction(e -> showPreview(treeView.getRoot()));
        box.getChildren().addAll(preview, Common.createSpacer(Common.SpacerEnum.VerticalMid));
        box.getChildren().add(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        hBox.getChildren().addAll(Common.createSpacer(Common.SpacerEnum.HorizontalMid), box,
                Common.createSpacer(Common.SpacerEnum.HorizontalMid));
        borderPane.setRight(hBox);
    }

    private List<MatrixItem> createStructure(Consumer<Exception> onError)
    {
        List<MatrixItem> list = new ArrayList<>();
        parse((child, comment) ->
        {
            MatrixItem testCase = CommandBuilder.create(this.currentMatrix, Tokens.TestCase.get(), null);
            ArrayList<String> comments = comment == null ? null
                    : Arrays.stream(comment.split("\n")).collect(Collectors.toCollection(ArrayList::new));
            HashMap<Tokens, String> systemParameters = new HashMap<>();
            systemParameters.put(Tokens.TestCase, "TestCase (" + child.getKeyword().trim() + ") " + child.getName());
            Common.tryCatch(() -> testCase.init(this.currentMatrix, comments, systemParameters, null), "Error");
            list.add(testCase);
            return testCase;
        }, (step, scenario) ->
        {
            MatrixItem stepItem = CommandBuilder.create(this.currentMatrix, Tokens.Step.get(), null);
            HashMap<Tokens, String> systemMap = new HashMap<>();
            systemMap.put(Tokens.Step, "'Step [" + step.getKeyword() + "] " + step.getText() + "'");
            Common.tryCatch(() -> stepItem.init(this.currentMatrix, null, systemMap, null), "Error");
            scenario.insert(scenario.count(), stepItem);
        }, onError);
        return list;
    }

    private void showPreview(TreeItem<String> root)
    {
        root.getChildren().clear();
        parse((child, comment) ->
        {
            String name = "";
            if (comment != null)
            {
                name = comment;
            }
            name += "TestCase (" + child.getKeyword().trim() + ") " + child.getName();
            TreeItem<String> scenario = new TreeItem<>(name);
            scenario.setExpanded(true);
            root.getChildren().add(scenario);
            return scenario;
        }, (step, scenario) -> scenario.getChildren()
                .add(new TreeItem<>("Step [" + step.getKeyword() + "] " + step.getText())),
                e -> root.getChildren().setAll(new TreeItem<>("Some error with parsing text")));
    }

    private <T> void parse(BiFunction<ScenarioDefinition, String, T> testCaseConsumer, BiConsumer<Step, T> stepConsumer,
            Consumer<Exception> onError)
    {
        try
        {
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            GherkinDocument parse = parser.parse(this.stringProperty.get());
            Feature feature = parse.getFeature();
            AtomicBoolean isFirst = new AtomicBoolean(true);
            feature.getChildren().forEach(child ->
            {
                String comment = null;
                if (isFirst.getAndSet(false))
                {
                    comment = feature.getName() + "\n" + feature.getDescription() + "\n";
                }
                T t = testCaseConsumer.apply(child, comment);
                List<Step> steps = child.getSteps();
                steps.forEach(step -> stepConsumer.accept(step, t));

            });
        }
        catch (Exception e)
        {
            onError.accept(e);
        }
    }

}
