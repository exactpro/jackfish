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
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;

/**
 *
 * Pickers can display some Images next to the headers. <br>
 * You can specify the image by providing custom StyleClass :<br>
 * 
 * <pre>
 * .picker-label{
 *   -fx-graphic: url("add.png"); 
 *   -fx-background-color: white;
 *   -fx-padding: 0 0 0 0;
 * }
 * </pre>
 * 
 * The {@link #onClick() } method does nothing by default, so you can override it
 * if you want to execute a custom action when the user will click on your Picker.
 * 
 * <h3>Visual:</h3> <center><img src="pickers.PNG" alt="Screenshot of Picker"></center>
 * 
 */
@Deprecated
public class Picker {

    private final ObservableList<String> styleClass = FXCollections.observableArrayList();

    /**
     * Default constructor, the default "picker-label" styleClass is applied.
     */
    public Picker() {
        this("picker-label"); //$NON-NLS-1$
    }

    /**
     * Initialize this Picker with the style classes provided.
     * @param styleClass 
     */
    public Picker(String... styleClass) {
        this.styleClass.addAll(styleClass);
    }
    
    /**
     * Initialize this Picker with the style classes provided.
     * @param styleClass 
     */
    public Picker(Collection<String> styleClass) {
        this.styleClass.addAll(styleClass);
    }


    /**
     * @return the style class of this picker.
     */
    public final ObservableList<String> getStyleClass() {
        return styleClass;
    }

    /**
     * This method will be called whenever the user clicks on this picker.
     */
    public void onClick(){
        //no-op by default
    }
}
