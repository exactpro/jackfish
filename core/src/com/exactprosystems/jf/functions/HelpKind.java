package com.exactprosystems.jf.functions;

public enum HelpKind
{ 
    String                  (""),
    Number                  (""),
    Boolean                 (""),
    Expression              (""),
    ChooseSaveFile          ("…"), 
    ChooseOpenFile          ("…"), 
    ChooseFolder            ("…"), 
    ChooseDateTime          ("D"), 
    ChooseFromList          ("≡"), 
    BuildQuery              ("S"), 
    BuildXPath              ("X"), 
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
