/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.util.Arrays;
import java.util.List;

class MockTable
{
    private TableColumn <TableData, String> head1;
    private TableColumn <TableData, String> head2;
    private TableColumn <TableData, String> head3;
    private ObservableList <TableData> tableData;

    MockTable()
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
        this.head2.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        this.head2.setOnEditCommit(event -> tableData.get(event.getTablePosition().getRow()).setHead2(event.getNewValue()));
        this.head3 = new TableColumn<>("Head3");
        this.head3.setCellValueFactory(new PropertyValueFactory<>("head3"));
    }

    List<TableColumn<TableData, String>> getHeaders()
    {
        return Arrays.asList(head1, head2, head3);
    }

    ObservableList<TableData> getTableData()
    {
        return this.tableData;
    }

    public class TableData
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
