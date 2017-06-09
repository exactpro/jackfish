package com.exactprosystems.jf.common.documentation;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckItemsAndActions {

    public CheckItemsAndActions() {
    }

    public static void main(String[] args){

        for (Class<?> clazz : Parser.knownItems)
        {
            MatrixItemAttribute attribute = clazz.getAnnotation(MatrixItemAttribute.class);
            if (attribute == null)
            {
                continue;
            }

            if (!attribute.real() || clazz.equals(ActionItem.class) || clazz.equals(TempItem.class))
            {
                continue;
            }

            String examples = attribute.examples();

            if (!Str.IsNullOrEmpty(examples)){
                Pattern openPattern = Pattern.compile("(\\{\\{[#])");
                Pattern closePattern = Pattern.compile("([#]\\}\\})");
                Matcher open = openPattern.matcher(examples);
                int openCount = 0;
                int closeCount = 0;
                while (open.find()){
                    openCount +=1;
                }
                Matcher close = closePattern.matcher(examples);
                while (close.find()){
                    closeCount +=1;
                }
                if (openCount != closeCount){
                    System.out.println(clazz.getSimpleName());
                }
            }
        }
    }
}
