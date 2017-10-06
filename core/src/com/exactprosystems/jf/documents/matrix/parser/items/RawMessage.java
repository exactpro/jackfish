////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.client.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@MatrixItemAttribute(
		description 	= "This operator is used to describe an object MapMessage of the type given.",
		examples 		= "{{#\n" +
							"#Id;#RawMessage;#Client\n" +
							"MSG1;none;FIX\n" +
							"$;MessageType\n" +
							"0;none\n" +
							"#EndRawMessage#}}",
		shouldContain 	= { Tokens.RawMessage, Tokens.Client }, 
		mayContain 		= { Tokens.Id, Tokens.Off, Tokens.RepOff, Tokens.Global }, 
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real 			= true, 
		hasValue 		= true, 
		hasParameters 	= false, 
		hasChildren 	= false,
		raw 			= true
	)
public class RawMessage extends MatrixItem
{
	private String[] headers = null;
	private MapMessage reading = null;
	private Map<Integer, List<MapMessage>> groups = new HashMap<>();

	private MutableValue<String> clientName;
	private MutableValue<String> typeName;
	private MapMessage message;

	public RawMessage()
	{
		super();
		this.clientName	= new MutableValue<>();
		this.typeName  	= new MutableValue<>();
		this.message 	= new MapMessage((String) null);
	}

	public RawMessage(RawMessage rm)
	{
		if (rm != null)
		{
			if (rm.headers != null)
			{
				this.headers = new String[rm.headers.length];
				System.arraycopy(rm.headers, 0, this.headers, 0, rm.headers.length);
			}
			if (this.reading != null)
			{
				this.reading = new MapMessage(this.reading);
			}
			this.groups = new HashMap<>(rm.groups);
			this.clientName = new MutableValue<>(rm.clientName);
			this.typeName = new MutableValue<>(rm.typeName);
			this.message = new MapMessage(rm.message);
		}
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new RawMessage(this);
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 3);
		
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.RawMessage.get(), context.getFactory().getSettings());
		driver.showLabel(this, layout, 1, 2, Tokens.Client.get());
		driver.showComboBox(this, layout, 1, 3, this.clientName, this.clientName, () -> context.getConfiguration().getClientPool().clientNames(), (str) -> true);
		driver.showLabel(this, layout, 1, 4, "Message type");
		driver.showComboBox(this, layout, 1, 5, str ->
		{
			this.typeName.set(str);
			this.message.setMessageType(str);
			IMessageDictionary dictionary = getDictionary(context);
			updateMessage(str, dictionary);
			driver.updateTree(this, layout, this.message, dictionary);
		}
		, this.typeName, () ->
		{
			if (this.clientName == null)
			{
				return new ArrayList<>();
			}
			try
			{
				IMessageDictionary dictionary = getDictionary(context);
				if (dictionary == null)
				{
					return new ArrayList<>();
				}
				return dictionary.getMessages().stream().map(IField::getName).collect(Collectors.toList());
			}
			catch (Exception e)
			{
				;
			}
			return new ArrayList<>();
		}
		, str -> {
				//TODO
//				return	DialogsHelper.showQuestionDialog("Do you want to change message type?", "The message will cleared");
				return	true;
		});
		driver.showCheckBox(this, layout, 1, 6, "Global", this.global, this.global);
		driver.showToggleButton(this, layout, 1, 7, 
		        b -> driver.hide(this, layout, 2, b), 
		        b -> b ? "Hide" : "Show", this.message.size() != 0);
		driver.showTree(this, layout, 2, 0, this.message, getDictionary(context), context);
		driver.hide(this, layout, 2, this.message.size() == 0);
		return layout;
	}

	// ==============================================================================================
	// Getters / setters
	// ==============================================================================================
	public String getType()
	{
		return this.typeName.get();
	}

	public String getClient()
	{
		return this.clientName.get();
	}

	// ==============================================================================================
	// Interface Mutable
	// ==============================================================================================
	@Override
	public boolean isChanged()
	{
		if (this.clientName.isChanged() || this.typeName.isChanged() || this.message.isChanged())
		{
			return true;
		}
		return super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.clientName.saved();
		this.typeName.saved();
		this.message.saved();
	}

	// ==============================================================================================
	// Protected members should be overridden
	// ==============================================================================================
	@Override
	public void processRawData(String[] str)
	{
		addValue(str);
	}
	
	@Override
	public String getItemName()
	{
		return super.getItemName() + (this.clientName.toString().equals("") ? "" : " Client:" + this.clientName)
				+ (this.typeName.toString().equals("") ? "" : " Message type:" + this.typeName);
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.typeName.set(systemParameters.get(Tokens.RawMessage));
		this.clientName.set(systemParameters.get(Tokens.Client));
		Map<String, Object> map = new LinkedHashMap<>();
		this.message = new MapMessage(this.typeName.get(), map, null);


//		this.message.put("Fld1", 1);
//		this.message.put("Fld2", 2);
//
//		MapMessage group1 = new MapMessage(null);
//		group1.put("Field1", 101);
//		group1.put("Field2", 102);
//
//		MapMessage subGroup1 = new MapMessage(null);
//		subGroup1.put("SubField1", "66");
//		subGroup1.put("SubField2", 302);
//
//		MapMessage subGroup2 = new MapMessage(null);
//		subGroup2.put("SSubField1", 401);
//		subGroup2.put("SSubField2", 402);
//
//		MapMessage group2 = new MapMessage(null);
//		group2.put("FieldGroup1", 201);
//		group2.put("FieldGroup2", 202);
//		group2.put("SubGroup1", new Map[] { subGroup1 });
//		group2.put("SubGroup2", new Map[] { subGroup2 });
//
//		MapMessage group3 = new MapMessage(null);
//		group3.put("group3f1", "f1");
//		group3.put("group3f2", "f2");
//
//		group2.put("Simple Group", group3);
//
//		this.message.put("Group1", new Map[] { group1, group2 } );
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "MSG";
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.RawMessage.get(), this.typeName.get());
		addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Client.get(), this.clientName.get());
	}
	
	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		save(writer, this.message, indent, new AtomicInteger(0));
		super.addParameter(line, TypeMandatory.System, Tokens.EndRawMessage.get());
	}
	

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive,
			boolean wholeWord)
	{
		return SearchHelper.matches(this.typeName.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.clientName.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator,
			ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReportTable table = report.addTable("Message", null, true, true, new int[] { });
			table.addValues(Str.asString(this.message));
			Variables vars = isGlobal() ? evaluator.getGlobals() : evaluator.getLocals();
			ReturnAndResult ret = new ReturnAndResult(start, Result.Passed, new MapMessage(this.message.getSource(), this.message));

			if (super.getId() != null && !super.getId().isEmpty())
			{
				// set variable into local name space
				vars.set(super.getId(), ret.getOut());
			}

			return ret;
		} 
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}


	// ==============================================================================================
	// Private members
	// ==============================================================================================

	private void save(CsvWriter writer, MapMessage message, String indent, AtomicInteger number)
	{
		try
		{
			int thisNumber = number.get();
			
			for (Object group : message.values())
			{
				if(group.getClass().isArray())
				{
					number.incrementAndGet();
					for (Object leg : (Object[])group)
					{
						if (leg instanceof MapMessage)
						{
							save(writer, (MapMessage)leg, indent, number);
						}
					}
				}
			}

			String messageType = message.getMessageType();
			
			String[] record = new String[message.size() + (messageType == null ? 1 : 2)];
			int count = 0;
			record[count++] = indent + "$";
			if (messageType != null)
			{
				record[count++] = "MessageType";
			}
			for (String key : message.keySet())
			{
				record[count++] = key;
			}
			writer.writeRecord(record, true);

			count = 0;
			record[count++] = indent + thisNumber;
			if (messageType != null)
			{
				record[count++] = messageType;
			}
			
			int groupNumber = thisNumber;
			for (Object value : message.values())
			{
				if (value.getClass().isArray())
				{
					record[count++] = "$" + ++groupNumber;
				}
				else
				{
					record[count++] = ("" + value).replace("$", "\\$");
				}
			}
			writer.writeRecord(record, true);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	private void addValue(String[] str)
	{
		if (str.length > 0)
		{
			if (str[0].startsWith("$"))
			{
				this.headers = str;
			}
			else
			{
				if (this.headers != null && str.length > 0)
				{
					this.reading = new MapMessage((String)null);
					for (int i = 1; i < Math.min(this.headers.length, str.length); i++)
					{
						String value = "" + str[i];
						if (value.startsWith("$"))
						{
							Integer key = Integer.parseInt(value.substring(1));
							this.reading.put(this.headers[i], this.groups.get(key).toArray());
						}
						else
						{
							this.reading.put(this.headers[i], value.replace("\\$", "$"));
						}
					}
					Integer key = Integer.parseInt(str[0]);
					if (!this.groups.containsKey(key))
					{
						this.groups.put(key, new ArrayList<MapMessage>());
					}
					this.groups.get(key).add(this.reading);
					if (key == 0)
					{
					    String type = this.message.getMessageType();
						this.message = this.reading;
						this.message.setMessageType(type);
						this.reading = null;
					}
				}
			}
		}
	}

	private IMessageDictionary getDictionary(Context context)
	{
		try
		{
			IClientFactory  factory = context.getConfiguration().getClientPool().loadClientFactory(this.clientName.get());
			if (factory == null)
			{
				DialogsHelper.showError("Can't load factory " + this.clientName.get());
				return null;
			}
			return factory.getDictionary();
		}
		catch (Exception e)
		{
			;
		}
		return null;
	}

	private void updateMessage(String messageType, IMessageDictionary dictionary)
	{
		this.message.clear();
		IMessage message = dictionary.getMessageByName(messageType);
		extractFields(this.message, message.getMessageField());
	}

	private void extractFields(MapMessage mapMessage, List<IField> fields)
	{
		fields.stream()
				.filter(IField::isRequired)
				.forEach(field -> {
					String name = field.getName();
					IField reference = field.getReference();
					if (reference instanceof IMessage)
					{
						mapMessage.put(name, extractMapMessage((IMessage) reference));
					}
					else
					{
						mapMessage.put(name, field.getDefaultvalue() == null ? "" : field.getDefaultvalue());
					}
				});
	}

	private MapMessage extractMapMessage(IMessage reference)
	{
		MapMessage mapMessage = new MapMessage((String)null);
		extractFields(mapMessage, reference.getMessageField());
		return mapMessage;
	}
}
