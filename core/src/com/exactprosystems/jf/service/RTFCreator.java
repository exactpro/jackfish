package com.exactprosystems.jf.service;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.common.RtfHelp.Help;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemAttribute;

import com.tutego.jrtf.*;
import static com.tutego.jrtf.Rtf.rtf;
import static com.tutego.jrtf.RtfHeader.*;
import static com.tutego.jrtf.RtfInfo.*;
import static com.tutego.jrtf.RtfPara.*;
import static com.tutego.jrtf.RtfText.*;

import javax.lang.model.type.NullType;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

class RTFCreator {

    RTFCreator() {
    }

    private final String documentName = "JackFish.rtf";
    private static Rtf document = rtf();
    private static Help rtfHelp = new Help();
    private List<RtfPara> items = new ArrayList<>();
    private List<RtfPara> actions = new ArrayList<>();
    private List<RtfPara> contents = new ArrayList<>();
    private List<RtfPara> mvels = new ArrayList<>();
    private static final String workDir = System.getProperty("user.dir");
    private final String path =  workDir + File.separator + documentName;
    private final String link = "file:///" + path + "#";
    private final String trait = "123456789";
    private final URL pictureHeader = rtfHelp.header();
    private final URL pictureFooter = rtfHelp.footer();
    private final URL introduction = rtfHelp.introduction();
    private final URL mvelDoc = rtfHelp.mvel();

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
                        fontSize(20, "London Stock Exchange Group plc. Registered in England & Wales No 5369106. Registered office 10 Paternoster Square, London EC4M 7LS")
                ))
        );
    }

    private void createContents(List<String> actions, List<String> items) throws IOException
    {
        contents.add(p(fontSize(40, "Contents:")));
        contents.add(p());
        contents.add(p(text("1. "), text (" "), hyperlink(link + "Introduction", p("Introduction"))));
        contents.add(p(text("2. "), text (" "), hyperlink(link + "MVEL", p("MVEL"))));
        contents.add(p("3. Actions: "));
        int count = 0;
        for(String name : actions)
        {
            count++;
            contents.add(p(tab(), text(" 3." + count + " "), text (" "), hyperlink(link + name, p(name))));
        }

        contents.add(p("4. Items: "));
        count = 0;
        for(String name : items)
        {
            count++;
            contents.add(p(tab(), text(" 4." + count + " "), text (" "), hyperlink(link + name, p(name))));
        }
        writeContents();
    }

    private void createActionsManual(String className, ActionAttribute classAnnotations,
                                    Map<String, ActionFieldAttribute> fieldAnnotations)
    {
        String[] n = className.split("\\.");
        String name = n[n.length - 1] + trait;

        actions.add(p(tab(), bold(name), lineBreak()));
        actions.add(p(findHyperlinks(classAnnotations.generalDescription()).toArray()));
        actions.add(p());

        if (classAnnotations.additionFieldsAllowed())
        {
            actions.add(p(italic("Additional fields: "), text("Yes")));
            if (!classAnnotations.additionalDescription().equals(""))
                actions.add(p(findHyperlinks(classAnnotations.additionalDescription()).toArray()));
        } else {
            actions.add(p(italic("Additional fields: "), text("No")));
        }
        actions.add(p());

        if (classAnnotations.outputType() != NullType.class)
        {
            String[] out = classAnnotations.outputType().getName().split("\\.");
            String outName = out[out.length - 1];
            actions.add(p(italic("Output")));
            actions.add(p(findHyperlinks(classAnnotations.outputDescription()).toArray()));
            actions.add(p(text("Output type: " + outName)));
        } else
        {
            actions.add(p(italic("Output: "), text("Null")));
        }
        actions.add(p());

        if (!classAnnotations.seeAlso().equals(""))
        {
            List<RtfText> seeAlsoText = new ArrayList<>();
            String[] seeAlso = classAnnotations.seeAlso().split(",");
            Arrays.asList(seeAlso).forEach(r-> {
                        String q = r.replaceAll("(?U)[\\pP\\s]", "");
                        seeAlsoText.add(hyperlink(link + q, p(q)));
                        seeAlsoText.add(text(" "));
                    }
            );
            actions.add(p(italic("See also:")));
            actions.add(p(seeAlsoText.toArray()));
            actions.add(p());
        }

        if (!fieldAnnotations.isEmpty())
        {
            actions.add(p(italic("Fields:")));
            fieldAnnotations.forEach((fName, attr) ->
                    {
                        List<RtfText> listParams = new ArrayList<>();
                        actions.add(p(underline(attr.name())));
                        if (attr.mandatory())
                        {
                            actions.add(p(text("Mandatory: Yes")));
                        }
                        else
                        {
                            actions.add(p(text("Mandatory: No")));
                        }
                        actions.add(p(text("Description: ")));
                        findHyperlinks(attr.description()).forEach(el->
                                listParams.add(el)
                        );
                        actions.add(p(listParams.toArray()));
                        actions.add(p());
                    }
            );
        }

        if (!classAnnotations.examples().equals(""))
        {
            actions.add(p(italic("Examples")));
            actions.add(p(parseExamples(classAnnotations.examples()).toArray()));
            actions.add(p());
        }
    }

    private void createItemsManual(String className, MatrixItemAttribute classAnnotation)
    {
        String[] n = className.split("\\.");
        String name = n[n.length - 1] + trait;

        items.add(p(tab(), bold(name), lineBreak()));
        items.add(p(findHyperlinks(classAnnotation.description()).toArray()));
        items.add(p());
        if (classAnnotation.shouldContain().length != 0)
        {
            List<RtfText> shouldContain = new ArrayList<>();
            Arrays.asList(classAnnotation.shouldContain()).forEach(t->
                    {
                        shouldContain.add(text(t.get()));
                        shouldContain.add(text(" "));
                    }
            );
            items.add(p(italic("ShouldContain:")));
            items.add(p(shouldContain.toArray()));
            items.add(p());
        }

        if (classAnnotation.mayContain().length != 0)
        {
            List<RtfText> mayContain = new ArrayList<>();
            Arrays.asList(classAnnotation.mayContain()).forEach(t->
                    {
                        mayContain.add(text(t.get()));
                        mayContain.add(text(" "));
                    }
            );
            items.add(p(italic("MayContain:")));
            items.add(p(mayContain.toArray()));
            items.add(p());
        }

        if(classAnnotation.closes() != NullType.class)
        {
            String[] out = classAnnotation.closes().getName().split("\\.");
            String outName = out[out.length - 1];
            items.add(p(italic("Closes: "), text(outName)));
            items.add(p());
        } else
        {
            items.add(p(italic("Closes: "), text("Null")));
            items.add(p());
        }

        if(classAnnotation.real())
        {
            items.add(p(italic("Real: "), text("Yes")));
            items.add(p());
        }

        if(classAnnotation.hasValue())
        {
            items.add(p(italic("HasValue: "), text("Yes")));
            items.add(p());
        }

        if(classAnnotation.hasParameters())
        {
            items.add(p(italic("HasParameters: "), text("Yes")));
            items.add(p());
        }

        if(classAnnotation.hasChildren())
        {
            items.add(p(italic("HasChildren: "), text("Yes")));
            items.add(p());
        }

        if(classAnnotation.raw())
        {
            items.add(p(italic("Raw: "), text("Yes")));
            items.add(p());
        }

        items.add(p(lineBreak()));
    }

    private List<RtfText> parseExamples(String examples)
    {
        String[] strs = examples.split("\\s+");
        StringBuilder sb = new StringBuilder();
        List<RtfText> result = new ArrayList<>();
        boolean lineSeparator = false;
        boolean code = false;
        for (String s : strs)
        {
            if (s.contains("{{#") || s.contains("#}}") || code) {
                if (s.contains("{{#") && s.contains("#}}"))
                {
                    result.add(text(sb.toString()));
                    result.add(lineBreak());
                    sb.setLength(0);
                    result.add(font(1, replaceChars(s)));
                    result.add(lineBreak());
                } else if (s.contains("{{#") && !s.contains("#}}")) {
                    result.add(text(sb.toString()));
                    result.add(lineBreak());
                    sb.setLength(0);
                    result.add(font(1, replaceChars(s)));
                    code = true;
                    lineSeparator = true;
                } else if (s.contains("#}}") && code) {
                    if (lineSeparator) {
                        result.add(lineBreak());
                    }
                    result.add(font(1, replaceChars(s)));
                    result.add(lineBreak());
                    code = false;
                    lineSeparator = false;
                } else if (code) {
                    if (lineSeparator) {
                        result.add(lineBreak());
                        lineSeparator = false;
                    }
                    if (s.startsWith("#")) {
                        result.add(lineBreak());
                        result.add(font(1, replaceChars(s) + " "));
                        result.add(lineBreak());
                    }
                    else
                    {
                        result.add(font(1, replaceChars(s) + " "));
                    }
                }
            }
            else if (s.contains("{{$") || s.contains("$}}"))
            {
                result.add(text(sb.toString()));
                sb.setLength(0);
                result.add(italic(replaceChars(s) + " "));
            }
            else {
                sb.append(s + " ");
            }
        }
        if (sb.length() !=0)
        {
            result.add(text(sb.toString()));
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
                    result.add(hyperlink(link + name, p(name)));
                    result.add(text(" "));
                }
                else if (s.contains("{{$") || s.contains("$}}"))
                {
                    result.add(italic(replaceChars(s) + " "));
                }
                else
                {
                    result.add(text(replaceChars(s) + " "));
                }
                result.add(text(" "));
            }

        return result;
    }

    private void deleteDocument(String path) {
        try {
            File file = new File(path);

            if (file.delete()) {
                System.out.println(file.getName() + " is deleted!");
            } else {
                System.out.println("Delete operation is failed.");
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
                p(text("Title page")));
    }

    void createDescription() throws IOException, BadLocationException
    {
        InputStream is = introduction.openStream();
        RTFEditorKit rtfParser = new RTFEditorKit();
        Document doc = rtfParser.createDefaultDocument();
        rtfParser.read(is, doc, 0);
        String text = doc.getText(0, doc.getLength());

        document.section(createSectionFormat(), p(tab(), tab(), tab(), tab(), fontSize(40, "Introduction" + trait), lineBreak()), p(text(text)));
    }

    void createContents() throws IOException
    {
        List<String> actions = new ArrayList<>();
        List<String> items = new ArrayList<>();

        for (Class<?> clazz : Parser.knownItems)
        {
            String[] n = clazz.getName().split("\\.");
            String name = n[n.length - 1];
            items.add(name);
        }

        for (Class<?> clazz : ActionsList.actions)
        {
            String[] n = clazz.getName().split("\\.");
            String name = n[n.length - 1];
            actions.add(name);
        }

        createContents(actions, items);
    }

    private String replaceChars (String s)
    {
        return s.replace("(?U)[\\pP\\s]", "")
                .replace("{{&", "").replace("&}}", "")  //font2
                .replace("{{*", "").replace("*}}", "")  //bolder
                .replace("{{=", "").replace("=}}", "")  //row
                .replace("{{-", "").replace("-}}", "")  //cell
                .replace("{{$", "").replace("$}}", "")  //italic
                .replace("{{#", "").replace("#}}", ""); //code
    }

    void mvelDocumentation() throws IOException
    {
        mvels.add(p(tab(), tab(), tab(), tab(), fontSize(40, "MVEL" + trait), lineBreak()));
        BufferedReader br = new BufferedReader(new FileReader( new File(mvelDoc.getFile())));
        for(String line; (line = br.readLine()) != null; ) {
            String[] strs = line.split("\\s+");
            ArrayList<RtfText> text = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            boolean header = false;
            boolean cell = false;

            if (strs.length != 0)
            {
                //rows
                if (line.contains("{{=") && line.contains("=}}"))
                {
                    for (String s : strs)
                    {
                        if (s.contains("{{*Operator*}}") || s.contains("{{*Description*}}") || s.contains("{{*Example*}}"))
                        {
                            text.add(bold(replaceChars(s)));
                        }
                        else if (s.contains("{{-") && s.contains("-}}"))
                        {
                            text.add(text(replaceChars(s)));
                        }
                        else if (s.contains("{{-"))
                        {
                            sb.append(replaceChars(s) + " ");
                            cell = true;
                        }
                        else if (s.contains("-}}") && cell)
                        {
                            sb.append(replaceChars(s));
                            cell = false;
                            text.add(text(sb.toString()));
                            sb.setLength(0);
                        }
                        else if (cell)
                        {
                            sb.append(s + " ");
                        }
                        else
                        {
                            System.out.println("mvelDocumentation! " + s);
                        }
                    }
                    RtfText[] arr = new RtfText[text.size()];
                    mvels.add(row(text.toArray(arr)));
                    text.clear();
                }
                else if (line.contains("{{#") && line.contains("#}}")) //code
                {
                    for (String s : strs)
                    {
                        sb.append(replaceChars(s) + " ");
                    }
                    if (sb.length() != 0){
                        mvels.add(p(font(1, sb.toString())));
                        sb.setLength(0);
                    }
                }
                else
                {
                    for (String s : strs)
                    {
                        if (s.contains("{{&") && s.contains("&}}")){
                            if (sb.length() !=0){
                                text.add(text(sb.toString()));
                                text.add(text(" "));
                                mvels.add(p(text(sb.toString())));
                                sb.setLength(0);
                            }
                            mvels.add(p(font(2, replaceChars(s))));
                        }
                        else if (s.startsWith("{{&"))
                        {
                            sb.append(replaceChars(s) + " ");
                            header = true;
                        }
                        else if (header && s.contains("&}}"))
                        {
                            sb.append(replaceChars(s));
                            mvels.add(p(font(2, sb.toString())));
                            sb.setLength(0);
                            header = false;
                        }
                        else if (header)
                        {
                            sb.append(s + " ");
                        }
                        else if (s.contains("{{*") && s.contains("*}}"))
                        {
                            if (sb.length() != 0){
                                text.add(text(sb.toString()));
                                text.add(text(" "));
                                text.add(bold(replaceChars(s + " ")));
                                sb.setLength(0);
                            }
                        }
                        else
                        {
                            sb.append(s + " ");
                        }
                    }
                    if (sb.length() != 0){
                        text.add(text(sb.toString()));
                        text.add(text(" "));
                        RtfText[] arr = new RtfText[text.size()];
                        mvels.add(p(text.toArray(arr)));
                        text.clear();
                    }
                }
            }
            else
            {
                if (strs[0].contains("{{&") && strs[0].contains("&}}"))
                {
                    mvels.add(p(font(2, replaceChars(strs[0]))));
                }
                else if (strs[0].contains("{{#") && strs[0].contains("#}}"))
                {
                    mvels.add(p(font(1, replaceChars(strs[0]))));
                }
                else
                {
                    mvels.add(p(text(replaceChars(strs[0]))));
                }
            }
        }
        writeMvel();
    }

    void getAnnotationsForActions() throws IOException
    {
        actions.add(p(tab(),tab(), tab(),tab(), fontSize(40, "Actions" + trait), lineBreak()));
        for (Class<?> clazz : ActionsList.actions)
        {
            ActionAttribute classAnnotation = null;
            Map<String, ActionFieldAttribute> fieldAnnotations = new HashMap<>();

            Annotation[] ann = clazz.getAnnotations();
            for (Annotation an : ann){
                if (an instanceof ActionAttribute)
                {
                    classAnnotation = (ActionAttribute) an;
                }
            }

            Field[] fields =  clazz.getDeclaredFields();
            for (Field f : fields)
            {
                Annotation[] fAnnotations = f.getDeclaredAnnotations();
                Arrays.asList(fAnnotations).removeIf(fa -> !(fa instanceof ActionFieldAttribute));
                for (Annotation an : fAnnotations){
                    fieldAnnotations.put(f.getName(), (ActionFieldAttribute) an );
                }
            }
            createActionsManual(clazz.getName(), classAnnotation, fieldAnnotations);
        }
        writeActions();
    }

    void getAnnotationsForItems() throws IOException
    {
        items.add(p(tab(),tab(), tab(),tab(), fontSize(40, "Items" + trait), lineBreak()));
        for (Class<?> clazz : Parser.knownItems){
            MatrixItemAttribute classAnnotation = null;

            Annotation[] ann = clazz.getAnnotations();
            for (Annotation an : ann){
                if (an instanceof MatrixItemAttribute)
                {
                    classAnnotation = (MatrixItemAttribute) an;
                }
                else
                {
                    System.out.println(an.annotationType());
                }
            }
            createItemsManual(clazz.getName(), classAnnotation);
        }
        writeItems();
    }

    void saveDocument() throws IOException {
        document.out(new BookmarksCreator(path, true));
    }
}
