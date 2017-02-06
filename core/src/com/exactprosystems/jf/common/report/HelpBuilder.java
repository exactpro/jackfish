////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.OperationKind;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.common.ControlsAttributes;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemAttribute;
import com.exactprosystems.jf.documents.matrix.parser.items.TempItem;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HelpBuilder extends ReportBuilder
{
	private StringWriter menuWriter = new StringWriter();
	private StringWriter actionWriter = new StringWriter();

	public HelpBuilder(Date currentTime) throws IOException
	{
		super(null, null, currentTime);
	}

	@Override
	protected String postProcess(String result)
	{
		return super.postProcess(result);
		//		return super.postProcess(HTMLhelper.htmlescape(result));
	}

	@Override
	protected String decorateStyle(String value, String style)
	{
		return HTMLhelper.htmlMarker(value);
	}

	@Override
	protected String decorateLink(String name, String link)
	{
		return HTMLhelper.htmlMarker(name);
	}

	@Override
	protected String decorateExpandingBlock(String name, String content)
	{
		return HTMLhelper.htmlMarker(name);
	}

	@Override
	protected String replaceMarker(String marker)
	{
		return HTMLhelper.htmlMarker(marker);
	}

	@Override
	protected String generateReportName(String outputPath, String matrixName, String suffix, Date date)
	{
		return "";
	}

	@Override
	protected String generateReportDir(String matrixName, Date date) throws IOException
	{
		return null;
	}

	@Override
	protected void putMark(ReportWriter writer, String mark) throws IOException
	{
	}

	//region Matrix
	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrixName) throws IOException
	{
	}

	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException
	{
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer) throws IOException
	{
	}
	//endregion

	//region Header
	@Override
	protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException
	{
		writer.fwrite("<!DOCTYPE html>");
		writer.fwrite("<html>\n" + "<head>\n" + "<title>Help</title>\n" + "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n");

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

		writer.fwrite("</head>\n" + "<body>\n"
			+ "<div class='searchDiv'>\n"
			+ "<span class='searchControls'>\n"
			+ "<input class='searchInput' type='text' placeholder='Search'/\n>"
			+ "<button id='btnPrev'>&#923;</button>\n"
			+ "<button id='btnNext'>V</button>\n"
			+ "</span>\n"
			+ "<label class='searchLabel'></label\n>");
		writer.fwrite("<td><h0>Version <td>%s</h0></td>\n", VersionInfo.getVersion());
		writer.fwrite("</div>");

		writer.fwrite("<div class='container-fluid'>\n");
		writer.fwrite("<div class='row'>\n");

		this.menuWriter.fwrite("<div class='col-sm-3 menuCont'>\n");
		this.menuWriter.fwrite("<div class='mainMenu'>\n");
		this.menuWriter.fwrite("<ul class='nav nav-pills nav-stacked'>\n");

		this.actionWriter.fwrite("<div class='col-sm-9 helpViewer'>\n");

	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date) throws IOException
	{
		addMvelHelp();
		addAllControls();
	}

	@Override
	protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException
	{
		this.menuWriter.fwrite("</ul>\n</div>\n");
		this.menuWriter.fwrite("</div>\n");
		this.actionWriter.fwrite("</div>\n");

		writer.fwrite(this.menuWriter.toString());
		writer.fwrite(this.actionWriter.toString());

		writer.fwrite("</div>\n");
		writer.fwrite("</div>\n");
		writer.fwrite("<script type='text/javascript'>\n<!--\n");
		writer.include(getClass().getResourceAsStream("help.js"));
		writer.fwrite("-->\n</script>\n");
		writer.fwrite("</body>\n");
		writer.fwrite("</html>");
	}

	private void addMvelHelp() throws IOException
	{
		this.menuWriter.fwrite("<li role='presentation' class='mParent' id='MVELsyntax'>\n");
		this.menuWriter.fwrite("<a href='#'>MVEL syntax<span class='caret'></span></a>\n");
		this.menuWriter.fwrite("</li>\n");
		this.menuWriter.fwrite("<ul class='nav nav-pills nav-stacked navChild' id='MVELsyntax_child'>\n");
		this.menuWriter.fwrite("<li role='presentation'><a href='#BasicSyntax'>Basic syntax</a></li>\n");
		this.menuWriter.fwrite("<li role='presentation'><a href='#MVELOperators'>MVEL 2.0 Operators</a></li>\n");
		this.menuWriter.fwrite("<li role='presentation'><a href='#InlineListMapsAndArrays'>Inline List,Maps and Arrays</a></li>\n");
		this.menuWriter.fwrite("<li role='presentation'><a href='#MVELPropertyNavigation'>MVEL 2.0 Property navigation</a></li>\n");
		this.menuWriter.fwrite("<li role='presentation'><a href='#MVELLiterals'>MVEL 2.0 Literals</a></li>\n");
		this.menuWriter.fwrite("<li role='presentation'><a href='#MVELProjectionsAndFolds'>MVEL 2.0 Projections and Folds</a></li>\n");
		this.menuWriter.fwrite("<li role='presentation'><a href='#LambdaExpressions'>Lambda Expressions</a></li>\n");
		this.menuWriter.fwrite("</ul>\n");

		this.actionWriter.include(getClass().getResourceAsStream("mvel.html"));
	}

	private void addAllControls() throws IOException
	{
		this.menuWriter.fwrite("<li role='presentation'>\n");
		this.menuWriter.fwrite("<a href='#AllControls'>All controls</a>\n");
		this.menuWriter.fwrite("</li>\n");

		this.actionWriter.fwrite("<div id='AllControls'>\n");
		this.actionWriter.fwrite("<h2>All Controls</h2>\n");

		//region First column
		this.actionWriter.fwrite("<div class='co-1'>\n");
		this.actionWriter.fwrite("<div id='co-1Table'></div>\n");

		this.actionWriter.fwrite("<table class='table table-bordered table-condensed table-striped'>\n");
		this.actionWriter.fwrite("<thead>\n");
		this.actionWriter.fwrite("<tr>\n<th>#</th>\n</tr>\n");
		this.actionWriter.fwrite("</thead>\n");
		this.actionWriter.fwrite("<tbody>\n");
		for (ControlKind controlKind : ControlKind.values())
		{
			try
			{
				Class<?> controlClass = Class.forName(AbstractControl.class.getPackage().getName() + "." + controlKind.getClazz());
				this.actionWriter.fwrite("<tr>\n<th>%s</th>\n</tr>\n", controlClass.getSimpleName());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		this.actionWriter.fwrite("</tbody>\n");
		this.actionWriter.fwrite("</table>\n");
		this.actionWriter.fwrite("</div>\n");
		//endregion

		//region Second column
		this.actionWriter.fwrite("<div class='co-2'>\n");

		this.actionWriter.fwrite("<div class='bigTable' id='doublescroll'>\n");

		this.actionWriter.fwrite("<table class='table table-bordered table-condensed table-striped bigTable-hover'>\n");
		this.actionWriter.fwrite("<thead>\n");
		this.actionWriter.fwrite("<tr>\n");
		for (OperationKind kind : OperationKind.values())
		{
			this.actionWriter.fwrite("<th>%s</th>", kind.toString());
		}
		this.actionWriter.fwrite("</tr>\n");
		this.actionWriter.fwrite("</thead>\n");
		this.actionWriter.fwrite("<tbody>\n");
		for (ControlKind k : ControlKind.values())
		{
			try
			{
				this.actionWriter.fwrite("<tr>");
				Class<?> controlClass = Class.forName(AbstractControl.class.getPackage().getName() +"."+ k.getClazz());
				ControlsAttributes annotation = controlClass.getAnnotation(ControlsAttributes.class);
				OperationKind defaultOperation = annotation.bindedClass().defaultOperation();
				for (OperationKind kind : OperationKind.values())
				{
					this.actionWriter.fwrite("<td>");
					if (annotation.bindedClass().isAllowed(kind))
					{
						boolean isDefaultOperation = kind == defaultOperation;
						if (isDefaultOperation)
						{
							this.actionWriter.fwrite("<font color='#ff0000'>");
						}
						this.actionWriter.fwrite("+");
						if (isDefaultOperation)
						{
							this.actionWriter.fwrite("</font>");
						}
					}
					this.actionWriter.fwrite("</td>\n");
				}
				this.actionWriter.fwrite("</tr>\n");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		this.actionWriter.fwrite("</tbody>\n");
		this.actionWriter.fwrite("</table>\n");
		this.actionWriter.fwrite("</div>\n");
		this.actionWriter.fwrite("</div>\n");
		//endregion

		this.actionWriter.fwrite("</div>\n");
		this.actionWriter.fwrite("<div class='clearfix'></div>");
	}

	//endregion

	private boolean remove = true;

	//region Items
	@Override
    protected void reportItemHeader(ReportWriter writer, MatrixItem item, Integer id) throws IOException
    {
		boolean hasChildren = item.count() > 0;
		String itemName = item.getItemName();
		String idOfItem = itemName.replace(" ", "");
		if (hasChildren)
		{

			this.menuWriter.fwrite("<li role='presentation' class='mParent' id='%s'>\n", idOfItem);
			this.menuWriter.fwrite("<a href='#'>%s<span class='caret'></span></a>\n", itemName);
			this.menuWriter.fwrite("</li>\n");
			this.menuWriter.fwrite("<ul class='nav nav-pills nav-stacked deepNav navChild' id='%s_child'>",idOfItem);
		}
		else
		{
			this.menuWriter.fwrite("<li role='presentation'>\n");
			this.menuWriter.fwrite("<a href='#%s'>%s</a>\n", idOfItem, itemName);
			this.menuWriter.fwrite("</li>\n");

			this.actionWriter.fwrite("<div id='%s'>\n", idOfItem);
			this.actionWriter.fwrite("<h2>%s</h2>\n", itemName);
		}
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{

	}
	
	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time, ImageWrapper screenshot) throws IOException
	{
		boolean hasChildren = item.count() > 0;
		if (hasChildren)
		{
			this.menuWriter.fwrite("</ul>");
		}
		else
		{
			this.actionWriter.fwrite("</div>");
		}
		return;
	}
	//endregion

	//region Tables
	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		this.actionWriter.fwrite("<table class='table table-bordered'>\n");
		this.actionWriter.fwrite("<thead>\n");
		this.actionWriter.fwrite("<tr>\n");
		for (String column : columns)
		{
			this.actionWriter.fwrite("<th>%s</th>", column);
		}
		this.actionWriter.fwrite("</tr>\n");
		this.actionWriter.fwrite("</thead>\n");
	}
	
	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException
	{
		if (value != null)
        {
			this.actionWriter.fwrite("<tr>");
			int count = 0;
			for (Object obj : value)
			{
				this.actionWriter.fwrite("<td>%s</td>", ReportHelper.objToString(obj, count >= quotes));
				count++;
			}
			this.actionWriter.fwrite("</tr>");
			this.actionWriter.fwrite("\n");
        }
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
		this.actionWriter.fwrite("</table>");
	}
	//endregion

	//region Images and charts
	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title, Boolean asLink) throws IOException
	{
	}
	//endregion

	public void helpCreate(ReportBuilder report) throws Exception {

		report.reportStarted(null,"");
		chapterStart("Matrix syntax");
		makeItemHelp(report);
		chapterFinish();
		chapterStart("All actions by groups");
		helpForActions(report);
		chapterFinish();
		report.reportFinished(0,0,new Date(),new Date());
	}


    private void makeItemHelp(ReportBuilder report) throws IllegalAccessException, InstantiationException
    {

        MatrixItem tmp;
        for (Class<?> clazz : Parser.knownItems)
        {

            MatrixItemAttribute attribute = clazz.getAnnotation(MatrixItemAttribute.class);
            if (attribute == null)
            {
                return;
            }

            if (!attribute.real() || clazz.equals(ActionItem.class) || clazz.equals(TempItem.class))
            {
                continue;
            }

            tmp = (MatrixItem) clazz.newInstance();
            report.itemStarted(tmp);
            report.itemIntermediate(tmp);
            ReportTable table = report.addTable("", null, true, 100, new int[] { 30, 70 });
            table.addValues("Description", attribute.description());
            table.addValues("Examples", attribute.examples());
            if (!attribute.seeAlso().equals(""))
            {
                table.addValues("See also", attribute.seeAlso());
            }
            report.itemFinished(tmp, 0, null);
        }
    }

    private void makeActionHelp(ReportBuilder report, Class<?> clazz) throws Exception
    {
        ActionItem tmp;

        tmp = new ActionItem(clazz.getSimpleName());
        report.itemStarted(tmp);
        report.itemIntermediate(tmp);
        ReportTable table;

        ActionAttribute attr = clazz.getAnnotation(ActionAttribute.class);

        table = report.addTable("", null, true, 0, new int[] { 30, 70 }, "Action item", clazz.getSimpleName());

        table.addValues("Description", attr.generalDescription());
        if (attr.additionFieldsAllowed())
        {
            table.addValues("Additional fields", "Yes");
            table.addValues("Additional fields description", attr.additionalDescription());
        }
        else
        {
            table.addValues("Additional fields", "No");
        }
        table.addValues("See also", attr.seeAlso());
        table.addValues("Examples", HTMLhelper.htmlescape(attr.examples()));

        // Input
        Field[] fields = clazz.getDeclaredFields();
        table = report.addTable("Input:", null, true, 4, new int[] { 0, 0, 60, 0, 0 }, "Field name", "Field type",
                "Description", "Mandatory");
        table.addValues();
        for (Field f : fields)
        {
            ActionFieldAttribute annotation = f.getAnnotation(ActionFieldAttribute.class);
            if (annotation == null)
            {
                continue;
            }
            table.addValues(annotation.name(), f.getType().getSimpleName(), annotation.description(),
                    annotation.mandatory() ? "Yes" : "No");
        }

        // Output
        table = report.addTable("Output:", null, true, 100, new int[] { 20, 40 }, "Output type", "Description");
        table.addValues(attr.outputType().getSimpleName(), attr.outputDescription());

        report.itemFinished(tmp, 0, null);

    }

	private void chapterStart(String chapter) throws IOException {
		this.menuWriter.fwrite("<li role='presentation' class='mParent' id='%s'>\n", chapter);
		this.menuWriter.fwrite("<a href='#'>%s<span class='caret'></span></a>\n", chapter);
		this.menuWriter.fwrite("</li>\n");
		this.menuWriter.fwrite("<ul class='nav nav-pills nav-stacked deepNav navChild' id='%s_child'>", chapter);
	}

	private void chapterFinish() throws IOException {
		this.menuWriter.fwrite("</ul>\n");

	}

	private void helpForActions(ReportBuilder report) throws Exception {
		Map<Class<?>, ActionGroups> map = new HashMap<>();
		for (Class<?> action : ActionsList.actions)
		{
			map.put(action, action.getAnnotation(ActionAttribute.class).group());
		}

		for (ActionGroups groups : ActionGroups.values())
		{
			chapterStart(groups.toString());
			for (Map.Entry<Class<?>, ActionGroups> entry : map.entrySet())
			{
				if (entry.getValue() == groups)
				{
					makeActionHelp(report, entry.getKey());
				}
			}
			chapterFinish();
		}
	}

}
