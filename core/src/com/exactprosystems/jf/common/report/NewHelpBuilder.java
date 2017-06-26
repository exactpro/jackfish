package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Content;

import java.io.IOException;
import java.util.Date;

public class NewHelpBuilder extends ReportBuilder {

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
        return String.format("<a href=\"%s\">%s</a>", name, link);
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
        return null; //todo
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
        writer.fwrite("<a name=\"%s\"></a>", mark);
        writer.fwrite("<br>");
    }

    @Override
    protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException {
        writer.fwrite("<!DOCTYPE html>");
        writer.fwrite("<html>");
        writer.fwrite("<head>");
        writer.fwrite("<title>Help</title>");
        writer.fwrite("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
        writer.fwrite("</head>");
        writer.fwrite("<body>");
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
        //todo https://www.w3schools.com/tags/att_col_width.asp
    }

    @Override
    protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object... value) throws IOException {
        //todo
    }

    @Override
    protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException {
        writer.fwrite("</table>");
    }

    @Override
    protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException {
        // ???
    }
}
