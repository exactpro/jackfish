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
    protected String decorateStyle(String value, String style) {
        return null;
    }

    @Override
    protected String decorateLink(String name, String link) {
        return null;
    }

    @Override
    protected String decorateExpandingBlock(String name, String content) {
        return null;
    }

    @Override
    protected String decorateGroupCell(String content, int level, boolean isNode) {
        return null;
    }

    @Override
    protected String replaceMarker(String marker) {
        return null;
    }

    @Override
    protected String generateReportName(String outputPath, String matrixName, String suffix, Date date) throws IOException {
        return null;
    }

    @Override
    protected String generateReportDir(String matrixName, Date date) throws IOException {
        return null;
    }

    @Override
    protected void putMark(ReportWriter writer, String mark) throws IOException {

    }

    @Override
    protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException {

    }

    @Override
    protected void reportMatrixHeader(ReportWriter writer, String matrix) throws IOException {

    }

    @Override
    protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException {

    }

    @Override
    protected void reportMatrixFooter(ReportWriter writer) throws IOException {

    }

    @Override
    protected void reportHeaderTotal(ReportWriter writer, Date date) throws IOException {

    }

    @Override
    protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException {

    }

    @Override
    protected void reportItemHeader(ReportWriter writer, MatrixItem entry, Integer id) throws IOException {

    }

    @Override
    protected void reportContent(ReportWriter writer, MatrixItem item, String beforeTestcase, Content content, String title) throws IOException {

    }

    @Override
    protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException {

    }

    @Override
    protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, int scale, ImageReportMode reportMode) throws IOException {

    }

    @Override
    protected void reportItemFooter(ReportWriter writer, MatrixItem entry, Integer id, long time, ImageWrapper screenshot) throws IOException {

    }

    @Override
    protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException {

    }

    @Override
    protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object... value) throws IOException {

    }

    @Override
    protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException {

    }

    @Override
    protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException {

    }
}
