////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.Str;
import java.io.Serializable;

public class Locator implements Serializable
{

	private static final long serialVersionUID = 4842798390468499571L;

	public Locator(IControl control)
	{
		this.controlKind	= control.getBindedClass();

		this.id					= notNull(control.getID());
		this.uid				= notNull(control.getUID());
		this.xpath				= notNull(control.getXpath());
		this.clazz				= notNull(control.getClazz());
		this.name				= notNull(control.getName());
		this.title				= notNull(control.getTitle());
		this.action				= notNull(control.getAction());
		this.text				= notNull(control.getText());
		this.tooltip			= notNull(control.getTooltip());
		this.expression			= notNull(control.getExpression());
		this.weak 				= control.isWeak();
		this.addition			= control.getAddition();
		this.visibility			= control.getVisibility();
		this.useNumericHeader	= control.useNumericHeader();
		this.useAbsoluteXpath	= control.useAbsoluteXpath();
		this.columns			= notNull(control.getColumns());
	}

	public Locator(Operation operation, String id, ControlKind kind)
	{
		this.operation = operation;
		this.id = id;
		this.controlKind = kind;
	}

	public Locator()
	{
	}

	@Override
	public String toString()
	{
		String comma = "";
		StringBuilder sb = new StringBuilder("Locator ");
		if (this.controlKind != null)
		{
			sb.append(this.controlKind.toString());
			sb.append(" ");
		}
		sb.append("[");
		if (this.id != null)
		{
			sb.append("id = "); 	sb.append(this.id);  	comma = ", ";
		}
		if (this.uid != null)
		{
			sb.append(comma); 		sb.append("uid = "); 		sb.append(this.uid);  		comma = ", ";
		}
		if (this.clazz != null)
		{
			sb.append(comma); 		sb.append("class = "); 		sb.append(this.clazz);  	comma = ", ";
		}
		if (this.xpath != null)
		{
			sb.append(comma); 		sb.append("xpath = "); 		sb.append(this.xpath);  	comma = ", ";
		}
		if (this.name != null)
		{
			sb.append(comma); 		sb.append("name = "); 		sb.append(this.name);  		comma = ", ";
		}
		if (this.title != null)
		{
			sb.append(comma); 		sb.append("title = "); 		sb.append(this.title);  	comma = ", ";
		}
		if (this.text != null)
		{
			sb.append(comma); 		sb.append("text = "); 		sb.append(this.text);  		comma = ", ";
		}
		if (this.tooltip != null)
		{
			sb.append(comma); 		sb.append("tooltip = "); 	sb.append(this.tooltip);	comma = ", ";
		}
		if (this.action != null)
		{
			sb.append(comma); 		sb.append("action = ");		sb.append(this.action);  	comma = ", ";
		}
		if (this.expression != null)
		{
			sb.append(comma); 		sb.append("expression = ");	sb.append(this.expression); comma = ", ";
		}
		if (this.weak != null)
		{
			sb.append(comma); 		sb.append("weak = "); 		sb.append(this.weak);  		comma = ", ";
		}
		if (this.visibility != null)
		{
			sb.append(comma); 		sb.append("visibility = "); sb.append(this.visibility);	comma = ", ";
		}
		if (this.addition != null)
		{
			sb.append(comma); 		sb.append("addition = "); 	sb.append(this.addition);  	comma = ", ";
		}
		if (this.useAbsoluteXpath != null)
		{
			sb.append(comma);		sb.append("useAbsoluteXpath = ");sb.append(this.useAbsoluteXpath);
		}
		if (!Str.IsNullOrEmpty(this.columns))
		{
			sb.append(comma);		sb.append("columns = ");sb.append(this.columns);
		}
		
		sb.append("]");
		
		return sb.toString(); 
	}

	public ControlKind getControlKind()
	{
		return this.controlKind;
	}

	public String getId()
	{
		return this.id;
	}
	
	public String getXpath()
	{
		return this.xpath;
	}

	public String getUid()
	{
		return this.uid;
	}

	public String getClazz()
	{
		return this.clazz;
	}

	public String getName()
	{
		return this.name;
	}

	public String getTitle()
	{
		return this.title;
	}

	public String getAction()
	{
		return this.action;
	}

	public String getText()
	{
		return this.text;
	}

	public String getTooltip()
	{
		return this.tooltip;
	}

	public String getExpression()
	{
		return this.expression;
	}

	public String getColumns()
	{
		return columns;
	}

	public boolean isWeak()
	{
		return this.weak != null && this.weak.booleanValue();
	}

	public Addition getAddition()
	{
		return this.addition;
	}

	public Visibility getVisibility()
	{
		return this.visibility;
	}

	public boolean useNumericHeader()
	{
		return this.useNumericHeader!= null && this.useNumericHeader;
	}

	@Deprecated
	public boolean useAbsoluteXpath()
	{
		return this.useAbsoluteXpath != null && this.useAbsoluteXpath;
	}

	public Locator id(String id)
	{
		this.id = id;
		return this;
	}

	public Locator uid(String uid)
	{
		this.uid = uid;
		return this;
	}

	public Locator clazz(String clazz)
	{
		this.clazz = clazz;
		return this;
	}

	public Locator xpath(String xpath)
	{
		this.xpath = xpath;
		return this;
	}

	public Locator name(String name)
	{
		this.name = name;
		return this;
	}

	public Locator title(String title)
	{
		this.title = title;
		return this;
	}

	public Locator action(String action)
	{
		this.action = action;
		return this;
	}

	public Locator text(String text)
	{
		this.text = text;
		return this;
	}

	public Locator tooltip(String tooltip)
	{
		this.tooltip = tooltip;
		return this;
	}

	public Locator expression(String expression)
	{
		this.expression = expression;
		return this;
	}

	public Locator weak(Boolean weak)
	{
		this.weak = weak;
		return this;
	}

	public Locator visibility(Visibility visibility)
	{
		this.visibility = visibility;
		return this;
	}

	public Locator addition(Addition addition)
	{
		this.addition = addition;
		return this;
	}

	public Locator columns(String columns)
	{
		this.columns = columns;
		return this;
	}

	public Locator numericHeader(Boolean useNumericHeader)
	{
		this.useNumericHeader = useNumericHeader;
		return this;
	}

	public Locator kind(ControlKind controlKind)
	{
		this.controlKind = controlKind;
		return this;
	}

	public Locator absoluteXpath(Boolean useAbsoluteXpath)
	{
		this.useAbsoluteXpath = useAbsoluteXpath;
		return this;
	}

	public Operation operation()
	{
		return this.operation;
	}

	public boolean isDummy()
	{
		return isDummy(this.id) && isDummy(this.uid) && isDummy(this.xpath) && isDummy(this.clazz) && isDummy(this.name);
	}

	public Object get(LocatorFieldKind kind)
	{
	    if (kind == null)
	    {
	        return null;
	    }
	    
	    switch (kind)
        {
            case UID:                   return this.uid;
            case CLAZZ:                 return this.clazz;
            case XPATH:                 return this.xpath;
            case NAME:                  return this.name;
            case TITLE:                 return this.title;
            case ACTION:                return this.action;
            case TEXT:                  return this.text;
            case TOOLTIP:               return this.tooltip;
            
            case EXPRESSION:            return this.expression;
            case COLUMNS:               return this.columns;
            case WEAK:                  return this.weak;
            case ADDITION:              return this.addition;
            case VISIBILITY:            return this.visibility;
            case USE_NUMERIC_HEADER:    return this.useNumericHeader;
            case USE_ABSOLUTE_XPATH:    return this.useAbsoluteXpath;
        }

	    return null;
	}
	
	public void set(LocatorFieldKind kind, Object value)
	{
	    if (kind == null)
	    {
	        return;
	    }
	    
	    switch (kind)
        {
            case UID:                   this.uid			= (String)value; return;
            case CLAZZ:                 this.clazz			= (String)value; return;	
            case XPATH:                 this.xpath			= (String)value; return;
            case NAME:                  this.name			= (String)value; return;
            case TITLE:                 this.title			= (String)value; return;
            case ACTION:                this.action			= (String)value; return;
            case TEXT:                  this.text			= (String)value; return;
            case TOOLTIP:               this.tooltip		= (String)value; return;
            
            case EXPRESSION:            this.expression		= (String)value; return;
            case COLUMNS:               this.columns		= (String)value; return;
            case WEAK:                  this.weak			= (Boolean)value; return;
            case ADDITION:              this.addition		= (Addition)value; return;
            case VISIBILITY:            this.visibility		= (Visibility)value; return;
            case USE_NUMERIC_HEADER:    this.useNumericHeader	= (Boolean)value; return;
            case USE_ABSOLUTE_XPATH:    this.useAbsoluteXpath	= (Boolean)value; return;
        }
	}
	
	private boolean isDummy(String str)
	{
		return str != null && str.equals(IControl.DUMMY);
	}


	private static String notNull(String str)
	{
		if (str == null || str.isEmpty())
		{
			return null;
		}
		return str;
	}

	private Operation operation;
	private ControlKind controlKind; 
	private String id;

	private String uid;
	private String clazz;
	private String xpath;
	private String name;
	private String title;
	private String action;
	private String text;
	private String tooltip;
	private String expression;
	private String columns;
	private Boolean weak;
	private Addition addition;
	private Visibility visibility;
	private Boolean useNumericHeader;
	@Deprecated
	private Boolean useAbsoluteXpath;
}
