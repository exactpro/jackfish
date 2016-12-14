package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;

import java.util.List;

public class ActionsReportHelper {
    public static void fillListForParameter(Matrix matrix, List<ReadableValue> list, AbstractEvaluator evaluator) throws Exception
    {
        List<String> l = matrix.listOfIds(TestCase.class);
        if (!l.isEmpty())
        {
            //empty string for reset value
            list.add(0, new ReadableValue(""));
            l.forEach(s-> list.add(new ReadableValue(evaluator.createString(s))));
        }

    }
}
