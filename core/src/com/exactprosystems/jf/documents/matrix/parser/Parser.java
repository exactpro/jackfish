////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.parser.items.*;

import org.apache.log4j.Logger;

import javax.lang.model.type.NullType;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class Parser
{
	// other syntax details
	public static final String	error					= "error";
	public static final String	err						= "err";
	public static final String	commentPrefix			= "//";
	public static final String	systemPrefix			= "#";
    public static final String  knownPrefix             = "$";
	public static final char	prefferedQuotes			= '"';

	public Parser()
	{}
	
	public static Class<?> [] knownItems = new Class<?>[]
			{	
				TempItem.class,
				NameSpace.class,
				EndNameSpace.class,
				TestCase.class,
				Step.class,
				EndStep.class,
				ActionItem.class,
				If.class,
				Else.class,
				EndIf.class,
				For.class,
				EndFor.class,
				ForEach.class,
				EndForEach.class,
				Switch.class,
				Case.class,
				Default.class,
				EndSwitch.class,
				While.class,
				EndWhile.class,
				Break.class,
				Continue.class,
				Return.class,
				SubCase.class,
				EndSubCase.class,
				Call.class,
				OnError.class,
				Fail.class,
				RawTable.class,
				EndRawTable.class,
				RawMessage.class,
				EndRawMessage.class,
				RawText.class,
				EndRawText.class,
				Let.class,
				Assert.class,
				SetHandler.class,
			};

	public static void main(String[] args)
	{
		System.out.printf("%-15s %c %c %c %c %-15s%n", "Name", 'R', 'C', 'P', 'V', "Closes");
		System.out.println("======================================");
		Arrays.stream(knownItems)
			.sorted((f, s) -> f.getSimpleName().compareTo(s.getSimpleName()))
			.forEach(clazz ->
				{
					MatrixItemAttribute attr = clazz.getAnnotation(MatrixItemAttribute.class);
					System.out.printf("%-15s %c %c %c %c %-15s %n", clazz.getSimpleName(), 
							(attr.real() ? 'T' : ' '), 
							(attr.hasChildren() ? 'T' : ' '),
							(attr.hasParameters() ? 'T' : ' '),
							(attr.hasValue() ? 'T' : ' '),
							(attr.closes() == NullType.class ? "" : attr.closes().getSimpleName()));
				});
	}

    public MatrixItem readMatrix(MatrixItem root, Reader rawReader) throws MatrixException
    {
        CsvReader reader = new CsvReader(rawReader);
        reader.setSkipEmptyRecords(false);
        reader.setDelimiter(Configuration.matrixDelimiter);

        MatrixItem currentItem = root;

        int count = 0;

        List<String> comments = new ArrayList<String>();
        String[] headers = null;
        ItemTypeAndAttribute itemAttr = null;
        String[] str;
        boolean needFillValue = false;

        try
        {
            while (reader.readRecord())
            {
                count++;
                str = reader.getValues();
                
                if (Arrays.stream(str).allMatch(String::isEmpty) && !needFillValue)
                {
                    // nothing to do
                    continue;
                }

                if (str[0].startsWith(commentPrefix))
                {
                    // it is a comment
                    comments.add(str[0].substring(commentPrefix.length()).trim());
                    continue;
                }

                if (str[0].startsWith(systemPrefix))
                {
                    // it is a header
                    headers = str;

                    // determinate - what is it
                    itemAttr = lookUp(count, headers);

                    if (needFillValue)
                    {
                        throw new MatrixException(count, null,
                                String.format(R.PARSER_VALUE_EXCEPTION.get(), root.getItemName()));
                    }
                    else
                    {
                        needFillValue = itemAttr.attribute.hasValue();
                    }

                    if (!needFillValue)
                    {
                        // no wait other line
                        currentItem = addNewMatrixItem(currentItem, itemAttr, headers, null, comments);
                        comments.clear();

                        itemAttr = null;
                    }
                    continue;
                }

                if (isRaw(currentItem))
                {
                    currentItem.processRawData(str);
                }
                else
                {
                    if (itemAttr == null)
                    {
                        throw new MatrixException(count, null,
                                String.format(R.PARSER_HEADER_EXCEPTION.get(), root.getItemName()));
                    }

                    needFillValue = false;
                    currentItem = addNewMatrixItem(currentItem, itemAttr, headers, str, comments);
                    comments.clear();
                }
            }
        }
        catch (MatrixException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new MatrixException(count, currentItem, e);
        }

        root.bypass(MatrixItem::correctParametersType);
        root.saved();
        return root;
    }

    private MatrixItem addNewMatrixItem(MatrixItem currentItem, ItemTypeAndAttribute itemAttr, String[] headers,
            String[] str, List<String> comments) throws Exception
    {
        // unreal items have to close something and return parent of it
        if (!itemAttr.attribute.real())
        {
            currentItem = currentItem.findParent(itemAttr.attribute.closes());
            currentItem = currentItem.getParent() == null ? currentItem : currentItem.getParent();

            return currentItem;
        }

        // if real items close then they make current it before
        if (itemAttr.attribute.closes() != NullType.class)
        {
            currentItem = currentItem.findParent(itemAttr.attribute.closes());
        }

        Map<Tokens, String> systemParameters = new LinkedHashMap<Tokens, String>();
        Parameters userParameters = new Parameters();

        if (str == null)
        {
            for (String header : headers)
            {
                if (header.startsWith(commentPrefix))
                {
                    break;
                }
                if (header.startsWith(systemPrefix))
                {
                    String token = header.substring(systemPrefix.length());
                    if (Tokens.contains(token))
                    {
                        systemParameters.put(Tokens.valueOf(token), null);
                    }
                }
            }
        }
        else
        {
            for (int column = 0; column < Math.max(headers.length, str.length); column++)
            {
                String value = str.length > column ? str[column] : null;
                String header = headers.length > column ? headers[column] : null;

                if (value != null && value.startsWith(commentPrefix))
                {
                    break;
                }
                if (header != null)
                {
                    if(header.startsWith(commentPrefix))
                    {
                        break;
                    }

                    if (value != null)
                    {
                        value = value.replaceAll(Configuration.unicodeDelimiter, String.valueOf(Configuration.matrixDelimiter));
                    }
                    if (header.startsWith(systemPrefix))
                    {
                        String token = header.substring(systemPrefix.length());
                        if (Tokens.contains(token))
                        {
                            systemParameters.put(Tokens.valueOf(token), value);
                        }
                        else
                        {
                            userParameters.add(token, value, TypeMandatory.Extra);
                        }
                    }
                    else if (header.startsWith(knownPrefix))
                    {
                        String parName = header.substring(knownPrefix.length());
                        userParameters.add(parName, value, TypeMandatory.Mandatory);
                    }
                    else
                    {
                        userParameters.add(header, value, TypeMandatory.Extra);
                    }
                }
            }
        }

        MatrixItem newItem = (MatrixItem) itemAttr.itemType.newInstance();
        newItem.init(null, comments, systemParameters, userParameters);
        currentItem.insert(currentItem.count(), newItem);

        if (itemAttr.attribute.hasChildren() || itemAttr.attribute.raw())
        {
            return newItem;
        }

        return currentItem;
    }

	private boolean isRaw(MatrixItem item)
	{
		if (item == null)
		{
			return false;
		}
		
		MatrixItemAttribute attribute = item.getClass().getAnnotation(MatrixItemAttribute.class);
		if (attribute == null)
		{
			return false;
		}
		
		return attribute.raw();
	}
	
	public void saveMatrix(MatrixItem root, Writer rawWriter) throws Exception
	{
		CsvWriter writer = prepareCsvWriter(rawWriter);
		
		for(int index = 0; index < root.count(); index++)
		{
			write(root.get(index), writer);
		}
	}
	
	public String itemsToString(MatrixItem ... items) throws Exception
	{
		try (StringWriter stringWriter = new StringWriter())
		{
			CsvWriter writer = prepareCsvWriter(stringWriter);
			for (MatrixItem item : items)
			{
				write(item, writer);
				writer.endRecord();
			}
			return stringWriter.getBuffer().toString();
		}
	}

	public MatrixItem[] stringToItems(String string) throws MatrixException, Exception
	{
		Reader reader = CommonHelper.readerFromString(string);
		MatrixItem root = new MatrixRoot("root");
		
		root = readMatrix(root, reader);

		if (root != null)
		{
			MatrixItem[] res = new MatrixItem[root.count()];
			for(int index = 0; index < root.count(); index++)
			{
				res[index] = root.get(index);
			}
			return res;
		}
		
		return null;
	}

	private void write(MatrixItem root, CsvWriter writer) throws Exception
	{
		root.write(0, writer);
		writer.flush();
	}

	private CsvWriter prepareCsvWriter(Writer rawWriter)
	{
		CsvWriter writer = new CsvWriter(rawWriter, Configuration.matrixDelimiter);
		writer.setDelimiter(Configuration.matrixDelimiter);
		writer.setForceQualifier(false);
		writer.setUseTextQualifier(false);
		writer.setTextQualifier(prefferedQuotes);
		return writer;
	}

	
	
	
	public static MatrixItem createItem(String type, String value) throws Exception
	{
		Optional<Class<?>> res = Arrays.stream(knownItems).filter(clazz -> clazz.getSimpleName().equalsIgnoreCase(type)).findFirst();

		Map<Tokens, String> systemParameters = new HashMap<>();
		systemParameters.put(Tokens.valueOf(type), value);
				
		if (res.isPresent()) 
		{
			MatrixItem item =  (MatrixItem)res.get().newInstance();
			item.addKnownParameters();
			item.init(null, null, systemParameters, null);
			return item;
		}
		else
		{
			if (type.equals(Tokens.Action.get()))
			{
				MatrixItem item = ActionItem.class.newInstance();
				item.init(null, null, systemParameters, null);
				item.addKnownParameters();
				return item;
			}
		}
		
		throw new Exception(String.format(R.PARSER_UNKNOWN_TYPE_EXCEPTION.get(), type));
	}
	

	private class ItemTypeAndAttribute
	{
		public ItemTypeAndAttribute(MatrixItemAttribute attribute, Class<?> itemType)
		{
			this.attribute = attribute;
			this.itemType = itemType;
		}
		public MatrixItemAttribute attribute;
		public Class<?> itemType;
	}
	
	private ItemTypeAndAttribute lookUp(int lineNumber, String[] headers) throws MatrixException
	{
		List<Tokens> tokens = new ArrayList<Tokens>();

		for (String header : headers)
		{
			if (header.startsWith(commentPrefix))
			{
				break; 
			}

			if (header.startsWith(systemPrefix) && Tokens.contains(header.substring(systemPrefix.length())))
			{
				tokens.add(Tokens.valueOf(header.substring(systemPrefix.length())));
			}
		}
		
		List<ItemTypeAndAttribute> suitable = new ArrayList<ItemTypeAndAttribute>();

		for (Class<?> itemType :  knownItems)
		{
			List<Tokens> tokensCopy = new ArrayList<Tokens>();
            tokensCopy.addAll(tokens);
			MatrixItemAttribute annotation = itemType.getAnnotation(MatrixItemAttribute.class);
			tokensCopy.removeAll(Arrays.asList(annotation.mayContain()));
			
			if (tokensCopy.containsAll(Arrays.asList(annotation.shouldContain())))
			{
				tokensCopy.removeAll(Arrays.asList(annotation.shouldContain()));
				if (annotation.hasParameters() || (!annotation.hasParameters() && tokensCopy.size() == 0))
				{
					suitable.add(new ItemTypeAndAttribute(annotation, itemType));
				}
			}
		}

		if (suitable.size() == 0)
		{
			throw new MatrixException(lineNumber, null, String.format(R.PARSER_UNKNOWN_SYNTAX_EXCEPTION.get(), Arrays.toString(tokens.toArray())));
		}
		
		if (suitable.size() > 1)
		{
			String s = suitable.stream().map(i -> i.itemType.getSimpleName()).reduce(" ", String::concat);
			throw new MatrixException(lineNumber, null, String.format(R.PARSER_TOO_MANY_SYNTAX_EXCEPTION.get(), s));
		}

		return suitable.get(0);
	}

	private static final Logger logger = Logger.getLogger(Parser.class);
}
