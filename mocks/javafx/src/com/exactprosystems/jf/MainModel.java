package com.exactprosystems.jf;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

class MainModel
{
    private String[] data;
    private final TableColumn <String, String> head1;
    private final TableColumn <String, String> head2;
    private final TableColumn <String, String> head3;
    private final ObservableList <TableData> tableData;

    MainModel()
    {
        this.data = new String[]{"Green", "Yellow", "Orange", "Blue"};

        tableData = FXCollections.observableArrayList(
                new TableData("tr_1_td_1", "tr_1_td_2", "tr_1_td_3"),
                new TableData("tr_2_td_1", "tr_2_td_2", "tr_2_td_3"),
                new TableData("tr_3_td_1", "tr_3_td_2", "tr_3_td_3")
        );
        head1 = new TableColumn<>("Head1");
        head1.setCellValueFactory(new PropertyValueFactory<>("head1"));
        head2 = new TableColumn<>("Head2");
        head2.setCellValueFactory(new PropertyValueFactory<>("head2"));
        head3 = new TableColumn<>("Head3");
        head3.setCellValueFactory(new PropertyValueFactory<>("head3"));
    }

    String[] getData()
    {
        return this.data;
    }

    TableColumn[] getHeaders()
    {
        return new TableColumn[]{head1, head2, head3};
    }

    ObservableList <TableData> getTableData()
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
