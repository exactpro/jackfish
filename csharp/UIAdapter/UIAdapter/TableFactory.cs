/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Automation;
using UIAdapter;
using System.Text;
using System.Xml;

namespace UIAdapter.Tables
{
    class TableFactory
    {
        public static AbstractTable createTable(AutomationElement table)
        {
            string frameworkId = GetFrameworkId(table);
            switch (frameworkId)
            {
                case "WinForm": return new WinFormTable(table);
                case "Win32": return new Win32Table(table);
                case "WPF": return new WPFTable(table);
                case "Silverlight": return new SilverligthTable(table);
            }
            throw new Exception("Framework " + frameworkId + " not found");
        }

        public static String GetFrameworkId(AutomationElement element)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            string frameworkId = element.Current.FrameworkId;
            AutomationElement parent = element;
            while (String.IsNullOrEmpty(frameworkId))
            {
                if (parent == null)
                {
                    throw new Exception("Cant get framework id");
                }
                frameworkId = parent.Current.FrameworkId;
                parent = walker.GetParent(parent);
            }
            return frameworkId;
        }
    }

    /*
     *          TableStructures
     * MC - may contains, may not.
     * 
     *      WinForms
     * table
     *      pane    Vertical Scroll Bar             MC
     *      
     *      custom  TOP ROW                         Header
     *              Header  header1
     *              Header  header2
     *              Header  TopLeft Header Cell     MC
     *              
     *      custom  Row0
     *              custom  cell0
     *              custom  cell1
     *              header  Left Header             MC
     *      ...
     *      custom  RowN
     *              ...
     * 
     *      pane    Horizontal Scroll Bar           MC
     * 
     *      WPF
     * dataGrid
     *      DataItem    firstRow
     *          Custom  firstCell
     *              Text    textOfCurrentCell
     *          Custom  secondCell
     *              Text    textOfCurrentCell
     *          ...
     *          HeaderItem  headerItem              MC
     *              Thumb   gripTop
     *              Thumb   gripBot
     *
     *      DataItem    secondRow
     *          ...
     *
     *      ...
     *      Header
     *          HeaderItem  firstHeaderItem
     *              Thumb   gripLeft
     *              Thumb   gripRight
     *              Text    valueOfHeader
     *          HeaderItem  anotherHeaderItem
     * 
     * TODO add for silverlight and win32 ( need examples)
     * 
     */

    abstract class AbstractTable
    {
        public static readonly String EMPTY_CELL = "EMPTY_CELL_EMPTY";
        public static readonly String EMPTY_HEADER_CELL = "EMPTY_HEADER_CELL_EMPTY";

        protected readonly AutomationElement table;
        protected readonly Logger.Logger logger;

        public AbstractTable(AutomationElement table)
        {
            this.table = table;
            this.logger = Program.logger;
            this.logger.All("Create table " + this.GetType().Name);
        }

        #region Abstract methods
        protected abstract Boolean RowIsGood(AutomationElement row);
        protected abstract Boolean RowCellIsGood(AutomationElement rowCell);
        protected abstract Boolean HeaderIsGood(AutomationElement header);
        protected abstract Boolean HeaderCellIsGood(AutomationElement headerCell);

        public abstract String GetValueRowCell(AutomationElement element);
        public abstract String GetValueHeaderCell(AutomationElement headerCell);
        #endregion

        #region JnaMethods

        public AutomationElement FindCell(int column, int row)
        {
            long start = Program.getMilis();
            if (row < 0)
            {
                return FindHeaderCell(this.FindHeader(), column);
            }

            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement findRow = this.FindFirstRow();
            int currentRowNumber = 0;
            while (currentRowNumber != row)
            {
                if (findRow == null)
                {
                    throw new Exception(String.Format("Invalid row index {0}", row));
                }
                if (this.RowIsGood(findRow))
                {
                    currentRowNumber += 1;
                }
                findRow = walker.GetNextSibling(findRow);
            }
            if (findRow == null)
            {
                throw new Exception(String.Format("Row with number {0} not found", row));
            }
            int currentColumnNumber = 0;
            AutomationElement findCell = this.FindFirstCellFromRow(findRow);
            while (currentColumnNumber != column)
            {
                if (findCell == null)
                {
                    throw new Exception(String.Format("Invalid column index {0}", column));
                }
                if (this.RowCellIsGood(findCell))
                {
                    currentColumnNumber += 1;
                }
                findCell = walker.GetNextSibling(findCell);
            }
            logger.All("Find all cell in the table", Program.getMilis() - start);
            return findCell;
        }

        public String GetRowByIndex(int index)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement firstRow = this.FindFirstRow();
            int rows = 0;
            while (rows != index)
            {
                if (firstRow == null)
                {
                    throw new Exception("Invalid row index : " + index);
                }
                if (this.RowIsGood(firstRow))
                {
                    rows++;
                }
                firstRow = walker.GetNextSibling(firstRow);
            }
            AutomationElement header = this.FindHeader();
            StringBuilder sb = new StringBuilder();
            return sb
                .Append(this.HeaderToString(header, null))
                .Append(Program.SEPARATOR_ROWS)
                .Append(this.RowToString(firstRow))
                .ToString();
        }

        public int GetTableSize()
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement row = this.FindFirstRow();
            int i = 0;
            while (row != null)
            {
                if (this.RowIsGood(row))
                {
                    i++;
                }
                row = walker.GetNextSibling(row);
            }
            return i;
        }

        public String GetRow(String condition, String columns)
        {
            return RowViaCondtion(condition, columns, false);
        }

        public String GetRowIndexes(String condition, String columns)
        {
            return RowViaCondtion(condition, columns, true);
        }

        public String GetTable()
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement header = this.FindHeader();
            StringBuilder builder = new StringBuilder();

            builder.Append(this.HeaderToString(header, null));

            AutomationElement firstRow = this.FindFirstRow();

            while (firstRow != null)
            {
                if (this.RowIsGood(firstRow))
                {
                    builder.Append(Program.SEPARATOR_ROWS).Append(this.RowToString(firstRow));
                }
                firstRow = walker.GetNextSibling(firstRow);
            }
            return builder.ToString();
        }
        #endregion

        #region Protected methods, may be overrided
        protected virtual AutomationElement FindFirstRow()
        {
            return FindFirstByCondition(this.table, this.RowIsGood);
        }
        protected virtual AutomationElement FindFirstCellFromRow(AutomationElement row)
        {
            return FindFirstByCondition(row, this.RowCellIsGood, "Cant find first cell");
        }
        protected virtual AutomationElement FindHeader()
        {
            return FindFirstByCondition(this.table, this.HeaderIsGood, "Cant find header");
        }
        protected virtual AutomationElement FindFirstCellFromHeader(AutomationElement header)
        {
            return FindFirstByCondition(header, this.HeaderCellIsGood, "Cant find header cell");
        }

        protected void buildDom(AutomationElement start)
        {
            XmlDocument document = new XmlDocument();
            buildDom(document, start, document);
            String fileName = "myDom";
            int i = 0;
            while (System.IO.File.Exists(fileName))
            {
                fileName += ++i;
            }
            document.Save(DateTime.Now.ToString("yyyy MM dd HH mm ss fff") + fileName);
        }

        private void buildDom(XmlDocument document, AutomationElement element, XmlNode xmlElement)
        {
            String name = element.Current.ControlType.ProgrammaticName;
            XmlElement e = document.CreateElement(name);
            xmlElement.AppendChild(e);
            AutomationElementCollection col = element.FindAll(TreeScope.Children, Condition.TrueCondition);
            foreach (AutomationElement au in col)
            {
                buildDom(document, au, e);
            }
        }
        #endregion

        #region private methods

        private String RowViaCondtion(String condition, String columns, bool multiRows)
        {
            this.logger.All("Start get row, condition : " + condition);
            TreeWalker walker = TreeWalker.RawViewWalker;

            AutomationElement header = this.FindHeader();
            String headerToStr = this.HeaderToString(header, columns);

            StringBuilder builder = new StringBuilder();
            if (!multiRows)
            {
                builder.Append(headerToStr);
            }
            AutomationElement firstRow = this.FindFirstRow();

            Cond.Condition cond = null;
            Dictionary<string, int> indexes = new Dictionary<string, int>();
            if (!string.IsNullOrEmpty(condition))
            {
                cond = Cond.Condition.Deserialize(condition);
                HashSet<string> names = cond.GetNames();

                string[] headerCell = headerToStr.Split(new char[] { Program.SEPARATOR_CELL[0] }, StringSplitOptions.RemoveEmptyEntries);
                for (int i = 0; i < headerCell.Length; i++)
                {
                    if (names.Contains(headerCell[i]))
                    {
                        indexes.Add(headerCell[i], i);
                    }
                }
            }
            int findedIndex = 0;
            string sep = "";
            while (firstRow != null)
            {
                if (firstRow == null)
                {
                    throw new Exception("Cant find row via condition : " + condition);
                }
                if (this.RowIsGood(firstRow))
                {
                    if (RowMatchesNew(firstRow, indexes, cond))
                    {
                        if (multiRows)
                        {
                            builder.Append(sep).Append(findedIndex);
                            sep = Program.SEPARATOR_CELL;
                        }
                        else
                        {
                            builder.Append(Program.SEPARATOR_ROWS).Append(RowToString(firstRow));
                            this.logger.All("Return : " + builder.ToString());
                            return builder.ToString();
                        }
                    }
                    findedIndex++;
                }
                firstRow = walker.GetNextSibling(firstRow);
            }
            this.logger.All("Return : " + builder.ToString());
            return builder.ToString();
        }

        private AutomationElement FindHeaderCell(AutomationElement header, int column)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement headerCell = this.FindFirstCellFromHeader(header);
            int curCol = 0;
            while (curCol != column)
            {
                if (headerCell == null)
                {
                    throw new Exception(String.Format("Invalid column index {0}", column));
                }
                if (this.HeaderCellIsGood(headerCell))
                {
                    curCol++;
                }
                headerCell = walker.GetNextSibling(headerCell);
            }
            return headerCell;
        }

        private String HeaderToString(AutomationElement header, String columns)
        {
            string[] columnsArray = null;
            if (!String.IsNullOrEmpty(columns))
            {
                columnsArray = columns.Split(new char[] { Program.SEPARATOR_CELL[0] }, StringSplitOptions.RemoveEmptyEntries);
            }

            TreeWalker walker = TreeWalker.RawViewWalker;

            StringBuilder builder = new StringBuilder();
            string sep = "";
            AutomationElement headerCell = this.FindFirstCellFromHeader(header);
            int i = 0;
            while (headerCell != null)
            {
                if (this.HeaderCellIsGood(headerCell) && !headerCell.Current.IsOffscreen)
                {
                    String headerCellValue = this.GetValueHeaderCell(headerCell);
                    builder.Append(sep).Append(GetValueFromArray(columnsArray, i, headerCellValue));
                    sep = Program.SEPARATOR_CELL;
                    i++;
                }
                headerCell = walker.GetNextSibling(headerCell);
            }
            this.logger.All("Headers to string : " + builder.ToString());
            return builder.ToString();
        }

        private String GetValueFromArray(String[] array, int index, string defaultValue)
        {
            if (array == null)
            {
                return defaultValue;
            }
            if (index >= array.Length)
            {
                return "" + index;
            }
            return array[index];
        }

        private String RowToString(AutomationElement row)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            this.logger.All("Row : " + row.Current.Name + " and controlType : " + row.Current.ControlType.ProgrammaticName);
            AutomationElement firstCell = this.FindFirstCellFromRow(row);
            StringBuilder builder = new StringBuilder();
            string sep = "";
            while (firstCell != null)
            {
                if (this.RowCellIsGood(firstCell))
                {
                    builder.Append(sep).Append(this.GetValueRowCell(firstCell));
                    sep = Program.SEPARATOR_CELL;
                }
                firstCell = walker.GetNextSibling(firstCell);
            }
            return builder.ToString();
        }

        private Boolean RowMatchesNew(AutomationElement element, Dictionary<string, int> indexes, Cond.Condition condition)
        {
            Dictionary<string, object> row = RowToDictionary(element, indexes);
            return condition.IsMatched(row);
        }

        private Dictionary<string, object> RowToDictionary(AutomationElement row, Dictionary<string, int> indexes)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement firstCell = this.FindFirstCellFromRow(row);

            Dictionary<string, object> res = new Dictionary<string, object>();
            var iterator = indexes.GetEnumerator();
            bool next = iterator.MoveNext();
            for (int count = 0; firstCell != null && next; firstCell = walker.GetNextSibling(firstCell))
            {
                bool flag = this.RowCellIsGood(firstCell);
                if (flag)
                {
                    var currentKey = iterator.Current.Key;
                    if (iterator.Current.Value == count++)
                    {
                        next = iterator.MoveNext();
                    }
                    else
                    {
                        continue;
                    }
                    res.Add(currentKey, this.GetValueRowCell(firstCell));
                }
            }
            return res;
        }

        protected AutomationElement FindFirstByCondition(AutomationElement where, Predicate<AutomationElement> condition, String exceptionMsg)
        {
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement child = walker.GetFirstChild(where);
            while (child != null && !condition(child))
            {
                child = walker.GetNextSibling(child);
            }
            if (child == null && exceptionMsg != null)
            {
                throw new Exception(exceptionMsg);
            }
            return child;
        }

        protected AutomationElement FindFirstByCondition(AutomationElement where, Predicate<AutomationElement> condition)
        {
            return this.FindFirstByCondition(where, condition, null);
        }


        #endregion
    }

    class WPFTable : AbstractTable
    {
        public WPFTable(AutomationElement table)
            : base(table)
        {

        }

        public override string GetValueRowCell(AutomationElement element)
        {
            string value = "" + element.GetCurrentPropertyValue(Program.VALUE_PROPERTY);
            if (String.IsNullOrEmpty(value))
            {
                return EMPTY_CELL;
            }
            return value;
        }

        public override string GetValueHeaderCell(AutomationElement headerCell)
        {
            string cellValue = "" + headerCell.GetCurrentPropertyValue(Program.VALUE_PROPERTY);
            if (!String.IsNullOrEmpty(cellValue))
            {
                return cellValue;
            }
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement child = walker.GetFirstChild(headerCell);
            Predicate<AutomationElement> isValid = (c) =>
            {
                return !c.Current.ControlType.Equals(ControlType.Thumb);
            };
            while (child != null)
            {
                if (isValid(child))
                {
                    cellValue += child.Current.Name;
                }
                child = walker.GetNextSibling(child);
            }
            if (String.IsNullOrEmpty(cellValue))
            {
                return EMPTY_HEADER_CELL;
            }
            return cellValue;
        }

        protected override bool RowIsGood(AutomationElement row)
        {
            return row.Current.ControlType.Equals(ControlType.DataItem);
        }

        protected override bool HeaderCellIsGood(AutomationElement headerCell)
        {
            return headerCell.Current.ControlType.Equals(ControlType.HeaderItem);
        }

        protected override bool RowCellIsGood(AutomationElement rowCell)
        {
            return !rowCell.Current.ControlType.Equals(ControlType.HeaderItem);
        }

        protected override bool HeaderIsGood(AutomationElement header)
        {
            return header.Current.ControlType.Equals(ControlType.Header);
        }
    }

    class WinFormTable : AbstractTable
    {
        public WinFormTable(AutomationElement table)
            : base(table)
        {

        }

        public override string GetValueRowCell(AutomationElement element)
        {
            string value = "" + element.GetCurrentPropertyValue(Program.VALUE_PROPERTY);
            if (String.IsNullOrEmpty(value))
            {
                value = element.Current.Name;
                if (String.IsNullOrEmpty(value))
                {
                    return EMPTY_CELL;
                }
            }
            return value;
        }

        public override string GetValueHeaderCell(AutomationElement headerCell)
        {
            string value = "" + headerCell.GetCurrentPropertyValue(Program.VALUE_PROPERTY);
            if (String.IsNullOrEmpty(value))
            {
                value = headerCell.Current.Name;
                if (String.IsNullOrEmpty(value))
                {
                    return EMPTY_HEADER_CELL;
                }
            }
            return value;
        }

        protected override bool RowIsGood(AutomationElement row)
        {
            string rowName = row.Current.Name;
            return !rowName.ToUpper().Contains("SCROLL") && !rowName.ToUpper().Contains("TOP ROW");
        }

        protected override bool HeaderCellIsGood(AutomationElement headerCell)
        {
            return (headerCell.Current.ControlType.Equals(ControlType.Header)               // for event viewer
                && !headerCell.Current.Name.ToUpper().Contains("TOP LEFT HEADER CELL")) || headerCell.Current.ControlType.Equals(ControlType.HeaderItem);
        }

        protected override bool RowCellIsGood(AutomationElement rowCell)
        {
            return !rowCell.Current.ControlType.Equals(ControlType.Header);
        }

        protected override bool HeaderIsGood(AutomationElement header)
        {                                                               // for event viewer
            return header.Current.Name.ToUpper().Equals("TOP ROW") || header.Current.ControlType.Equals(ControlType.Header);
        }
    }

    class Win32Table : AbstractTable
    {
        public Win32Table(AutomationElement table)
            : base(table)
        {

        }

        public override string GetValueRowCell(AutomationElement rowCell)
        {
            string cellValue = rowCell.Current.Name;
            if (!String.IsNullOrEmpty(cellValue))
            {
                return cellValue;
            }
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement child = walker.GetFirstChild(rowCell);
            while (child != null)
            {
                cellValue += child.Current.Name;
                child = walker.GetNextSibling(child);
            }
            if (String.IsNullOrEmpty(cellValue))
            {
                return EMPTY_CELL;
            }
            return cellValue;
        }

        public override string GetValueHeaderCell(AutomationElement headerCell)
        {
            string headerValue = headerCell.Current.Name;
            if (!String.IsNullOrEmpty(headerValue))
            {
                return headerValue;
            }
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement child = walker.GetFirstChild(headerCell);
            while (child != null)
            {
                headerValue += child.Current.Name;
                child = walker.GetNextSibling(child);
            }
            if (String.IsNullOrEmpty(headerValue))
            {
                return EMPTY_HEADER_CELL;
            }
            return headerValue;
        }

        protected override bool RowIsGood(AutomationElement row)
        {
            return row.Current.ControlType.Equals(ControlType.DataItem);
        }

        protected override bool HeaderCellIsGood(AutomationElement headerCell)
        {
            return headerCell.Current.ControlType.Equals(ControlType.HeaderItem);
        }

        protected override bool RowCellIsGood(AutomationElement rowCell)
        {
            //TODO think about it
            return true;
        }

        protected override bool HeaderIsGood(AutomationElement header)
        {
            return header.Current.ControlType.Equals(ControlType.Header);
        }
    }

    class SilverligthTable : AbstractTable
    {
        public SilverligthTable(AutomationElement table)
            : base(table)
        {
        }

        public override string GetValueRowCell(AutomationElement rowCell)
        {
            string cellValue = rowCell.Current.Name;
            if (!String.IsNullOrEmpty(cellValue))
            {
                return cellValue;
            }
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement child = walker.GetFirstChild(rowCell);
            while (child != null)
            {
                cellValue += child.Current.Name;
                child = walker.GetNextSibling(child);
            }
            if (String.IsNullOrEmpty(cellValue))
            {
                return EMPTY_CELL;
            }
            return cellValue;
        }

        public override string GetValueHeaderCell(AutomationElement headerCell)
        {
            string headerCellValue = headerCell.Current.Name;
            if (!String.IsNullOrEmpty(headerCellValue))
            {
                return headerCellValue;
            }
            TreeWalker walker = TreeWalker.RawViewWalker;
            AutomationElement child = walker.GetFirstChild(headerCell);
            while (child != null)
            {
                headerCellValue += child.Current.Name;
                child = walker.GetNextSibling(child);
            }
            if (String.IsNullOrEmpty(headerCellValue))
            {
                return EMPTY_HEADER_CELL;
            }
            return headerCellValue;
        }

        protected override bool RowIsGood(AutomationElement row)
        {
            bool controlTypeIsDataItem = row.Current.ControlType.Equals(ControlType.DataItem);
            if (!controlTypeIsDataItem)
            {
                return false;
            }
            return TreeWalker.RawViewWalker.GetFirstChild(row) != null;
        }

        protected override bool HeaderCellIsGood(AutomationElement headerCell)
        {
            return headerCell.Current.ControlType.Equals(ControlType.HeaderItem);
        }

        protected override bool RowCellIsGood(AutomationElement rowCell)
        {
            return !rowCell.Current.ControlType.Equals(ControlType.HeaderItem);
        }

        protected override bool HeaderIsGood(AutomationElement header)
        {
            return header.Current.ControlType.Equals(ControlType.Header);
        }

        protected override AutomationElement FindFirstCellFromRow(AutomationElement row)
        {
            //buildDom(row);
            return base.FindFirstCellFromRow(row);
        }

        protected override AutomationElement FindFirstRow()
        {
            AutomationElement child = this.FindFirstByCondition(this.table, (c) => c.Current.ClassName.ToUpper().Contains("ROWSPRESENTER"), "Row layout is not presented");
            return this.FindFirstByCondition(child, this.RowIsGood);
        }
    }
}
