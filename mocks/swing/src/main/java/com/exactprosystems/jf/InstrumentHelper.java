package com.exactprosystems.jf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class InstrumentHelper {
    private Object id;
    private String BBCode;

    public Object getId() {
        return id;
    }

    InstrumentHelper(int num) {
        getBBCode();
        this.id = num;
    }

    public boolean equals(Object obj) {
        return obj instanceof InstrumentHelper && ((InstrumentHelper) obj).BBCode.equals(BBCode);
    }

    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (BBCode != null ? BBCode.hashCode() : 0);
        return result;
    }

    private String getBBCode() {
        String csvFile = "C:\\Users\\user_adm\\workspace - idea\\MockApp\\bbcodeTable.csv";
        String line = "";
        String cvsSplitBy = ",";
        String[] str = new String[0];

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                str = line.split(cvsSplitBy);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str[0];
    }
}
