package com.exactprosystems.jf.tool.matrix.grid;

import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Map;

public class TableGridPane extends GridPane
{
	private MatrixItem matrixItem;
	private Table table;

	private ArrayList<String> headerNames = new ArrayList<>();

	private static final int BUTTON_LINE		= 0;
	private static final int HEADER_LINE		= 1;
	private static final int BODY_START_LINE	= 2;

	private int rowCount = 0;
	private int columnCount = 0;

	public TableGridPane(MatrixItem matrixItem, Table table)
	{
		super();
		this.table = table;
		this.matrixItem = matrixItem;
		this.setGridLinesVisible(true);
		addButtons();
		addHeader();
		addBody();
	}

	private void addButtons()
	{
		HBox box = new HBox();
		Button addCol = new Button("+ col");
		Button addRow = new Button("+ row");

		Button removeCol = new Button("- col");
		Button removeRow = new Button("- row");

		box.getChildren().addAll(addCol, removeCol, addRow, removeRow);
		this.add(box, 0,BUTTON_LINE, Integer.MAX_VALUE, 1);

		addCol.setOnAction(event -> addColumn());
		removeCol.setOnAction(event -> removeColumn());

		addRow.setOnAction(event -> addRow());
		removeRow.setOnAction(event -> removeRow());
	}

	private void addHeader()
	{
		int headerSize = this.table.getHeaderSize();
		for (int i = 0; i < headerSize; i++)
		{
			String header = this.table.getHeader(i);
			this.headerNames.add(header);
			this.add(createColumn(header), i, HEADER_LINE);
			columnCount++;
		}
	}

	private void addBody()
	{
		int bodyCount = this.table.size();
		for (int i = 0; i < bodyCount; i++)
		{
			Map<String, Object> row = this.table.get(i);
			ArrayList<String> headerNames1 = this.headerNames;
			for (int j = 0; j < headerNames1.size(); j++)
			{
				String headerName = headerNames1.get(j);
				Object value = row.get(headerName);
				TextField textField = new TextField(value.toString());
				this.add(textField, j, i + BODY_START_LINE);
			}
			rowCount++;
		}
	}

	private Text createColumn(String name)
	{
		Text column = new Text(name);
		GridPane.setHalignment(column, HPos.CENTER);
		column.setFill(Color.DARKBLUE);
		return column;
	}

	private void removeColumn()
	{

	}

	private void addColumn()
	{
		String newName = "newColumn" + columnCount;
		this.table.addColumns(newName);
		columnCount++;
		this.add(createColumn(newName), columnCount, HEADER_LINE);
		for (int i = 0; i < this.table.size(); i++)
		{
			this.add(new TextField(), columnCount, i + BODY_START_LINE);
		}
	}

	private void removeRow()
	{

	}

	private void addRow()
	{

	}
}
