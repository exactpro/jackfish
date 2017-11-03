package com.exactprosystems.jf;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class Table
{
    private TableColumn <String, String> head1;
    private TableColumn <String, String> head2;
    private TableColumn <String, String> head3;
    private ObservableList <TableData> tableData;

    Table()
    {
        this.tableData = FXCollections.observableArrayList(
                new TableData("tr_1_td_1", "tr_1_td_2", "tr_1_td_3"),
                new TableData("tr_2_td_1", "tr_2_td_2", "tr_2_td_3"),
                new TableData("tr_3_td_1", "tr_3_td_2", "tr_3_td_3")
        );
        this.head1 = new TableColumn<>("Head1");
        this.head1.setCellValueFactory(new PropertyValueFactory<>("head1"));
        this.head2 = new TableColumn<>("Head2");
        this.head2.setCellValueFactory(new PropertyValueFactory<>("head2"));
        this.head3 = new TableColumn<>("Head3");
        this.head3.setCellValueFactory(new PropertyValueFactory<>("head3"));
    }

    TableColumn[] getHeaders()
    {
        return new TableColumn[]{head1, head2, head3};
    }

    ObservableList<TableData> getTableData()
    {
        return tableData;
    }

    public static class TableData
    {
        private final SimpleStringProperty head1;
        private final SimpleStringProperty head2;
        private final SimpleStringProperty head3;

        private TableData(String head1, String head2, String head3)
        {
            this.head1 = new SimpleStringProperty(head1);
            this.head2 = new SimpleStringProperty(head2);
            this.head3 = new SimpleStringProperty(head3);
        }

        public String getHead1()
        {
            return head1.get();
        }

        public void setHead1(String head1)
        {
            this.head1.set(head1);
        }

        public String getHead2()
        {
            return head2.get();
        }

        public void setHead2(String head2)
        {
            this.head2.set(head2);
        }

        public String getHead3()
        {
            return head3.get();
        }

        public void setHead3(String head3)
        {
            this.head3.set(head3);
        }
    }
}
