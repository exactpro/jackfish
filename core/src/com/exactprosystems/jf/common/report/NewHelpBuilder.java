package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Content;

import java.io.*;
import java.nio.file.*;
import java.util.Date;

public class NewHelpBuilder extends ReportBuilder {

    private static final long serialVersionUID = -5583389098545753476L;

    public NewHelpBuilder(Date currentTime) throws IOException
    {
        super(".", "help", currentTime);
    }

    @Override
    protected String postProcess(String source) {
        return super.postProcess(source);
    }

    @Override
    protected String decorateStyle(String value, String style) {
        String rgbColor;
        switch (style){
            case "BLACK": rgbColor = "#000000";
                break;
            case "BLUE": rgbColor = "#0000FF";
                break;
            case "CYAN": rgbColor = "#00FFFF";
                break;
            case "DARK_GRAY": rgbColor = "#A9A9A9";
                break;
            case "GRAY": rgbColor = "#808080";
                break;
            case "GREEN": rgbColor = "#00FF00";
                break;
            case "LIGHT_GRAY": rgbColor = "#D3D3D3";
                break;
            case "MAGENTA": rgbColor = "#FF00FF";
                break;
            case "ORANGE": rgbColor = "#FFA500";
                break;
            case "PINK": rgbColor = "#FFC0CB";
                break;
            case "RED": rgbColor = "#FF0000";
                break;
            case "WHITE": rgbColor = "#FFFFFF";
                break;
            case "Failed": rgbColor = "#FF0000";
                break;
            default: rgbColor = "#000000";
        }

        return String.format("<span style=\"color: %s\">%s</span>", rgbColor, value);
    }

    @Override
    protected String decorateLink(String name, String link) {
        return String.format("<a href=\"#%s\">%s</a>", name, link);
    }

    @Override
    protected String decorateExpandingBlock(String name, String content) {
        return null;
    }

    @Override
    protected String decorateGroupCell(String content, int level, boolean isNode) {
        return content;
    }

    @Override
    protected String replaceMarker(String marker) {
        return HTMLhelper.newHtmlMarker(marker);
    }

    @Override
    protected String generateReportName(String outputPath, String matrixName, String suffix, Date date) throws IOException {
        return "";
    }

    @Override
    protected String generateReportDir(String matrixName, Date date) throws IOException {
        return null;
    }

    @Override
    protected void putMark(ReportWriter writer, String mark) throws IOException {
        String[] s = mark.split("\\.");
        String correctMark = s[s.length -1];
        writer.fwrite("<a id=\"%s\"></a><br>", correctMark).newline();
    }

    @Override
    protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException {
        writer.fwrite("<!DOCTYPE html>");
        writer.fwrite("<html>");
        writer.fwrite("<script type='text/javascript'>\n<!--\n");
        writer.include(getClass().getResourceAsStream("jquery-3.1.1.min.js"));
        writer.fwrite("-->\n</script>\n");

        writer.fwrite("<script type='text/javascript'>\n<!--\n");
        writer.include(getClass().getResourceAsStream("bootstrap.min.js"));
        writer.fwrite("-->\n</script>\n");

        writer.fwrite("<style>\n" + "<!--\n");
        writer.include(getClass().getResourceAsStream("bootstrap.min.css"));
        writer.fwrite("-->\n" + "</style>\n");

        writer.fwrite("<style>\n" + "<!--\n");
        writer.include(getClass().getResourceAsStream("help.css"));
        writer.fwrite("-->\n" + "</style>\n");
        writer.fwrite("<head>");
        writer.fwrite("<title>Help</title>");
        writer.fwrite("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
        writer.fwrite("</head>");
        writer.fwrite("<body>").newline();
    }

    @Override
    protected void reportMatrixHeader(ReportWriter writer, String matrix) throws IOException {
        // nothing to do
    }

    @Override
    protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException {
        // nothing to do
    }

    @Override
    protected void reportMatrixFooter(ReportWriter writer) throws IOException {
        // nothing to do
    }

    @Override
    protected void reportHeaderTotal(ReportWriter writer, Date date) throws IOException {
        // nothing to do
    }

    @Override
    protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException {
        writer.fwrite("</body>");
        writer.fwrite("</html>");
    }

    @Override
    protected void reportItemHeader(ReportWriter writer, MatrixItem entry, Integer id) throws IOException {
        String itemId = entry.getId();

        if (itemId == null)
        {
            itemId = "";
        }
    }

    @Override
    protected void reportContent(ReportWriter writer, MatrixItem item, String beforeTestcase, Content content, String title) throws IOException {
        //todo
    }

    @Override
    protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException {
        writer.fwrite(string + "<br>");
    }

    @Override
    protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, int scale, ImageReportMode reportMode) throws IOException {
        writer.fwrite("<img src=\"%s\" alt=\"%s\" >", fileName, title);
    }

    @Override
    protected void reportItemFooter(ReportWriter writer, MatrixItem entry, Integer id, long time, ImageWrapper screenshot) throws IOException {
        //nothing to do
    }

    @Override
    protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException {
        boolean columnWidth = percents.length != 0 && columns.length == percents.length;
        if (!Str.IsNullOrEmpty(tableTitle)){
            writer.fwrite("<h3>" + tableTitle + "</h3>").newline();
        }
        writer.fwrite("<table class='table table-bordered'>\n");
        if (columnWidth){
            writer.fwrite("<colgroup>");
            for(int i = 0; i < percents.length; i++){
                writer.fwrite("<col span=\"1\" style=\"width: " + percents[i] + "%;\">");
            }
            writer.fwrite("</colgroup>");
        }
        writer.fwrite("<thead>\n");
        writer.fwrite("<tr>\n");
        for (String column : columns)
        {
            writer.fwrite("<th>%s</th>", column);
        }
        writer.fwrite("</tr>\n");
        writer.fwrite("</thead>\n");
    }

    @Override
    protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object... value) throws IOException {
        if (value != null){
            writer.fwrite("<tr>");
            int count = 0;
            for (Object obj : value)
            {
                writer.fwrite("<td>%s</td>", ReportHelper.objToString(obj, count >= quotes));
                count++;
            }
            writer.fwrite("</tr>");
            writer.fwrite("\n");
        }
    }

    @Override
    protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException {
        writer.fwrite("</table>");
    }

    @Override
    protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException {
        // ???
    }

    public void printOut() throws  IOException{
        String content = this.getContent();
        System.out.println(content.length());
        File file = new File("New_Help.html");
        Path path = file.toPath();
        Files.deleteIfExists(path);
        PrintWriter pw =  new PrintWriter(file);
        pw.print(content);
        pw.close();
    }
}
