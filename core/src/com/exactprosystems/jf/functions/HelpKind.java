package com.exactprosystems.jf.functions;

public enum HelpKind
{ 
    ChooseSaveFile          ("…"), 
    ChooseOpenFile          ("…"), 
    ChooseFolder            ("…"), 
    ChooseDateTime          ("D"), 
    ChooseFromList          ("≡"), 
    BuildQuery              ("S"), 
    BuildXPath              ("X"), 
    BuildLayoutExpression   ("↔")
    ;
    
    HelpKind (String label)
    {
        this.label = label;
    }
    
    public String getLabel()
    {
        return this.label;
    }
    
    
    private String label;
}
