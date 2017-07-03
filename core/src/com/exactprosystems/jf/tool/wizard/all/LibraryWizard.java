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
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

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
        experimental 		= true,
        strongCriteries     = true,
        criteries           = {MatrixFx.class, SubCase.class},
        detailedDescription = ""
)


public class LibraryWizard extends AbstractWizard {

    private Map<ActionRequired, Supplier<List<WizardCommand>>> suppliers = new HashMap<>();
    private List<String> commonFolder = new ArrayList<>();

    private Matrix currentMatrix;
    private Set<File> allFiles = new HashSet<>();
    private SubCase currentSubCase;

    private String oldSubId;
    private String newSubId;
    private String newNameSpaceName;
    private String oldNameSpaceName;
    private String newFileName;



    @Override
    public boolean beforeRun() {
        return true;
    }

    @Override
    protected void initDialog(BorderPane borderPane) {

        ListView<String> listView = new ListView<>();
        listView.setEditable(false);
        listView.setMinHeight(400);
        listView.setMaxHeight(600);


        Label label1 = new Label("Name of SubCase:");
        Label label2 = new Label("Name of NameSpace:");
        Label label3 = new Label("Name of a new library file:");

        label1.setTooltip(new Tooltip("Change the field to give a new name for the SubCase"));
        label2.setTooltip(new Tooltip("Select the NameSpace where SubCase will be moved"));
        label3.setTooltip(new Tooltip("Use if you need to create NameSpace in a new file"));

        TextField nameOfSub = new TextField(currentSubCase.getId());
        TextField nameOfNameSpace = new TextField(oldNameSpaceName);
        TextField nameOfaNewFile = new TextField();

        nameOfSub.textProperty().addListener((observable, oldValue, newValue) -> this.newSubId = newValue);
        nameOfNameSpace.textProperty().addListener((observable, oldValue, newValue) -> this.newNameSpaceName = newValue);
        nameOfaNewFile.textProperty().addListener((observable, oldValue, newValue) -> this.newFileName = newValue);

        GridPane pane = new GridPane();
        pane.setVgap(10);
        pane.setHgap(5);

        Button refresh = new Button("Get affected files");
        refresh.setOnAction(event -> commonFolder.forEach(s ->
        {
            ObservableList<String> objects = FXCollections.observableArrayList();
            List<File> affectedObjects = getAffectedObjects(oldSubId, oldNameSpaceName);
            objects.addAll(affectedObjects.stream().map(File::getName).collect(Collectors.toList()));
            listView.setItems(objects);

        }));

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


    }

    private List<File> getAffectedObjects(String subCase, String nameSpace) {
        List<File> result = new LinkedList<>();

        allFiles.forEach(file ->
        {
            Matrix matrix = new Matrix(file.getPath(), new ConsoleDocumentFactory(VerboseLevel.None));
            try
            {
                matrix.load(new FileReader(file));
                matrix.getRoot().bypass(item ->
                {
                    if (item.getClass() == Call.class && (item.getItemName().contains(subCase) || item.getItemName().contains(nameSpace)))
                    {
                        result.add(file);
                    }
                });
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        });

        return result;
    }

    private ActionRequired getAction() {
        if (newSubId != null && newNameSpaceName != null && newFileName != null)
        {
            return ActionRequired.SUB_NAMESPACE_FILE;
        }
        else if (newSubId != null && newNameSpaceName != null)
        {
            return ActionRequired.SUB_NAMESPACE;

        }else if(newSubId != null && newFileName != null)
        {
            return ActionRequired.SUB_FILE;
        }
        else if (newSubId != null)
        {
            return ActionRequired.ONLY_SUB;

        }else if(newNameSpaceName != null)
        {
            return ActionRequired.NAMESPACE;
        }else
        {
            return ActionRequired.NAMESPACE_FILE;
        }

    }


    private void getAllFiles(File path) {

        if (path.isDirectory())
        {
            File[] files = path.listFiles();
            if (files != null)
            {
                Arrays.stream(files).forEach(this::getAllFiles);
            }
        }
        else
        {
            if (path.getName().endsWith(".jf"))
            {
                this.allFiles.add(path);
            }
        }
    }

    @Override
    protected Supplier<List<WizardCommand>> getCommands() {
        return suppliers.get(getAction());
    }

//    private List<MatrixItem> makeRefactor() {
//        List<MatrixItem> res = new LinkedList<>();
//
//        switch (getAction())
//        {
//            case ONLY_SUB:
//                break;
//            case NAMESPACE_FILE:
//                break;
//            case NAMESPACE:
//                break;
//            case SUB_FILE:
//                break;
//            case SUB_NAMESPACE:
//                break;
//            case SUB_NAMESPACE_FILE:
//                break;
//        }
//
//        return res;
//    }

    @Override
    public void init(IContext context, WizardManager wizardManager, Object... parameters) {
        super.init(context, wizardManager, parameters);

        this.currentMatrix = super.get(MatrixFx.class, parameters);
        this.currentSubCase = super.get(SubCase.class, parameters);
        this.oldSubId = currentSubCase.getId();
        this.oldNameSpaceName = currentSubCase.getParent().getClass() == NameSpace.class ? currentSubCase.getParent().getId() : "";
        this.commonFolder.addAll(currentMatrix.getFactory().getConfiguration().getMatricesValue().stream().
                map(a -> MainRunner.makeDirWithSubstitutions(a.get())).collect(Collectors.toList()));
        this.commonFolder.addAll(currentMatrix.getFactory().getConfiguration().getLibrariesValue().stream().
                map(a -> MainRunner.makeDirWithSubstitutions(a.get())).collect(Collectors.toList()));
        this.commonFolder.forEach(s -> getAllFiles(new File(s)));

    }

    enum ActionRequired{
        ONLY_SUB,
        SUB_NAMESPACE,
        SUB_NAMESPACE_FILE,
        SUB_FILE,
        NAMESPACE,
        NAMESPACE_FILE
    }

    private Map<MatrixItem, String> getItemsFromMatrices() {
        Map<MatrixItem, String> res = new HashMap<>();
        List<File> affectedObjects = getAffectedObjects(oldSubId, oldNameSpaceName);
        affectedObjects.forEach(s -> {

            Matrix matrix = new Matrix(s.getName(), new ConsoleDocumentFactory(VerboseLevel.None));
            try
            {
                matrix.load(new FileReader(s));
                matrix.getRoot().bypass(item ->
                {
                    if (item.getClass() == Call.class && (item.getItemName().contains(oldSubId) || item.getItemName().contains(oldNameSpaceName)))
                    {
                        res.put(item, s.getAbsolutePath());
                    }
                });
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        });

        return res;
    }

    {
        suppliers.put(ActionRequired.ONLY_SUB, () ->{
            CommandBuilder builder = CommandBuilder.start();

            if (!currentMatrix.isLibrary())
            {
                currentSubCase.setId(newSubId);
                currentMatrix.getRoot().bypass(item -> {
                    if (item.getClass() == Call.class && ((Call) item).getName().equals(oldSubId))
                    {
                        MatrixItem newItem = CommandBuilder.create(currentMatrix, Call.class.getSimpleName(), null);
                        ((Call) newItem).setName(newSubId);
                        builder.removeMatrixItem(currentMatrix, item).addMatrixItem(currentMatrix, item.getParent(), item, item.getParent().index(item));
                    }
                });
                builder.removeMatrixItem(currentMatrix, currentSubCase).addMatrixItem(currentMatrix, currentSubCase.getParent(),currentSubCase, currentSubCase.getParent().index(currentSubCase));
                return builder.build();
            }else
            {

                getItemsFromMatrices().forEach((item, s) ->
                {
                    Matrix matrix = item.getMatrix();
                    MatrixItem newItem = CommandBuilder.create(matrix, Call.class.getSimpleName(), null);
                    newItem.setId(item.getId());
                    ((Call) newItem).setName(((Call) item).getName().replace(oldSubId, newSubId));
                    builder.removeMatrixItem(matrix, item).addMatrixItem(matrix, item.getParent(), newItem, item.getParent().index(item));
                    try
                    {
                        matrix.save(s);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });
                currentSubCase.setId(newSubId);
                builder.removeMatrixItem(currentMatrix, currentSubCase).addMatrixItem(currentMatrix, currentSubCase.getParent(), currentSubCase, currentSubCase.getParent().index(currentSubCase));
                try
                {
                    currentMatrix.save(currentMatrix.getName());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                return builder.build();
            }
        });
        suppliers.put(ActionRequired.SUB_NAMESPACE, () ->{
            CommandBuilder builder = CommandBuilder.start();

            getItemsFromMatrices().forEach((item, s) -> {
                Matrix matrix = item.getMatrix();
                MatrixItem newItem = CommandBuilder.create(matrix, Call.class.getSimpleName(), null);
                newItem.setId(item.getId());
                ((Call)newItem).setName(newNameSpaceName +"."+ newSubId);
                builder.removeMatrixItem(matrix, item).addMatrixItem(matrix, item.getParent(), newItem, item.getParent().index(item));
                try
                {
                    matrix.save(s);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
            currentSubCase.setId(newSubId);
            builder.removeMatrixItem(currentMatrix, currentSubCase).addMatrixItem(currentMatrix,currentSubCase.getParent(),currentSubCase,currentSubCase.getParent().index(currentSubCase));
            try
            {
                currentMatrix.save(currentMatrix.getName());
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return builder.build();
        });
    }
}
