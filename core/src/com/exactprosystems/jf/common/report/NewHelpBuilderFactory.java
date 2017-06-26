package com.exactprosystems.jf.common.report;

import java.io.IOException;
import java.util.Date;

public class NewHelpBuilderFactory extends ReportFactory {

    @Override
    public ReportBuilder createReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException {
        ReportBuilder result = new NewHelpBuilder(currentTime);
        result.init(new StringWriter());
        return result;
    }
}
