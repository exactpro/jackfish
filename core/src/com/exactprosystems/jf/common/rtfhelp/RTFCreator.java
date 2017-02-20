package com.exactprosystems.jf.common.rtfhelp;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemAttribute;
import com.exactprosystems.jf.api.common.DateTime;

import com.tutego.jrtf.*;
import static com.tutego.jrtf.Rtf.rtf;
import static com.tutego.jrtf.RtfHeader.*;
import static com.tutego.jrtf.RtfInfo.*;
import static com.tutego.jrtf.RtfPara.*;
import static com.tutego.jrtf.RtfText.*;

import javax.lang.model.type.NullType;
import javax.swing.text.BadLocationException;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.*;

class RTFCreator {

    RTFCreator() {
    }

    private static Rtf document = rtf();
    private static Help rtfHelp = new Help();
    private static String currentDate = new DateTime().str("dd LLLL YYYY");
    private final String documentName = "JackFish " + VersionInfo.getVersion() + ".rtf";
    private List<RtfPara> items = new ArrayList<>();
    private List<RtfPara> actions = new ArrayList<>();
    private List<RtfPara> contents = new ArrayList<>();
    private List<RtfPara> mvels = new ArrayList<>();
    private List<RtfPara> panels = new ArrayList<>();
    private List<RtfPara> intro = new ArrayList<>();
    private Map<String, List<RtfActionsHelper>> actionGroups = new HashMap<>();
    private List<String> itemsName = new ArrayList<>();
    private static final String workDir = System.getProperty("user.dir");
    private final String path =  workDir + File.separator + documentName;
    private final String link = "#";
    private final String trait = "123456789";
    private final String reverseTrait = "987654321";
    private final InputStream pictureIntro = rtfHelp.introPicture();
    private final InputStream pictureHeader = rtfHelp.headerPicture();
    private final InputStream pictureFooter = rtfHelp.footerPicture();
    private final InputStream introduction = rtfHelp.introduction();
    private final InputStream panelDoc = rtfHelp.panel();
    private final InputStream mvelDoc = rtfHelp.mvel();
    private final int fontSize = 20;

    private void writeItems() throws IOException
    {
        RtfPara[] arr = new RtfPara[items.size()];
        document.section(createSectionFormat(), items.toArray(arr));
    }

    private void writeMvel() throws IOException
    {
        RtfPara[] arr = new RtfPara[mvels.size()];
        document.section(createSectionFormat(), mvels.toArray(arr));
    }

    private void writePanel() throws IOException
    {
        RtfPara[] arr = new RtfPara[panels.size()];
        document.section(createSectionFormat(), panels.toArray(arr));
    }

    private void writeActions() throws IOException
    {
        RtfPara[] arr = new RtfPara[actions.size()];
        document.section(createSectionFormat(), actions.toArray(arr));
    }

    private void writeContents() throws IOException
    {
        RtfPara[] arr = new RtfPara[contents.size()];
        document.section(createSectionFormat(), contents.toArray(arr));
    }

    private void writeIntro() throws IOException
    {
        RtfPara[] arr = new RtfPara[intro.size()];
        document.section(createSectionFormat(), intro.toArray(arr));
    }

    private RtfSectionFormatAndHeaderFooter createSectionFormat() throws MalformedURLException{
        return RtfSectionFormatAndHeaderFooter.sectionFormatting(
                RtfSectionFormatAndHeaderFooter.headerForAllPages(p(
                        picture(pictureHeader)
                                .size(195, 45, RtfUnit.POINT)
                                .type(RtfPicture.PictureType.PNG),
                        text("WriteLine")
                )),
                RtfSectionFormatAndHeaderFooter.footerOnAllPages(p(
                        text("WriteTopLine"),
                        text("Exactpro Jackfish manual"),
                        lineBreak(),
                        text("Page "), text(currentPageNumber())
                ))
        );
    }

    private RtfSectionFormatAndHeaderFooter createFirstPageFormat() throws MalformedURLException{
        return RtfSectionFormatAndHeaderFooter.sectionFormatting(
                RtfSectionFormatAndHeaderFooter.headerForAllPages(p(
                        picture(pictureHeader)
                                .size(200, 50, RtfUnit.POINT)
                                .type(RtfPicture.PictureType.PNG),
                        text("WriteLine")
                )),
                RtfSectionFormatAndHeaderFooter.footerOnAllPages(p(
                        text("WriteTopLine"),
                        lineBreak(),
                        picture(pictureFooter)
                                .size(150, 45, RtfUnit.POINT)
                                .type(RtfPicture.PictureType.PNG),
                        lineBreak(),
                        fontSize(fontSize, "London Stock Exchange Group plc. Registered in England & Wales No 5369106. Registered office 10 Paternoster Square, London EC4M 7LS")
                ))
        );
    }

    private void createContents() throws IOException
    {
        contents.add(p(fontSize(40, "Contents:")));
        contents.add(p());
        contents.add(p(text("1. "), text (" "), hyperlink(link + "Introduction", p("Introduction" +reverseTrait))));
        contents.add(p(text("2. "), text (" "), hyperlink(link + "Architecture", p("Architecture" +reverseTrait))));
        contents.add(p(text("3. "), text (" "), hyperlink(link + "Requirements", p("System Requirements" +reverseTrait))));
        contents.add(p(text("4. "), text (" "), hyperlink(link + "panel", p("Configuration panel" +reverseTrait))));
        contents.add(p(text("5. "), text (" "), hyperlink(link + "MVEL", p("MVEL" +reverseTrait))));
        contents.add(p("6. Actions: "));
        int count = 0;
        int innerCount = 0;
        for( Map.Entry<String, List<RtfActionsHelper>> entry  : actionGroups.entrySet())
        {
            count++;
            contents.add(p(tab(), fontSize(fontSize, " 6." + count + " "), fontSize(fontSize, " " + entry.getKey())));
            for (RtfActionsHelper s : entry.getValue())
            {
                innerCount++;
                contents.add(p(tab(), tab(), fontSize(fontSize, " 6." + count + "." + innerCount + " " ), fontSize(fontSize, " "), hyperlink(link + s.getName(), p(fontSize(fontSize, s.getName() +reverseTrait) ))));
            }
            innerCount = 0;
        }

        contents.add(p("7. Items: "));
        count = 0;
        for(String name : itemsName)
        {
            count++;
            contents.add(p(tab(), fontSize(fontSize, " 7." + count + " "), fontSize(fontSize, " "), hyperlink(link + name, p(fontSize(fontSize, name+reverseTrait)))));
        }
        writeContents();
    }

    private void createActionsManual()
    {
        for( Map.Entry<String, List<RtfActionsHelper>> entry  : actionGroups.entrySet())
        {
            actions.add(p(bold(entry.getKey()), lineBreak()));

            for (RtfActionsHelper rah : entry.getValue())
            {
                String name = rah.getName() + trait;
                ActionAttribute classAnnotations = rah.getClassAnnotation();
                Map<String, ActionFieldAttribute> fieldAnnotations = rah.getFieldAnnotations();

                actions.add(p(bold(name), lineBreak()));
                actions.add(p(findHyperlinks(classAnnotations.generalDescription()).toArray()));
                actions.add(p());

                if (classAnnotations.additionFieldsAllowed())
                {
                    actions.add(p(italic(fontSize(fontSize, "Additional fields: ")), fontSize(fontSize, "Yes")));
                    if (!classAnnotations.additionalDescription().equals(""))
                        actions.add(p(findHyperlinks(classAnnotations.additionalDescription()).toArray()));
                } else {
                    actions.add(p(italic(fontSize(fontSize, "Additional fields: ")), fontSize(fontSize, "No")));
                }
                actions.add(p());

                if (classAnnotations.outputType() != NullType.class)
                {
                    String[] out = classAnnotations.outputType().getName().split("\\.");
                    String outName = out[out.length - 1];
                    actions.add(p(italic(fontSize(fontSize, "Output"))));
                    actions.add(p(findHyperlinks(classAnnotations.outputDescription()).toArray()));
                    actions.add(p(fontSize(fontSize, "Output type: " + outName)));
                } else
                {
                    actions.add(p(italic(fontSize(fontSize, "Output: ")), fontSize(fontSize, "Null")));
                }
                actions.add(p());

                if (!classAnnotations.seeAlso().equals(""))
                {
                    List<RtfText> seeAlsoText = new ArrayList<>();
                    String[] seeAlso = classAnnotations.seeAlso().split(",");
                    for (int i = 0; i < seeAlso.length; i++)
                    {
                        String q = replaceChars(seeAlso[i]);
                        seeAlsoText.add(hyperlink(link + q, p(fontSize(fontSize, q +reverseTrait))));
                        if (i != seeAlso.length -1){
                            seeAlsoText.add(fontSize(fontSize, ", "));
                        }
                    }
                    actions.add(p(italic(fontSize(fontSize, "See also:"))));
                    actions.add(p(seeAlsoText.toArray()));
                    actions.add(p());
                }

                if (!fieldAnnotations.isEmpty())
                {
                    actions.add(p(italic(fontSize(fontSize,"Fields:"))));
                    fieldAnnotations.forEach((fName, attr) ->
                            {
                                List<RtfText> listParams = new ArrayList<>();
                                actions.add(p(underline(fontSize(fontSize, attr.name()))));
                                for (RtfText el : findHyperlinks(attr.description())){
                                    listParams.add(el);
                                }
                                actions.add(p(listParams.toArray()));
                                if (attr.mandatory())
                                {
                                    actions.add(p(fontSize(fontSize, "Mandatory: Yes")));
                                }
                                else
                                {
                                    actions.add(p(fontSize(fontSize, "Mandatory: No")));
                                }
                                actions.add(p());
                            }
                    );
                }

                if (!classAnnotations.examples().equals(""))
                {
                    actions.add(p(italic(fontSize(fontSize, "Examples"))));
                    actions.addAll(parseExample(classAnnotations.examples()));
                    actions.add(p());
                }
            }
        }

    }

    private void createItemsManual(String className, MatrixItemAttribute classAnnotation)
    {
        String name = className + trait;

        items.add(p(bold(name), lineBreak()));
        items.add(p(findHyperlinks(classAnnotation.description()).toArray()));
        items.add(p());

        if (!classAnnotation.seeAlso().equals(""))
        {
            List<RtfText> seeAlsoText = new ArrayList<>();
            String[] seeAlso = classAnnotation.seeAlso().split(",");
            for (int i = 0; i < seeAlso.length; i++)
            {
                String q = replaceChars(seeAlso[i]);
                seeAlsoText.add(hyperlink(link + q, p(fontSize(fontSize, q +reverseTrait))));
                if (i != seeAlso.length -1){
                    seeAlsoText.add(fontSize(fontSize, ", "));
                }
            }
            items.add(p(italic(fontSize(fontSize, "See also:"))));
            items.add(p(seeAlsoText.toArray()));
            items.add(p());
        }

        if (!classAnnotation.examples().equals(""))
        {
            items.add(p(italic(fontSize(fontSize, "Examples"))));
            items.addAll(parseExample(classAnnotation.examples()));
            items.add(p());
        }
    }

    private List<RtfPara> parseExample(String examples)
    {
        String[] lines = examples.split("\\n");
        StringBuilder sb = new StringBuilder();
        List<RtfPara> result = new ArrayList<>();
        List<RtfText> newLine = new ArrayList<>();
        boolean code = false;
        for (String line : lines){
            String[] words = line.split("\\s");
            if (words.length != 0 )
            {
                for (String s : words)
                {
                    if (s.contains("{{#") || s.contains("#}}") || code) {
                        if (s.contains("{{#") && s.contains("#}}")) {
                            newLine.add(fontSize(fontSize, sb.toString()));
                            newLine.add(lineBreak());
                            sb.setLength(0);
                            newLine.add(font(1, fontSize(fontSize, replaceChars(s))));
                            newLine.add(lineBreak());
                        } else if (s.contains("{{#") && !s.contains("#}}")) {
                            newLine.add(fontSize(fontSize, sb.toString()));
                            newLine.add(lineBreak());
                            sb.setLength(0);
                            newLine.add(font(1, fontSize(fontSize, replaceChars(s))));
                            code = true;
                        } else if (s.contains("#}}") && code) {
                            newLine.add(font(1, fontSize(fontSize, replaceChars(s))));
                            newLine.add(lineBreak());
                            code = false;
                        } else if (code) {
                            if (s.startsWith("#")) {
                                newLine.add(font(1, fontSize(fontSize, replaceChars(s) + " ")));
                            } else {
                                newLine.add(font(1, fontSize(fontSize, replaceChars(s) + " ")));
                            }
                        }
                    }
                    else if (s.contains("{{$") || s.contains("$}}"))
                    {
                        newLine.add(fontSize(fontSize, sb.toString()));
                        sb.setLength(0);
                        newLine.add(italic(fontSize(fontSize, replaceChars(s) + " ")));
                    }
                    else if (s.contains("{{@") || s.contains("@$}}"))
                    {
                        newLine.add(fontSize(fontSize, sb.toString()));
                        sb.setLength(0);
                        newLine.add(hyperlink(link + replaceChars(s), p(fontSize(fontSize, replaceChars(s)))));
                    }
                    else {
                        sb.append(s).append(" ");
                    }
                }
                if (sb.length() != 0){
                    newLine.add(fontSize(fontSize, sb.toString()));
                    sb.setLength(0);
                }
                result.add(p(newLine.toArray()));
                newLine.clear();
            }
        }
        return result;
    }

    private List<RtfText> findHyperlinks(String description){
        String[] strs = description.split("\\s+");
        List<RtfText> result = new ArrayList<>();
        for (String s : strs)
            {
                if (s.contains("{{@") || s.contains("@}}"))
                {
                    String name = replaceChars(s);
                    result.add(hyperlink(link + name, p(fontSize(fontSize, name+reverseTrait))));
                    result.add(fontSize(fontSize, " "));
                }
                else if (s.contains("{{$") || s.contains("$}}"))
                {
                    result.add(italic(fontSize(fontSize, replaceChars(s) + " ")));
                }
                else
                {
                    result.add(fontSize(fontSize, replaceChars(s) + " "));
                }
                result.add(fontSize(fontSize, " "));
            }

        return result;
    }

    private void deleteDocument(String path) {
        try {
            File file = new File(path);

            if (file.delete()) {
                //gratz!
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void prepareDocument() throws IOException {
        deleteDocument(path);
        document.documentFormatting(RtfDocfmt.footnoteNumberingArabic());
        document.info(author("ExactProSystems"), subject("Contains info about classes which used in project"), title("JackFish manual"));
        document.header(color(233, 157, 80), font("WriteFonts"));
        document.section(
                createFirstPageFormat(),
                p(),
                p(font(0, fontSize(48, bold("JackFish"))),  text("FirstPageLine")),
                p(font(0, fontSize(36, bold("User guide")))),
                p(),
                row(fontSize(fontSize, "Version:"), fontSize(fontSize, VersionInfo.getVersion())),
                row(fontSize(fontSize, "Release date:"), fontSize(fontSize, currentDate))
        );
        document.section(
                createSectionFormat(),
                p(font(0, fontSize(30, bold("Document Information")))),
                p(),
                row(fontSize(fontSize, bold("Date")), fontSize(fontSize, bold("Version")), fontSize(fontSize, bold("By")), fontSize(fontSize, bold("Comments"))),
                row(fontSize(fontSize, currentDate), fontSize(fontSize, VersionInfo.getVersion()), fontSize(fontSize, "Valery Florov"), fontSize(fontSize, "Initial Draft")),
                p(),
                p(font(0, fontSize(30, bold("Abbreviations")))),
                p(),
                row(fontSize(fontSize, bold("Abbreviation")), fontSize(fontSize, bold("Meaning"))),
                row(fontSize(fontSize, "JF"), fontSize(fontSize, "JackFish"))
        );
    }

    private String replaceChars (String s)
    {
        return s.replace("(?U)[\\pP\\s]", "").trim()
                .replace("{{&", "").replace("&}}", "")  //font2
                .replace("{{*", "").replace("*}}", "")  //bolder
                .replace("{{=", "").replace("=}}", "")  //row
                .replace("{{-", "").replace("-}}", "")  //cell
                .replace("{{$", "").replace("$}}", "")  //italic
                .replace("{{#", "").replace("#}}", "")  //code
                .replace("{{!", "").replace("!}}", "")  //header
                .replace("{{@", "").replace("@}}", ""); //link
    }

    private void createDocumentation(InputStream stream, List<RtfPara> list) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));

        for(String line; (line = br.readLine()) != null; ) {
            String[] strs = line.split("\\s+");
            ArrayList<RtfText> text = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            boolean header = false;
            boolean cell = false;

            if (strs.length != 0)
            {
                //rows
                /*if (line.contains("{{=") && line.contains("=}}"))
                {
                    for (String s : strs)
                    {
                        if (s.contains("{{*Operator*}}") || s.contains("{{*Description*}}") || s.contains("{{*Example*}}"))
                        {
                            text.add(bold(fontSize(fontSize, replaceChars(s))));
                        }
                        else if (s.contains("{{-") && s.contains("-}}"))
                        {
                            text.add(fontSize(fontSize, replaceChars(s)));
                        }
                        else if (s.contains("{{-"))
                        {
                            sb.append(replaceChars(s)).append(" ");
                            cell = true;
                        }
                        else if (s.contains("-}}") && cell)
                        {
                            sb.append(replaceChars(s));
                            cell = false;
                            text.add(fontSize(fontSize, sb.toString()));
                            sb.setLength(0);
                        }
                        else if (cell)
                        {
                            sb.append(s).append(" ");
                        }
                        else
                        {

                        }
                    }
                    RtfText[] arr = new RtfText[text.size()];
                    list.add(row(text.toArray(arr)));
                    text.clear();
                }
                else*/ if (line.contains("{{#") && line.contains("#}}")) //code
                {
                    for (String s : strs)
                    {
                        sb.append(replaceChars(s)).append(" ");
                    }
                    if (sb.length() != 0){
                        list.add(p(font(1, fontSize(fontSize, sb.toString()))));
                        sb.setLength(0);
                    }
                }
                else if (line.contains("{{!") && line.contains("!}}")) //header
                {
                    for (int i = 0; i < strs.length; i++){
                        sb.append(replaceChars(strs[i]));
                        if (i != strs.length -1){
                            sb.append(" ");
                        }
                    }
                    if (sb.length() != 0){
                        list.add(p(tab(), tab(), tab(), tab(), font(0, fontSize(30, bold(sb.toString()+trait))), lineBreak()));
                        sb.setLength(0);
                    }
                }
                else
                {
                    for (String s : strs)
                    {
                        if (s.contains("{{&") && s.contains("&}}")){
                            if (sb.length() !=0){
                                text.add(fontSize(fontSize, sb.toString()));
                                text.add(fontSize(fontSize, " "));
                                list.add(p(fontSize(fontSize, sb.toString())));
                                sb.setLength(0);
                            }
                            list.add(p(font(0, fontSize(fontSize, replaceChars(s)))));
                        }
                        else if (s.startsWith("{{&"))
                        {
                            sb.append(replaceChars(s)).append(" ");
                            header = true;
                        }
                        else if (header && s.contains("&}}"))
                        {
                            sb.append(replaceChars(s));
                            list.add(p(font(0, fontSize(fontSize, sb.toString()))));
                            sb.setLength(0);
                            header = false;
                        }
                        else if (header)
                        {
                            sb.append(s).append(" ");
                        }
                        else if (s.equals("PutIntroPictureHere"))
                        {
                            list.add(p(picture(pictureIntro).type(RtfPicture.PictureType.PNG)));
                        }
                        else
                        {
                            sb.append(s).append(" ");
                        }
                    }
                    if (sb.length() != 0){
                        text.add(fontSize(fontSize, sb.toString()));
                        text.add(fontSize(fontSize, " "));
                        RtfText[] arr = new RtfText[text.size()];
                        list.add(p(text.toArray(arr)));
                        text.clear();
                    }
                }
            }
            else
            {
                if (strs[0].contains("{{&") && strs[0].contains("&}}"))
                {
                    list.add(p(font(0, fontSize(fontSize, replaceChars(strs[0])))));
                }
                else if (strs[0].contains("{{#") && strs[0].contains("#}}"))
                {
                    list.add(p(font(1, fontSize(fontSize, replaceChars(strs[0])))));
                }
                else if (strs[0].equals("PutIntroPictureHere"))
                {
                    list.add(p(picture(pictureIntro).type(RtfPicture.PictureType.PNG)));
                }
                else
                {
                    list.add(p(fontSize(fontSize, replaceChars(strs[0]))));
                }
            }
        }
    }

    private void createDescription() throws IOException, BadLocationException
    {
        intro.add(p(tab(), tab(), tab(), tab(), font(0, fontSize(30, bold("Introduction" + trait))), lineBreak()));
        createDocumentation(introduction, intro);
        writeIntro();
    }

    private void mvelDocumentation() throws IOException
    {
        mvels.add(p(tab(), tab(), tab(), tab(), font(0, fontSize(30, bold("MVEL" + trait))), lineBreak()));
        createDocumentation(mvelDoc, mvels);
        writeMvel();
    }

    private void panelDocumentation() throws IOException
    {
        createDocumentation(panelDoc, panels);
        writePanel();
    }

    void getAnnotationsForActions() throws IOException
    {
        List<RtfActionsHelper> gui = new ArrayList<>();
        List<RtfActionsHelper> system = new ArrayList<>();
        List<RtfActionsHelper> report = new ArrayList<>();
        List<RtfActionsHelper> app = new ArrayList<>();
        List<RtfActionsHelper> messages = new ArrayList<>();
        List<RtfActionsHelper> clients = new ArrayList<>();
        List<RtfActionsHelper> services = new ArrayList<>();
        List<RtfActionsHelper> tables = new ArrayList<>();
        List<RtfActionsHelper> text = new ArrayList<>();
        List<RtfActionsHelper> xml = new ArrayList<>();
        List<RtfActionsHelper> sql = new ArrayList<>();
        List<RtfActionsHelper> matrix = new ArrayList<>();

        actions.add(p(tab(),tab(), tab(),tab(), font(0, fontSize(30, bold("Actions" + trait))), lineBreak()));
        for (Class<?> clazz : ActionsList.actions)
        {
            ActionAttribute classAnnotation = null;
            Map<String, ActionFieldAttribute> fieldAnnotations = new HashMap<>();
            boolean deprecated = false;

            Annotation[] ann = clazz.getAnnotations();
            for (Annotation an : ann){
                if (an instanceof Deprecated)
                {
                    deprecated = true;
                }
                else if (an instanceof ActionAttribute)
                {
                    classAnnotation = (ActionAttribute) an;
                }
            }

            if (!deprecated)
            {
                Field[] fields =  clazz.getDeclaredFields();
                for (Field f : fields)
                {
                    Annotation[] fAnnotations = f.getDeclaredAnnotations();
                    Arrays.asList(fAnnotations).removeIf(fa -> !(fa instanceof ActionFieldAttribute));
                    for (Annotation an : fAnnotations){
                        fieldAnnotations.put(f.getName(), (ActionFieldAttribute) an );
                    }
                }
                String[] n = clazz.getName().split("\\.");
                String name = n[n.length - 1];
                RtfActionsHelper rah = new RtfActionsHelper(name, classAnnotation, fieldAnnotations);
                switch (classAnnotation.group()) {
                    case App:
                        app.add(rah);
                        break;
                    case Clients:
                        clients.add(rah);
                        break;
                    case GUI:
                        gui.add(rah);
                        break;
                    case Matrix:
                        matrix.add(rah);
                        break;
                    case Messages:
                        messages.add(rah);
                        break;
                    case Report:
                        report.add(rah);
                        break;
                    case Services:
                        services.add(rah);
                        break;
                    case SQL:
                        sql.add(rah);
                        break;
                    case System:
                        system.add(rah);
                        break;
                    case Tables:
                        tables.add(rah);
                        break;
                    case Text:
                        text.add(rah);
                        break;
                    case XML:
                        xml.add(rah);
                        break;
                }
            }
        }
        actionGroups.put("App", app);
        actionGroups.put("Clients", clients);
        actionGroups.put("GUI", gui);
        actionGroups.put("Matrix", matrix);
        actionGroups.put("Messages", messages);
        actionGroups.put("Report", report);
        actionGroups.put("Services", services);
        actionGroups.put("SQL", sql);
        actionGroups.put("System", system);
        actionGroups.put("Tables", tables);
        actionGroups.put("Text", text);
        actionGroups.put("XML", xml);
    }

    void getAnnotationsForItems() throws IOException, BadLocationException
    {
        items.add(p(tab(),tab(), tab(),tab(), font(0, fontSize(30, bold("Items" + trait))), lineBreak()));
        for (Class<?> clazz : Parser.knownItems){
            MatrixItemAttribute classAnnotation = null;
            boolean deprecated = false;

            Annotation[] ann = clazz.getAnnotations();
            for (Annotation an : ann){
                if (an instanceof Deprecated)
                {
                    deprecated = true;
                }
                else if (an instanceof MatrixItemAttribute)
                {
                    classAnnotation = (MatrixItemAttribute) an;
                }
            }

            if (!deprecated)
            {
                String[] n = clazz.getName().split("\\.");
                String name = n[n.length - 1];
                createItemsManual(name, classAnnotation);
                itemsName.add(name);
            }
        }
        createContents();
        createDescription();
        panelDocumentation();
        mvelDocumentation();
        createActionsManual();
        writeActions();
        writeItems();
    }

    void saveDocument() throws IOException {
        document.out(new BookmarksCreator(path, true));
    }
}
