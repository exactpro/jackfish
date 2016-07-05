////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic.controls;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.ControlsAttributes;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.HTMLhelper;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Field;
import java.rmi.RemoteException;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractControl implements IControl, Mutable
{
	protected static final Logger logger = Logger.getLogger(AbstractControl.class);

	public static final String idName				= "id";
	public static final String uidName				= "uid";
	public static final String xpathName			= "xpath";
	public static final String absoluteXpathName	= "useAbsoluteXpath";
	public static final String ownerIdName			= "owner";
	public static final String clazzName			= "class";
	public static final String nameName 			= "name";
	public static final String titleName			= "title";
	public static final String actionName			= "action";
	public static final String textName				= "text";
	public static final String tooltipName			= "tooltip";
	public static final String additionName			= "addition";
	public static final String visibilityName		= "visibility";
	public static final String weakName				= "weak";
	public static final String timeoutName			= "timeout";
	public static final String expressionName		= "expression";
	public static final String rowsName 			= "rows";
	public static final String headerName 			= "header";
	public static final String useNumericHeaderName = "useNumericHeader";

	@XmlAttribute(name = idName)
	protected String id;

	@XmlAttribute(name = uidName)
	protected String uid;

	@XmlAttribute(name = xpathName)
	protected String xpath;

	@XmlAttribute(name = absoluteXpathName)
	protected Boolean absoluteXpath;

	@XmlAttribute(name = ownerIdName)
	protected String ownerId;

	@XmlAttribute(name = clazzName)
	protected String clazz;

	@XmlAttribute(name = nameName)
	protected String name;

	@XmlAttribute(name = titleName)
	protected String title;

	@XmlAttribute(name = actionName)
	protected String action;

	@XmlAttribute(name = textName)
	protected String text;

	@XmlAttribute(name = tooltipName)
	protected String tooltip;

	@XmlAttribute(name = additionName)
	protected Addition addition;

	@XmlAttribute(name = visibilityName)
	protected Visibility visibility;

	@XmlAttribute(name = weakName)
	protected Boolean weak;

	@XmlAttribute(name = timeoutName)
	protected Integer timeout;

	@XmlAttribute(name = expressionName)
	protected String expression;

	@XmlAttribute(name = rowsName)
	protected String rows;

	@XmlAttribute(name = headerName)
	protected String header;

	@XmlAttribute(name = useNumericHeaderName)
	protected Boolean useNumericHeader;


	@XmlTransient
	private Operation operationFromExpression;

    @XmlTransient
	private boolean changed;

    @XmlTransient
	private ISection section;
	
	public AbstractControl() 
	{
		this.changed = false;
		this.section = null;
	}
	
    @Override
    public String toString() 
    {
        return getBindedClass() + (id == null ? "" : " [" + id + "]");
    }

	public static AbstractControl createCopy(IControl control, IControl owner, IControl rows, IControl header) throws Exception
	{
		AbstractControl abstractControl = createCopy(control);
		if (owner != null)
		{
			abstractControl.set(ownerIdName, owner.getID());
		}
		if (rows != null)
		{
			abstractControl.set(rowsName, rows.getID());
		}
		if (header != null)
		{
			abstractControl.set(headerName, header.getID());
		}
		return abstractControl;
	}

	public static AbstractControl createCopy(IControl control) throws Exception
	{
		if (control == null)
		{
			return null;
		}
        return createCopy(control, control.getBindedClass());
	}

    public static AbstractControl createCopy(IControl control, ControlKind kind) throws Exception
    {
    	if (control == null || kind == null)
    	{
    		return null;
    	}
    	
        AbstractControl copy = create(kind);

        copy.set(idName,				control.getID() 			);
		copy.set(uidName,				control.getUID() 			);
		copy.set(xpathName,				control.getXpath() 			);
		copy.set(ownerIdName,			control.getOwnerID() 		);
		copy.set(clazzName,				control.getClazz() 			);
		copy.set(nameName,				control.getName() 			);
		copy.set(titleName,				control.getTitle() 			);
		copy.set(actionName,			control.getAction() 		);
		copy.set(textName,				control.getText() 			);
		copy.set(tooltipName,			control.getTooltip() 		);
		copy.set(additionName,			control.getAddition() 		);
		copy.set(visibilityName,		control.getVisibility()		);
		copy.set(weakName,	 			control.isWeak() 			);
		copy.set(timeoutName, 			control.getTimeout() 		);
		copy.set(expressionName,		control.getExpression()		);
		copy.set(rowsName,				control.getRowsId()			);
		copy.set(headerName,			control.getHeaderId()		);
		copy.set(useNumericHeaderName,	control.useNumericHeader()	);
		copy.set(absoluteXpathName,		control.useAbsoluteXpath()	);

        return copy;
    }

	public static AbstractControl create(ControlKind kind) throws Exception
	{
		Class<?> clazz = Class.forName(AbstractControl.class.getPackage().getName() + "." + kind.getClazz());
		return ((AbstractControl) clazz.newInstance());
	}

    public static AbstractControl create(Locator locator, String ownerId) throws Exception
    {
    	AbstractControl ret = create(locator.getControlKind());
		ret.id = locator.getId();
		ret.uid = locator.getUid();
		ret.xpath = locator.getXpath();
		ret.ownerId = ownerId;
		ret.clazz = locator.getClazz();
		ret.name = locator.getName();
		ret.title = locator.getTitle();
		ret.action = locator.getAction();
		ret.text = locator.getText();
		ret.tooltip = locator.getTooltip();
		ret.expression = locator.getExpression();
		ret.addition = locator.getAddition();
		ret.visibility = locator.getVisibility();
		ret.weak = locator.isWeak();
		ret.timeout = 0; 
		ret.useNumericHeader = locator.useNumericHeader();
		ret.rows = "";
		ret.header = "";
		ret.absoluteXpath = locator.useAbsoluteXpath();
		return ret;
    }

	public boolean changedControlKind(ControlKind kind)
	{
		if (kind == null)
		{
			return false;
		}
		return getBindedClass() != kind;
	}
	
	public void renew(Locator locator)
	{
		this.uid = locator.getUid();
		this.xpath = locator.getXpath();
		this.absoluteXpath = locator.useAbsoluteXpath();
		this.clazz = locator.getClazz();
		this.name = locator.getName();
		this.title = locator.getTitle();
		this.action = locator.getAction();
		this.text = locator.getText();
		this.tooltip = locator.getTooltip();
	}

    //------------------------------------------------------------------------------------------------------------------
    // interface Mutable
    //------------------------------------------------------------------------------------------------------------------
	@Override
    public boolean isChanged()
	{
		return this.changed;
	}
	
	@Override
	public void saved()
	{
		this.changed = false;
	}
	
    //------------------------------------------------------------------------------------------------------------------
    // interface IControl
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public final ControlKind getBindedClass()
	{
		return getClass().getAnnotation(ControlsAttributes.class).bindedClass();
	}

	@Override
	public ISection getSection()
	{
		return this.section;
	}

	@Override
	public void setSection(ISection section)
	{
		this.section = section;
	}

	@Override
	public String getXpath() 
	{
		return this.xpath;
	}

	@Override
	public String getOwnerID() 
	{
		return this.ownerId;
	}

	@Override
	public String getID() 
	{
		return this.id;
	}
	
	@Override
	public String getUID()
	{
		return this.uid;
	}

	@Override
	public String getClazz()
	{
		return this.clazz;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getTitle()
	{
		return this.title;
	}

	@Override
	public String getAction()
	{
		return this.action;
	}

	@Override
	public String getText()
	{
		return this.text;
	}

	@Override
	public String getTooltip()
	{
		return this.tooltip;
	}

	@Override
	public int getTimeout()
	{
		return this.timeout == null ? 0 : this.timeout;
	}
	
	@Override
	public String getExpression()
	{
		return this.expression;
	}

	@Override
	public Addition getAddition()
	{
		return this.addition;
	}

	@Override
	public Visibility getVisibility()
	{
		return this.visibility;
	}

	@Override
	public boolean isWeak()
	{
		return this.weak == null ? false : this.weak;
	}

	@Override
	public boolean useNumericHeader()
	{
		return this.useNumericHeader == null ? false : this.useNumericHeader;
	}

	@Override
	public String getRowsId()
	{
		return this.rows;
	}

	@Override
	public String getHeaderId()
	{
		return this.header;
	}

	@Override
	public boolean useAbsoluteXpath()
	{
		return this.absoluteXpath == null ? false : this.absoluteXpath;
	}

	@Override
	public Locator locator()
	{
		return new Locator(this);
	}
	
	@Override
	public final OperationResult operate(IRemoteApplication remote, IWindow window, Object value)  throws Exception
	{
		try
		{
			IControl ow = window.getOwnerControl(this);
			Locator owner = ow == null ? null : ow.locator();

			IControl rowsControl = window.getRowsControl(this);
			Locator rows = rowsControl == null ? null : rowsControl.locator();

			IControl headerControl = window.getHeaderControl(this);
			Locator header = headerControl == null ? null : headerControl.locator();
			Locator element = locator();

			Operation operation = null;
			if (value instanceof Operation)
			{
				operation = (Operation) value;
			}
			else if (this.operationFromExpression != null)
			{
				operation = this.operationFromExpression;
			}
			else if (element.getAddition() == Addition.Many)
			{
				operation = Operation.create().count();
			}
			else
			{
				operation = Operation.create();
				Part part = operation.addPart(getBindedClass().defaultOperation());
				prepare(part, value);
			}

			operation.tune(window);
			return remote.operate(owner, element, rows, header, operation);
		}
		catch (RemoteException re)
		{
			logger.error(re.getMessage(), re);
			//TODO
			if (re.getMessage().contains("is not allowed") || re.getMessage().contains("does not support text entering"))
			{
				OperationResult result = new OperationResult();
				result.setText(re.getMessage());
				result.setOk(false);
				return result;
			}
			throw re;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public final CheckingLayoutResult checkLayout(IRemoteApplication remote, IWindow window, Object value)  throws Exception
	{
		try
		{
			IControl ow = window.getOwnerControl(this);
			Locator owner = ow == null ? null : ow.locator();

			Locator element = locator();

			Spec spec = null;
			if (value instanceof Spec)
			{
				spec = (Spec) value;
			}
			else
			{
				spec = DoSpec.visible();
			}

			spec.tune(window);
			return remote.checkLayout(owner, element, spec);
		}
		catch (RemoteException re)
		{
			logger.error(re.getMessage(), re);
			if (re.getMessage().contains("is not allowed"))
			{
				CheckingLayoutResult result = new CheckingLayoutResult();
				return result;
			}
			throw re;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void prepare(Part operationPart, Object value)  throws Exception
	{ }

    //------------------------------------------------------------------------------------------------------------------

	public void evaluate(AbstractEvaluator evaluator)
	{
		if (!Str.IsNullOrEmpty(this.expression))
		{
			try
			{
				Object value = evaluator.evaluate(this.expression);
				if (value instanceof Operation)
				{
					this.operationFromExpression = (Operation)value;
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public void correctAllXml()
	{
		this.id = xmlToText(this.id); 
		this.uid= xmlToText(this.uid);
		this.xpath = xmlToText(this.xpath);
		this.ownerId = xmlToText(this.ownerId);
		this.clazz = xmlToText(this.clazz);
		this.name = xmlToText(this.name);
		this.title = xmlToText(this.title);
		this.action= xmlToText(this.action);
		this.text = xmlToText(this.text);
		this.tooltip = xmlToText(this.tooltip);
		this.expression = xmlToText(this.expression);
	}

	public void correctAllText()
	{
		this.id = textToXml(this.id); 
		this.uid= textToXml(this.uid);;
		this.xpath = textToXml(this.xpath);
		this.ownerId = textToXml(this.ownerId);
		this.clazz = textToXml(this.clazz);
		this.name = textToXml(this.name);
		this.title = textToXml(this.title);
		this.action= textToXml(this.action);
		this.text = textToXml(this.text);
		this.tooltip = textToXml(this.tooltip);
		this.expression = textToXml(this.expression);
		this.rows = textToXml(this.rows);
		this.header = textToXml(this.header);
		this.weak = booleanToXml(this.weak);
		this.useNumericHeader = booleanToXml(this.useNumericHeader);
		this.timeout = integerToXml(this.timeout);
		this.absoluteXpath = booleanToXml(this.absoluteXpath);
	}

	private String xmlToText(String source)
	{
		return HTMLhelper.htmlunescape(source);
	}

	private String textToXml(String source)
	{
		return HTMLhelper.htmlescape(source);
	}

	private Boolean booleanToXml(Boolean b)
	{
		if (b == null || !b)
		{
			return null;
		}
		return true;
	}

	private Integer integerToXml(Integer i)
	{
		if (i == null || i.intValue() == 0)
		{
			return null;
		}
		return i;
	}

	static void set(Class<?> clazz, Object object, String name, Object value) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields)
		{
			XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
			if (attr == null)
			{
				continue;
			}
			if (attr.name().equals(name))
			{
				field.set(object, value);
			}
		}
	}

	static Object get(Class<?> clazz, Object object, String name) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields)
		{
			XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
			if (attr == null)
			{
				continue;
			}
			if (attr.name().equals(name))
			{
				return field.get(object);
			}
		}
		return null;
	}

	public void set(String name, Object value) throws Exception
	{
		Object oldValue = get(AbstractControl.class, this, name);
		set(AbstractControl.class, this, name, value);
		Object newValue = get(AbstractControl.class, this, name);
		checkField(oldValue, newValue, timeoutName.equals(name));
	}

	public Object get(String name) throws Exception
	{
		return get(AbstractControl.class, this, name);
	}

	private <T> void checkField(T oldValue, T newValue, boolean zeroIsNull)
	{
		if (!this.changed)
		{
			if (zeroIsNull)
			{
				boolean oldValueEmpty = oldValue == null || String.valueOf(oldValue).isEmpty() || String.valueOf(oldValue).equals("0"); 
				boolean newValueEmpty = newValue == null || String.valueOf(newValue).isEmpty() || String.valueOf(newValue).equals("0"); 
				if (oldValueEmpty && newValueEmpty)
				{
					return;
				}
			}
			
			if (oldValue != null)
			{
				this.changed = !oldValue.equals(newValue);
			}
			else
			{
				this.changed = oldValue != newValue;
			}
		}
	}
}