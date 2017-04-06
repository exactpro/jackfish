package com.exactprosystems.jf.common;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.actions.system.Vars;
import com.exactprosystems.jf.documents.ConsoleDocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.Let;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MatrixConverter {

    private Map<String, Predicate<MatrixItem>> predicates = new HashMap<>();
    private List<MutableString> matricesFolders;
    private List<MutableString> libsFolders;
    private String toFolder;


    {
        predicates.put("Vars", item -> item.getClass() == ActionItem.class && ((ActionItem) item).getActionClass() == Vars.class);
    }

    public MatrixConverter(List<MutableString> matricesFolders, List<MutableString> libsFolders, String toFolder) {

        this.matricesFolders = matricesFolders;
        this.libsFolders = libsFolders;
        this.toFolder = toFolder.endsWith(File.separator) ? toFolder : (toFolder + File.separator);
    }

    public void start() {

        List<MutableString> commonFolder = new ArrayList<>(matricesFolders);
        commonFolder.addAll(libsFolders);

        commonFolder.forEach(folder -> convert(new File(folder.get())));
    }

    private void convert(File path) {

        createFolders(toFolder, path);

        if (path != null && path.exists())
        {
            if (path.isDirectory())
            {
                for (File f : path.listFiles())
                {
                    convert(f);
                }
            }
            else
            {
                if (path.getName().endsWith(".jf"))
                {
                    try
                    {
                        Matrix matrix = new Matrix(path.getPath(), new ConsoleDocumentFactory(VerboseLevel.None));
                        matrix.load(new FileReader(path));
                        CsvWriter writer = prepareCsvWriter(toFolder + path.getParent() + File.separator + path.getName());
                        List<MatrixItem> items = findItems(matrix, predicates.get("Vars"));
                        varsToLet(items, matrix);
                        matrix.getRoot().write(-1, writer);
                        writer.flush();
                        writer.close();

                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private CsvWriter prepareCsvWriter(String toFolder) throws IOException {
        CsvWriter writer = new CsvWriter(new FileWriter(toFolder), Configuration.matrixDelimiter);
        writer.setDelimiter(Configuration.matrixDelimiter);
        writer.setForceQualifier(false);
        writer.setUseTextQualifier(false);
        writer.setTextQualifier(Parser.prefferedQuotes);
        return writer;
    }

    private List<MatrixItem> findItems(Matrix matrix, Predicate<MatrixItem> predicate) {

        List<MatrixItem> result = new ArrayList<>();
        matrix.getRoot().bypass(item ->
                {
                    if (predicate.test(item))
                    {
                        result.add(item);
                    }
                }
        );

        return result;
    }

    private void varsToLet(List<MatrixItem> items, Matrix matrix) {
        items.forEach(item ->
                {
                    for (Parameter parameter : item.getParameters())
                    {
                        Let let = new Let();
                        Map<Tokens, String> map = new HashMap<>();
                        List<String> collect = item.getComments().stream().map(CommentString::toString).collect(Collectors.toList());
                        map.put(Tokens.Let, parameter.getExpression());
                        map.put(Tokens.Id, parameter.getName());
                        map.put(Tokens.Off, item.isOff() ? "1" : "0");
                        map.put(Tokens.RepOff, item.isRepOff() ? "1" : "0");
                        map.put(Tokens.Global, item.isGlobal() ? "1" : "0");
                        try
                        {
                            let.init(matrix, collect, map, null);
                        } catch (MatrixException e)
                        {
                            e.printStackTrace();
                        }
                        int varsIndex = item.getParent().index(item);
                        item.getParent().insert(varsIndex, let);
                    }

                    matrix.remove(item);
                }
        );
    }

    private void createFolders(String toFolder, File path) {

        if (path.getParent() == null)
        {
            return;
        }

        Predicate<File> isFolderExist = f -> f.exists() && f.isDirectory();
        File folder = new File(toFolder + path.getParent());

        if(!isFolderExist.test(folder)){
            folder.mkdirs();
        }
    }
}


