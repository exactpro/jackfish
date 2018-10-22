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

package com.exactprosystems.jf.documents.guidic.controls;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.OperationNotAllowedException;
import com.exactprosystems.jf.common.ControlsAttributes;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.HTMLhelper;
import com.exactprosystems.jf.documents.guidic.ExtraInfo;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Field;
import java.rmi.ServerException;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractControl implements IControl
{
	protected static final Logger logger = Logger.getLogger(AbstractControl.class);

	public static final String idName               = "id";
	public static final String uidName              = "uid";
	public static final String ownerIdName          = "owner";
	public static final String refIdName            = "ref";
	public static final String xpathName            = "xpath";
	public static final String clazzName            = "class";
	public static final String nameName             = "name";
	public static final String titleName            = "title";
	public static final String actionName           = "action";
	public static final String textName             = "text";
	public static final String tooltipName          = "tooltip";
	public static final String additionName         = "addition";
	public static final String visibilityName       = "visibility";
	public static final String weakName             = "weak";
	public static final String timeoutName          = "timeout";
	public static final String expressionName       = "expression";
	public static final String rowsName             = "rows";
	public static final String headerName           = "header";
	public static final String columnsName          = "columns";
	public static final String useNumericHeaderName = "useNumericHeader";
	public static final String infoName             = "info";

	@XmlAttribute(name = idName)
	protected String id;

	@XmlAttribute(name = uidName)
	protected String uid;

	@XmlAttribute(name = xpathName)
	protected String xpath;

	@XmlAttribute(name = ownerIdName)
	protected String ownerId;

	@XmlAttribute(name = refIdName)
	protected String refId;

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

	@XmlAttribute(name = columnsName)
	protected String columns;

	@XmlAttribute(name = useNumericHeaderName)
	protected Boolean useNumericHeader;

	@XmlElement(name = infoName)
	protected ExtraInfo info;

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
		this.info = null;
	}
	
    @Override
    public String toString() 
    {
        return getBindedClass() + (this.id == null ? "" : " [" + this.id + "]");
    }

	//region static creating methods
	/**
	 * Create the copy of passed control. If the passed control is null, will return null.
	 * And into the copy control will set owner,rows and header link, if they not null
	 *
	 * @see IControl
	 */
	public static AbstractControl createCopy(IControl control, IControl owner, IControl rows, IControl header) throws Exception
	{
		AbstractControl abstractControl = createCopy(control);
		if (abstractControl == null)
		{
			return null;
		}
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

	/**
	 * @param control from which will create copy.
	 * @return a copy of the passed control. If a passed control is null, will return null
	 */
	public static AbstractControl createCopy(IControl control) throws Exception
	{
		if (control == null)
		{
			return null;
		}
        return createCopy(control, control.getBindedClass());
	}

	/**
	 * Create copy of control by passed controlKind and control ( from control will get all fields)
	 * @param control a base control for copying
	 * @param kind a kind of copied control
	 * @return a copy of AbstractControl, based on passed control and kind
	 */
	public static AbstractControl createCopy(IControl control, ControlKind kind) throws Exception
	{
		if (control == null || kind == null)
		{
			return null;
		}

		AbstractControl copy = create(kind);

		copy.set(idName, control.getID());
		copy.set(uidName, control.getUID());
		copy.set(xpathName, control.getXpath());
		copy.set(ownerIdName, control.getOwnerID());
		copy.set(clazzName, control.getClazz());
		copy.set(nameName, control.getName());
		copy.set(titleName, control.getTitle());
		copy.set(actionName, control.getAction());
		copy.set(textName, control.getText());
		copy.set(tooltipName, control.getTooltip());
		copy.set(additionName, control.getAddition());
		copy.set(visibilityName, control.getVisibility());
		copy.set(weakName, control.isWeak());
		copy.set(timeoutName, control.getTimeout());
		copy.set(expressionName, control.getExpression());
		copy.set(rowsName, control.getRowsId());
		copy.set(headerName, control.getHeaderId());
		copy.set(columnsName, control.getColumns());
		copy.set(useNumericHeaderName, control.useNumericHeader());
		copy.set(infoName, control.getInfo());
		copy.set(refIdName, control.getRefID());

		return copy;
	}

	/**
	 * Create a AbstractControl based on ControlKind.
	 * Creating will via {@link Class#forName(String)}
	 * @param kind a type of created ControlKind
	 * @return a AbstractControl based on ControlKind
	 * @throws Exception if something went wrong
	 *
	 * @see ControlKind
	 */
	public static AbstractControl create(ControlKind kind) throws Exception
	{
		Class<?> clazz = Class.forName(AbstractControl.class.getPackage().getName() + "." + kind.getClazz());
		return ((AbstractControl) clazz.newInstance());
	}

	/**
	 * Create new AbstractControl from passed locator and ownerId
	 *
	 * @param locator a locator, which used for creating new AbstractControl
	 * @param ownerId a ownerId, which will passed into new AbstractControl
	 * @return a new AbstractControl from passed parameters
	 *
	 * @throws Exception if something went wrong
	 *
	 * @see Locator
	 */
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
		ret.columns = "";
		return ret;
	}

	/**
	 * Create the dummy locator.
	 * @throws Exception if something went wrong
	 */
	public static AbstractControl createDummy() throws Exception
	{
		AbstractControl ret = create(ControlKind.Any);
		ret.id = IControl.DUMMY;
		ret.uid = IControl.DUMMY;
		ret.xpath = IControl.DUMMY;
		ret.clazz = IControl.DUMMY;
		ret.name = IControl.DUMMY;
		return ret;
	}
	//endregion

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
		this.clazz = locator.getClazz();
		this.name = locator.getName();
		this.title = locator.getTitle();
		this.action = locator.getAction();
		this.text = locator.getText();
		this.tooltip = locator.getTooltip();
	}

	//region  interface Mutable
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
	//endregion

	//region interface IControl
	@Override
	public final ControlKind getBindedClass()
	{
		return this.getClass().getAnnotation(ControlsAttributes.class).bindedClass();
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
	public String getRefID()
	{
		return this.refId;
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
	public IExtraInfo getInfo()
	{
		return this.info;
	}

	@Override
	public String getColumns()
	{
		return this.columns;
	}

	@Override
	public Locator locator()
	{
		Locator res = new Locator(this);
		if (this.section != null && this.refId != null)
		{
			IControl refControl = this.section.getWindow().getControlForName(null, this.refId);
			if (refControl != null)
			{
				res.kind(refControl.getBindedClass());
				res.uid(refControl.getUID());
				res.clazz(refControl.getClazz());
				res.xpath(refControl.getXpath());
				res.name(refControl.getName());
				res.title(refControl.getTitle());
				res.action(refControl.getAction());
				res.text(refControl.getText());
				res.tooltip(refControl.getTooltip());
			}
		}
		return res;
	}

	/**
	 * Create the operation from the passed object value, execute it and return the instance of {@link OperationResult} <br>
	 * If passed object value is {@code null}, will getting the {@link AbstractControl#operationFromExpression}.<br>
	 * If the operationFromExpression will {@code null}, will create a new Operation : <br>
	 * <ul>
	 *    <li>if the AbstractControl has addition {@link Addition#Many}, will create {@link Operation#count()} operation<br></li>
	 *    <li>Otherwise will create {@link Operation#create()} empty operation and invoke method {@link IControl#prepare(Part, Object)}</li>
	 * </ul>
	 * @param remote the remote object, which will execute the created operation
	 * @param window a window, which used as owner for the AbstractControl
	 * @param value a operation value
	 * @param templateEvaluator the evaluator, for compile templates, which can be on description of the element
	 * @return OperationResult, which contains result of executing the created operation
	 * @throws Exception if something went wrong
	 *
	 * @see IRemoteApplication
	 * @see Operation
	 * @see OperationResult
	 * @see IControl
	 * @see IControl#prepare(Part, Object)
	 */
	@Override
	public final OperationResult operate(IRemoteApplication remote, IWindow window, Object value, ITemplateEvaluator templateEvaluator) throws Exception
	{
		try
		{
			IControl ownerControl = window.getOwnerControl(this);
			Locator ownerLocator = ownerControl == null ? null : ownerControl.locator();

			IControl rowsControl = window.getRowsControl(this);
			Locator rowsLocator = rowsControl == null ? null : rowsControl.locator();

			IControl headerControl = window.getHeaderControl(this);
			Locator headerLocator = headerControl == null ? null : headerControl.locator();

			Locator element = this.locator();

			Operation operation;
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
				Part part = operation.addPart(this.getBindedClass().defaultOperation());
				this.prepare(part, value);
			}

			operation.tune(window, templateEvaluator);

			IControl.evaluateTemplate(ownerLocator, templateEvaluator);
			IControl.evaluateTemplate(element, templateEvaluator);
			IControl.evaluateTemplate(rowsLocator, templateEvaluator);
			IControl.evaluateTemplate(headerLocator, templateEvaluator);

			return remote.operate(ownerLocator, element, rowsLocator, headerLocator, operation);
		}
		//TODO think about catch ServerException
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Create the spec object from the passed object value, execute it and return the instance of {@link CheckingLayoutResult} <br>
	 * If the passed object is {@code null}, will create a new {@link DoSpec#visible()}
	 * @param remote the remote object, which will execute the created spec
	 * @param window a window, which used as owner for the AbstractControl
	 * @param value a spec value
	 * @param templateEvaluator the evaluator, for compile templates, which can be on description of the element
	 * @return CheckingLayoutResult, which contains result of executing the created spec object
	 * @throws Exception if something went wrong
	 *
	 * @see IRemoteApplication
	 * @see Spec
	 * @see CheckingLayoutResult
	 * @see IControl
	 */
	@Override
	public final CheckingLayoutResult checkLayout(IRemoteApplication remote, IWindow window, Object value, ITemplateEvaluator templateEvaluator) throws Exception
	{
		try
		{
			IControl ow = window.getOwnerControl(this);
			Locator owner = ow == null ? null : ow.locator();

			Locator element = locator();

			Spec spec;
			if (value instanceof Spec)
			{
				spec = (Spec) value;
			}
			else
			{
				spec = DoSpec.visible();
			}
			spec.tune(window, templateEvaluator);

			IControl.evaluateTemplate(owner, templateEvaluator);
			IControl.evaluateTemplate(element, templateEvaluator);

			return remote.checkLayout(owner, element, spec);
		}
		catch (ServerException se)
		{
			logger.error(se.getMessage(), se);
			if (se.getCause() instanceof OperationNotAllowedException)
			{
				return new CheckingLayoutResult();
			}
			if (se.getCause() != null)
			{
				throw new Exception(se.getCause().getMessage(), se.getCause());
			}
			throw se;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Prepare the passed operation for the any instance of AbstractControl. See override methods
	 * @param operationPart a part of the operation
	 * @param value a value, which should be prepared
	 * @throws Exception if something went wrong
	 */
	@Override
	public void prepare(Part operationPart, Object value) throws Exception
	{
	}

	//endregion

	/**
	 * Try to evaluate the {@link AbstractControl#expression}.
	 * If a evaluated value instanceof {@link Operation}, the value {@link AbstractControl#operationFromExpression} will filled
	 * @param evaluator a evaluator, which used for evaluating
	 *
	 * @see AbstractEvaluator
	 */
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
		if (this.info != null)
		{
			this.info.correctAllXml();
		}

		this.id = this.xmlToText(this.id);
		this.uid= this.xmlToText(this.uid);
		this.xpath = this.xmlToText(this.xpath);
		this.ownerId = this.xmlToText(this.ownerId);
		this.refId = this.xmlToText(this.refId);
		this.clazz = this.xmlToText(this.clazz);
		this.name = this.xmlToText(this.name);
		this.title = this.xmlToText(this.title);
		this.action= this.xmlToText(this.action);
		this.text = this.xmlToText(this.text);
		this.tooltip = this.xmlToText(this.tooltip);
		this.expression = this.xmlToText(this.expression);
		this.columns = this.xmlToText(this.columns);
		
		if (!Str.IsNullOrEmpty(this.ownerId) && !Str.IsNullOrEmpty(this.xpath) && this.xpath.startsWith("//"))
		{
			this.xpath = "." + this.xpath;
		}
	}

	public void correctAllText()
	{
		if (this.info != null)
		{
			this.info.correctAllText();
		}
		
		this.id = this.textToXml(this.id);
		this.uid= this.textToXml(this.uid);
		this.xpath = this.textToXml(this.xpath);
		this.ownerId = this.textToXml(this.ownerId);
		this.refId = this.textToXml(this.refId);
		this.clazz = this.textToXml(this.clazz);
		this.name = this.textToXml(this.name);
		this.title = this.textToXml(this.title);
		this.action= this.textToXml(this.action);
		this.text = this.textToXml(this.text);
		this.tooltip = this.textToXml(this.tooltip);
		this.expression = this.textToXml(this.expression);
		this.rows = this.textToXml(this.rows);
		this.header = this.textToXml(this.header);
		this.columns = this.textToXml(this.columns);
		this.weak = this.booleanToXml(this.weak);
		this.useNumericHeader = this.booleanToXml(this.useNumericHeader);
		this.timeout = this.integerToXml(this.timeout);
	}

	/**
	 * Set value via reflection
	 */
	static void set(Class<?> clazz, Object object, String name, Object value) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields)
		{
			XmlElement elem = field.getAnnotation(XmlElement.class);
			if (elem != null && elem.name().equals(name))
			{
				field.set(object, value);
				continue;
			}

			XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
			if (attr != null && attr.name().equals(name))
			{
				field.set(object, value);
				continue;
			}
		}
	}

	/**
	 * Get value via reflection
	 */
	static Object get(Class<?> clazz, Object object, String name) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields)
		{
            XmlElement elem = field.getAnnotation(XmlElement.class);
            if (elem != null && elem.name().equals(name))
            {
                return field.get(object);
            }

            XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
			if (attr != null && attr.name().equals(name))
			{
				return field.get(object);
			}
		}
		return null;
	}

	/**
	 * Set the field with passed name passed value
	 * @param name of field
	 * @param value the value, which need set to the field
	 * @throws Exception if field by passed name not found
	 */
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

	//region private methods
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
		if (i == null || i == 0)
		{
			return null;
		}
		return i;
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
	//endregion
}
