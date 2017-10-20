package com.exactprosystems.jf.common.documentation;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.OperationKind;
import com.exactprosystems.jf.api.common.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.ControlsAttributes;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.Marker;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.*;
import com.exactprosystems.jf.functions.Content;
import com.exactprosystems.jf.functions.ContentItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.settings.Theme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DocumentationBuilder
{
    private static final String mvel = "mvel.txt";

    public static MatrixItem createHelp (ReportBuilder report, Context context) throws Exception
    {
        Content content = new Content();
        AbstractEvaluator evaluator = context.getEvaluator();
        List<OperationKind> operations = Arrays.stream(OperationKind.values()).collect(Collectors.toList());

        MatrixItem help = new HelpTextLine("");
        content.add(new ContentItem(addPartOfContent("MVEL", true)));
        addPartOfContent(DocumentationBuilder.class.getResourceAsStream(mvel), content);
        addText(help, DocumentationBuilder.class.getResourceAsStream(mvel));
        content.add(new ContentItem(addEndParentPartOfContent()));

        addAllControlsTable(help, "All controls", context, operations, true, true, content);
        addAllItems(help, content);
        addAllActions(help, content);
        addPartOfContent(DocumentationBuilder.class.getResourceAsStream("specialSymbols.txt"), content);
        addText(help, DocumentationBuilder.class.getResourceAsStream("specialSymbols.txt"));
        addSpecialSymbols(help, evaluator);
        addContent(help, "", content);
        return help;
    }
    
    public static MatrixItem createUserManual (ReportBuilder report, Context context) throws Exception
    {
        String newPage = "{{&&}}";
        AbstractEvaluator evaluator = context.getEvaluator();
        Content content = new Content();
        MatrixItem help = new HelpTextLine("{{``}}");

        String[][] table1 = new String[][]
                {
                        { "Version",  VersionInfo.getVersion() },
                        { "Release date", DateTime.current().str("dd MMM yyyy")}
                };

        String[][] table2 = new String[][]
                {
                    { "{{*Date*}}", "{{*Version*}}", "{{*By*}}", "{{*Comments*}}" },
                    { DateTime.current().str("dd MMM yyyy"), VersionInfo.getVersion(), "Valery Florov", "Initial Draft" }
                };
                
        String[][] table3 = new String[][]
                {
                    { "{{*Abbreviation*}}", "{{*Meaning*}}" },
                    { "JF", "JackFish" }
                };

        List<OperationKind> operations = Arrays.stream(OperationKind.values()).collect(Collectors.toList());
        int size = operations.size();

        addTextLine(help, "\\JackFishHuge");
        addTextLine(help, "{{``}}");
        addPicture(help, "", 100, DocumentationBuilder.class.getResourceAsStream("BurntOrangeLine.png"));
        addTable(help, "\\UserGuide",              false,  table1, new int[] { 50, 50 },  evaluator);
        for (int i = 0; i < 5; i++){
            addTextLine(help, "{{` `}}");
        }

        addPicture(help, "", 100, DocumentationBuilder.class.getResourceAsStream("BurntOrangeLine.png"));
        addTable(help, "\\DocInfo",    false,  table2, new int[] { 25, 23, 23, 25 },  evaluator);
        addTable(help, "\\Abbreviations",           false,  table3, new int[] { 50, 50 },  evaluator);
        addTextLine(help, newPage);
        
        addContent(help, "{{*Table of contenst*}}", new Content());
        addTextLine(help, newPage);

        addText(help, DocumentationBuilder.class.getResourceAsStream("intro1.txt"));
        addPicture(help, "Architecture", 80, DocumentationBuilder.class.getResourceAsStream("Intro.png"));
        addText(help, DocumentationBuilder.class.getResourceAsStream("intro2.txt"));
        addTextLine(help, newPage);
        
        addTextLine(help, "{{1MVEL1}}");
        addText(help, DocumentationBuilder.class.getResourceAsStream(mvel));
        addTextLine(help, newPage);

        addTextLine(help, "{{1All controls1}}");
        addText(help, DocumentationBuilder.class.getResourceAsStream("controls.txt"));
        addTextLine(help, newPage);
        addAllControlsTable(help, "All controls", context, operations.subList(0, size/3), true, true, content);
        addTextLine(help, newPage);
        addAllControlsTable(help, "All controls - continue", context, operations.subList(size/3, size*2/3), true, true, content);
        addTextLine(help, newPage);
        addAllControlsTable(help, "All controls - end", context, operations.subList(size*2/3, size), true, true, content);
        addTextLine(help, newPage);

        addTextLine(help, "{{1Matrix syntax1}}");
        addAllItems(help, content);
        addTextLine(help, newPage);

        addAllActions(help, content);
        addTextLine(help, newPage);

        return help;
    }
        
    public static MatrixItem createHelpForItem (ReportBuilder report, Context context, MatrixItem item) throws Exception
    {
        MatrixItem help = null;
        
        if (item instanceof ActionItem)
        {
            help = new HelpActionItem( ((ActionItem)item).getActionClass() );
        }
        else
        {
            help = new HelpItem(item.getClass());
        }
        
        return help;
    }
    
    public static MatrixItem createHelpForWizard(ReportBuilder report, Context context, Class<? extends Wizard> clazz)
    {
        WizardManager manager = context.getFactory().getWizardManager();
        return new HelpWizardItem(manager, clazz);
    }

    public static MatrixItem createHelpForPlugin(ReportBuilder report, Context context, String title, IApplicationFactory applicationFactory) throws Exception
    {
        AbstractEvaluator evaluator = context.getEvaluator();
        MatrixItem help = new HelpTextLine("{{`{{2" + title.toUpperCase() + "2}}`}}");
        Class<?> clazz = applicationFactory.getClass();
        PluginDescription pd = clazz.getAnnotation(PluginDescription.class);
        if (pd != null)
        {
            //description
            addTextLine(help, pd.description().get());
            //fields -> table
            List<PluginFieldDescription> allFields = Arrays.stream(clazz.getDeclaredFields()).map(f-> f.getAnnotation(PluginFieldDescription.class)).filter(f-> f != null).collect(Collectors.toList());
            for(ParametersKind pk : ParametersKind.values())
            {
                String[] knownParameters = applicationFactory.wellKnownParameters(pk);
                if (knownParameters.length > 0)
                {
                    List<PluginFieldDescription> pfdList = new ArrayList<>();
                    for (String field : knownParameters)
                    {
                        List<PluginFieldDescription> filteredResult = allFields.stream().filter(f -> f.parameter().equals(field)).collect(Collectors.toList());
                        if (filteredResult.size() == 1){
                            pfdList.addAll(filteredResult);
                        }
                        else
                        {
                            filteredResult.forEach(f ->
                                {
                                    for(ParametersKind pKind : f.parametersKind())
                                    {
                                        if(pk == pKind)
                                        {
                                            pfdList.add(f);
                                            break;
                                        }
                                    }
                                }
                            );
                        }
                    }
                    addPluginDescriptionTable(help, evaluator, pfdList, pk);
                }
            }
            //controls
            addTextLine(help, "{{`{{*" + R.SUPPORTED_CONTROLS.get() + "*}}`}}");
            Set<ControlKind> controls = applicationFactory.supportedControlKinds();
            String s = controls.stream().map(c -> "{{@" + c.getClazz() + "@}}").collect(Collectors.joining(", "));
            addTextLine(help, "{{`" + s + "`}}");
            //additional info
            addTextLine(help, "{{`{{*" + R.DIFFERENCES.get() + ":*}}`}}");
            addTextLine(help, pd.difference().get());
        }
        return help;
    }

    private static void addContent(MatrixItem root, String title, Content content) throws Exception
    {
        MatrixItem contentItem = new HelpContent(title, content); 
        root.insert(root.count(), contentItem);
    }

    private static void addPicture(MatrixItem root, String title, int width, InputStream stream) throws Exception
    {
        MatrixItem picture = new HelpPicture(title, stream, width); 
        root.insert(root.count(), picture);
    }

    private static void addTable(MatrixItem root, String title, boolean bordered, String[][] content, int[] widths, AbstractEvaluator evaluator) throws Exception
    {
        Table table = new Table(content, evaluator);
        MatrixItem text = new HelpTable(title, table, bordered, widths);
        root.insert(root.count(), text);
    }

    private static void addSpecialSymbols(MatrixItem root, AbstractEvaluator evaluator) throws Exception
    {
        String[] headers = new String[] {"Symbol", "Description", "Example", "Example result"};
        int[] width = new int[] {15, 35, 25, 25};
        Table table = new Table(headers, evaluator);
        for (String[] element : new Marker.HTMLMaker(Theme.currentTheme().equals(Theme.WHITE)).keysDescriptions())
        {
            table.addValue(element);
        }
        MatrixItem tableItem = new HelpTable("", table, true, width);
        root.insert(root.count(), tableItem);
    }

    private static void addPluginDescriptionTable(MatrixItem root, AbstractEvaluator evaluator, List<PluginFieldDescription> fields, ParametersKind pk)
    {
        if(!fields.isEmpty())
        {
            String title;
            switch(pk)
            {
                case CONNECT: title = R.PARAMETERS_KIND_CONNECT.get(); break;
                case GET_PROPERTY: title = R.PARAMETERS_KIND_GET_PROPERTY.get(); break;
                case NEW_INSTANCE: title = R.PARAMETERS_KIND_NEW_INSTANCE.get(); break;
                case SET_PROPERTY: title = R.PARAMETERS_KIND_SET_PROPERTY.get(); break;
                case START: title = R.PARAMETERS_KIND_START.get(); break;
                case ENCODE: title = R.PARAMETERS_KIND_ENCODE.get(); break;
                case LOAD: title = R.PARAMETERS_KIND_LOAD.get(); break;
                case PROPS: title = R.PARAMETERS_KIND_PROPS.get(); break;
                case STOP: title = R.PARAMETERS_KIND_STOP.get(); break;
                default: title =""; break;
            }
            String[] headers = new String[] {R.PARAMETER.get(), R.DESCRIPTION.get(), R.EXAMPLE.get()};
            int[] width = new int[] {20, 50, 30};
            Table table = new Table(headers, evaluator);
            fields.forEach(f-> table.addValue(new String[] {f.parameter(), f.description().get(), f.example()}));
            MatrixItem tableItem = new HelpTable("{{`{{/" + title + "/}}`}}", table, true, width);
            root.insert(root.count(), tableItem);
        }
    }

    private static void addAllControlsTable(MatrixItem root, String title, Context context, List<OperationKind> operations, boolean rotate, boolean bordered, Content content) throws Exception
    {
        try
        {
            //todo
            content.add(new ContentItem(
                    String.format("<li role='presentation'>\n<a href='#%s'>%s</a>\n</li>\n", title.replaceAll("\\s+","").toLowerCase(), title)
            ));

            List<ControlKind> fullList = Arrays.stream(ControlKind.values())
                    .sorted((a,b) -> a.name().compareTo(b.name()))
                    .collect(Collectors.toList());
            
            for (ControlKind controlKind : fullList)
            {
                Class<?> controlClass = Class.forName(AbstractControl.class.getPackage().getName() + "." + controlKind.getClazz());
                controlClass.getSimpleName();
            }
        
            List<String> headers = new ArrayList<>();
            headers.add("#");
            
            for (OperationKind kind : operations)
            {
                if (rotate)
                {
                    headers.add("{{^" + kind.toString() + "^}}");
                }
                else
                {
                    headers.add(kind.toString());
                }
            }

            Table table = new Table(headers.toArray(new String[] {}), context.getEvaluator());
            for (int i = 1; i < table.getHeaderSize(); i++)
            {
                table.considerAsColored(table.getHeader(i));
            }

            for (ControlKind k : fullList)
            {
                String[] arr = new String[operations.size() + 1];
                
                Class<?> controlClass = Class.forName(AbstractControl.class.getPackage().getName() +"."+ k.getClazz());
                arr[0] = controlClass.getSimpleName();
                
                ControlsAttributes annotation = controlClass.getAnnotation(ControlsAttributes.class);
                OperationKind defaultOperation = annotation.bindedClass().defaultOperation();
                int count = 1; 
                for (OperationKind kind : operations)
                {
                    if (annotation.bindedClass().isAllowed(kind))
                    {
                        boolean isDefaultOperation = kind == defaultOperation;
                        if (isDefaultOperation)
                        {
                            arr[count] = "+|RED";
                        }
                        else
                        {
                            arr[count] = "+";
                        }
                    }
                    else
                    {
                        arr[count] = "";
                    }
                    count++;
                }
                table.addValue(arr);
            }
            
            MatrixItem tableItem = new HelpTable(title, table, bordered, new int[] {}); 
            root.insert(root.count(), tableItem);
        }
        catch (Exception e)
        {
        }
    }

    private static void addTextLine(MatrixItem root, String str) throws Exception
    {
        MatrixItem line = new HelpTextLine(str);
        root.insert(root.count(), line);
    }

    private static void addText(MatrixItem root, InputStream stream) throws Exception
    {
        MatrixItem text = new HelpText(stream);
        root.insert(root.count(), text);
    }

    private static void addClass(MatrixItem root, Class<?> clazz) throws Exception
    {
        MatrixItem item = new HelpClass(clazz);
        root.insert(root.count(), item);
    }

    @SuppressWarnings("unchecked")
    private static void addAllItems(MatrixItem root, Content content) throws Exception
    {
        content.add(new ContentItem(addPartOfContent("Matrix syntax", true)));

        MatrixItem item = new HelpTextLine("{{2Items2}}");
        root.insert(root.count(), item);

        for (Class<?> clazz : Parser.knownItems)
        {
            Deprecated deprecated = clazz.getAnnotation(Deprecated.class);
            if (deprecated != null)
            {
                continue;
            }
            
            MatrixItemAttribute attribute = clazz.getAnnotation(MatrixItemAttribute.class);
            if (attribute == null)
            {
                continue;
            }

            if (!attribute.real() || clazz.equals(ActionItem.class) || clazz.equals(TempItem.class))
            {
                continue;
            }
            content.add(new ContentItem(addPartOfContent(clazz.getSimpleName(), false)));
            item.insert(item.count(), new HelpItem((Class<? extends MatrixItem>) clazz));
        }
        content.add(new ContentItem(addEndParentPartOfContent()));
    }
    
    @SuppressWarnings("unchecked")
    private static void addAllActions(MatrixItem root, Content content)
    {
        MatrixItem item = new HelpTextLine("{{1All actions by groups1}}");
        root.insert(root.count(), item);

        content.add(new ContentItem(addPartOfContent("All actions by groups", true)));

        Map<Class<?>, ActionGroups> map = new LinkedHashMap<>();
        for (Class<?> action : ActionsList.actions)
        {
            map.put(action, action.getAnnotation(ActionAttribute.class).group());
        }

        for (ActionGroups groups : ActionGroups.values())
        {
            content.add(new ContentItem(addPartOfContent(groups.toString(), true)));
            MatrixItem groupItem = new HelpTextLine("{{2" + groups.toString() + "2}}");
            item.insert(item.count(), groupItem);

            for (Map.Entry<Class<?>, ActionGroups> entry : map.entrySet())
            {
                Deprecated deprecated = entry.getKey().getAnnotation(Deprecated.class);
                if (deprecated != null)
                {
                    continue;
                }
                
                if (entry.getValue() == groups)
                {
                    content.add(new ContentItem(addPartOfContent(entry.getKey().getSimpleName(), false)));
                    groupItem.insert(groupItem.count(), new HelpActionItem((Class<? extends AbstractAction>) entry.getKey()));
                }
            }
            content.add(new ContentItem(addEndParentPartOfContent()));
        }
        content.add(new ContentItem(addEndParentPartOfContent()));
    }

    private static String addPartOfContent(String s, boolean hasChildren){
        if(hasChildren){
            return String.format("<li role='presentation' class='mParent' id='%s'>\n", s) +
            String.format("<a href='#'>%s<span class='caret'></span></a>\n</li>\n", s) +
            String.format("<ul class='nav nav-pills nav-stacked deepNav navChild' id='%s_child'>\n",s);
        } else {
            return String.format("<li role='presentation'>\n<a href='#%s'>%s</a>\n", s, s);
        }
    }

    private static String addEndParentPartOfContent(){
        return "</ul>\n";
    }

    private static void addPartOfContent(InputStream is, Content content) throws IOException{
        StringBuilder sb = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(is)) )
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
        }
        String text = sb.toString();
        String reg = "((\\{\\{[1|2]).*?([2|1]\\}\\}))";
        Pattern patt = Pattern.compile(reg, Pattern.DOTALL);
        Matcher m = patt.matcher(text);
        while (m.find()) {
            String foundedText = m.group();
            String mark = foundedText.replace("{{1", "").replace("1}}", "")
                    .replace("{{2", "").replace("2}}", "");

            content.add(new ContentItem(
                    String.format("<li role='presentation'>\n<a href='#%s'>%s</a>\n", mark.replaceAll("\\s+", "").toLowerCase(), mark))
            );
        }
    }

}
