package com.exactprosystems.jf.common.RtfHelp;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;

import java.util.Map;

class RtfActionsHelper {

    private String name;
    private ActionAttribute classAnnotation;
    private Map<String, ActionFieldAttribute> fieldAnnotations;

    RtfActionsHelper(String name, ActionAttribute classAnnotation, Map<String, ActionFieldAttribute> fieldAnnotations) {
        this.name = name;
        this.classAnnotation = classAnnotation;
        this.fieldAnnotations = fieldAnnotations;
    }

    String getName() {
        return name;
    }

    ActionAttribute getClassAnnotation() {
        return classAnnotation;
    }

    Map<String, ActionFieldAttribute> getFieldAnnotations() {
        return fieldAnnotations;
    }

    void setName(String name) {
        this.name = name;
    }

    void setClassAnnotation(ActionAttribute classAnnotation) {
        this.classAnnotation = classAnnotation;
    }

    void setFieldAnnotations(Map<String, ActionFieldAttribute> fieldAnnotations) {
        this.fieldAnnotations = fieldAnnotations;
    }
}
