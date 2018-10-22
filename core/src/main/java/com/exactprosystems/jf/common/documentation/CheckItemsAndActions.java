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
package com.exactprosystems.jf.common.documentation;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckItemsAndActions {

    public CheckItemsAndActions() {
    }

    public static void main(String[] args){
        checkItems();
        checkActions();
    }

    private static void checkItems(){
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

            checkPartOfAttribute(attribute.constantGeneralDescription().get(), clazz.getSimpleName());
            checkPartOfAttribute(attribute.constantExamples().get(), clazz.getSimpleName());

        }
    }

    private static void checkActions(){
        for (Class<?> clazz : ActionsList.actions)
        {
            ActionAttribute attr = clazz.getAnnotation(ActionAttribute.class);
            if (attr != null){
	            String generalDescription = attr.constantGeneralDescription().get();
	            checkPartOfAttribute(generalDescription, clazz.getSimpleName());

				String examples = attr.constantExamples().get();
				checkPartOfAttribute(examples, clazz.getSimpleName());

				String additionalDescription = attr.constantAdditionalDescription().get();
                checkPartOfAttribute(additionalDescription, clazz.getSimpleName());

                String outputDescription = attr.constantOutputDescription().get();
                checkPartOfAttribute(outputDescription, clazz.getSimpleName());
            }
        }
    }

    private static void checkPartOfAttribute(String string, String className){
        if (!Str.IsNullOrEmpty(string)){
            Pattern openPattern = Pattern.compile("(\\{\\{[1|2|3|4|5|$|#|@|\\^|`|_|*|/|&|=|-])");
            Pattern closePattern = Pattern.compile("([1|2|3|4|5|$|#|@|\\^|`|_|*|/|&|=|-]\\}\\})");
            Matcher open = openPattern.matcher(string);
            int openCount = 0;
            int closeCount = 0;
            while (open.find()){
                openCount +=1;
            }
            Matcher close = closePattern.matcher(string);
            while (close.find()){
                closeCount +=1;
            }
            if (openCount != closeCount){
                System.out.println(className + " in string -> " + string);
            }
        }
    }
}
