package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.tool.CssVariables;

import javafx.scene.paint.Color;

public enum TreeItemState
{
    UPDATE    (CssVariables.Icons.REFRESH,        CssVariables.COLOR_UPDATE,      Color.web("#2687fb")),
    ADD       (CssVariables.Icons.ADD_16_ICON,    CssVariables.COLOR_ADD,         Color.web("#2687fb")),
    MARK      (CssVariables.Icons.MARK_ICON,      CssVariables.COLOR_MARK,        Color.web("#2a9635")),
    QUESTION  (CssVariables.Icons.QUESTION_ICON,  CssVariables.COLOR_QUESTION,    Color.web("#f3c738"));

    TreeItemState(String iconPath, String cssStyle, Color color)
    {
        this.iconPath = iconPath;
        this.cssStyle = cssStyle;
        this.color = color;
    }

    public String getIconPath()
    {
        return this.iconPath;
    }
    
    public String getCssStyle()
    {
        return this.cssStyle;
    }

    public Color color()
    {
        return this.color;
    }

    private String cssStyle;
    private String iconPath;
    private Color color;
}