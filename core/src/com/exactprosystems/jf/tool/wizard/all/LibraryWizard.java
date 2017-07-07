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

import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.common.VerboseLevel;
import com.exactprosystems.jf.documents.ConsoleDocumentFactory;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.Call;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.tool.custom.controls.field.autocomplete.AutoCompletionTextFieldBinding;
import com.exactprosystems.jf.tool.custom.controls.field.autocomplete.SuggestionProvider;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.related.refactor.Refactor;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@WizardAttribute(
        name                = "Library wizard", 
        pictureName         = "GherkinWizard.jpg", 
        category            = WizardCategory.MATRIX, 
        shortDescription    = "This wizard helps refactor libraries and  matrix.", 
        experimental        = true, 
        strongCriteries     = true, 
        criteries           = { MatrixFx.class, SubCase.class }, 
        detailedDescription = ""
)
public class LibraryWizard extends AbstractWizard
{
    private Matrix           currentMatrix;
    private SubCase          currentSubCase;
    private NameSpace        currentNameSpace;

    private ListView<Refactor> listView;
    private TextField nameOfSub;
    private TextField nameOfNameSpace;
    private TextField nameOfaNewFile;

//    private List<String>        commonFolder;
//    private Set<File>           allFiles = new HashSet<>();
//    private Map<String, String> knownNamespaces;
//
//    private String              oldSubId;
//    private String              newSubId;
//    private String              newNameSpaceId;
//    private String              oldNameSpaceId;
//    private String              newFileName;
//
//    private List<String>        libsFolders;
//    private List<String>        matricesFolders;
//
//    private List<Refactor>      beans;

    @Override
    public boolean beforeRun()
    {
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane)
    {

        borderPane.setPrefWidth(630);
        this.listView = new ListView<>();
        this.listView.setEditable(false);
        this.listView.setMinHeight(400);
        this.listView.setMaxHeight(600);

        Label label1 = new Label("Current SubCase ID: " + this.currentSubCase);
        Label label2 = new Label("Current NameSpace: " + this.currentNameSpace);
        Label label3 = new Label("Name of a new library file: ");

        this.nameOfSub          = new TextField(this.currentSubCase.getId());
        this.nameOfNameSpace    = new TextField(this.currentNameSpace == null ? "" : this.currentNameSpace.getId());
        this.nameOfaNewFile     = new TextField(this.currentSubCase.getMatrix().getName());
        
//        List<String> collect = getItems(NameSpace.class).keySet().stream()
//                .map(item -> item.getId() + " <" + item.getMatrix().getName() + ">").distinct()
//                .collect(Collectors.toList());
//        new AutoCompletionTextFieldBinding(nameOfNameSpace, SuggestionProvider.create(collect));
        this.nameOfaNewFile.promptTextProperty().setValue("Type a new file name where need to move current SubCase");

//        nameOfSub.textProperty().addListener((observable, oldValue, newValue) -> this.newSubId = newValue);
//        nameOfNameSpace.textProperty().addListener((observable, oldValue, newValue) -> this.newNameSpaceId = newValue);
//        nameOfaNewFile.textProperty().addListener((observable, oldValue, newValue) -> this.newFileName = newValue);

        GridPane pane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setMaxWidth(200);
        columnConstraints.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.SOMETIMES);
        pane.getColumnConstraints().addAll(columnConstraints, columnConstraints1);
        pane.setVgap(15);
        pane.setHgap(5);

        Button refresh = new Button("Get affected files");
        refresh.setOnAction(event ->
        {
            // TODO

        });

        pane.add(label1, 0, 0);
        pane.add(label2, 0, 1);
        pane.add(label3, 0, 2);
        pane.add(nameOfSub, 1, 0);
        pane.add(nameOfNameSpace, 1, 1);
        pane.add(nameOfaNewFile, 1, 2);
        pane.add(new Label("Matrices that will be affected: "), 0, 3, 2, 1);
        pane.add(listView, 0, 4, 2, 1);
        pane.add(refresh, 0, 5);
        pane.add(new Label("Total: " + listView.getItems().size()), 1, 5);

        borderPane.setCenter(pane);
        BorderPane.setAlignment(pane, Pos.CENTER);

    }

//    private List<File> getAffectedFiles(String subCaseId, String nameSpaceId)
//    {
//        List<File> result = new LinkedList<>();
//
//        allFiles.forEach(file ->
//        {
//            Matrix matrix = new Matrix(file.getPath(), new ConsoleDocumentFactory(VerboseLevel.None));
//            try
//            {
//                matrix.load(new FileReader(file));
//                matrix.getRoot().bypass(item ->
//                {
//                    if (item.getClass() == Call.class
//                            && (item.getItemName().contains(subCaseId) || item.getItemName().contains(nameSpaceId)))
//                    {
//                        result.add(file);
//                    }
//                });
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();// todo logger
//            }
//
//        });
//
//        return result;
//    }

//    private void getAllFiles(File path)
//    {
//
//        if (path.isDirectory())
//        {
//            File[] files = path.listFiles();
//            if (files != null)
//            {
//                Arrays.stream(files).forEach(this::getAllFiles);
//            }
//        }
//        else
//        {
//            if (path.getName().endsWith(".jf"))
//            {
//                this.allFiles.add(path);
//            }
//        }
//    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands()
    {
        return null;
    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters)
    {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentSubCase = super.get(SubCase.class, parameters);
        this.currentNameSpace = (NameSpace)this.currentSubCase.findParent(NameSpace.class);

        
        
//        commonFolder.forEach(s -> getAllFiles(new File(s)));
//        this.knownNamespaces = new HashMap<>();
//        getItems(NameSpace.class).forEach((item, s) -> knownNamespaces.put(item.getId(), item.getMatrix().getName()));
//        
//        this.oldSubId = currentSubCase.getId();
//        this.oldNameSpaceId = currentSubCase.getParent().getClass() == NameSpace.class
//                ? currentSubCase.getParent().getId() : "";
//        this.libsFolders = currentMatrix.getFactory().getConfiguration().getLibrariesValue().stream()
//                .map(a -> MainRunner.makeDirWithSubstitutions(a.get())).collect(Collectors.toList());
//        this.matricesFolders = currentMatrix.getFactory().getConfiguration().getMatricesValue().stream()
//                .map(a -> MainRunner.makeDirWithSubstitutions(a.get())).collect(Collectors.toList());
//        this.commonFolder = new ArrayList(libsFolders);
//        this.commonFolder.addAll(this.matricesFolders);

    }

//    private Map<MatrixItem, String> getItems(Class what)
//    {
//        Map<MatrixItem, String> res = new HashMap<>();
//        List<File> affectedObjects = getAffectedFiles(oldSubId, oldNameSpaceId);
//        affectedObjects.forEach(s ->
//        {
//
//            Matrix matrix = new Matrix(s.getName(), new ConsoleDocumentFactory(VerboseLevel.None));
//            try
//            {
//                matrix.load(new FileReader(s));
//                matrix.getRoot().bypass(item ->
//                {
//                    if (item.getClass() == what
//                            && (item.getItemName().contains(oldSubId) || item.getItemName().contains(oldNameSpaceId)))
//                    {
//                        res.put(item, s.getAbsolutePath());
//                    }
//                });
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//
//        });
//
//        return res;
//    }
}
