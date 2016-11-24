////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.text;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
		group					= ActionGroups.Text,
		generalDescription 		=
 "The purpose of the action is to save object {{$Text$}} to a file. "
+ "The object type {{$Text$}} is the text-based pattern which consists of lines.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True if saving is successful.",
		outputType				= Boolean.class,
		examples =
"Создание объекта {{$Text$}} посредством экшена {{@TextCreate@}}."
+ "{{##Id;#Action;#Content\n"
+"TXT1;TextCreate;'Text'#}}\n"
+ "\n"
+ "\n"
+ "Сохранение переданного объекта {{$Text$}} в файл с имненем 'file.txt' находящегося в директории path."
+ "{{##Id;#Action;#Text;#File\n"
+ "TXT2;TextSaveToFile;TXT1.Out;’path/file.txt’#}}\n"
+ "\n"
+ "\n"
+ "Проверка того, что все прошло успешно и объект {{$Text$}} сохранился."
+ "{{##Assert;#Message\n"
+ "TXT2.Out;#}}\n",
		seeAlso = "{{@TextReport@}}, {{@TextAddLine@}}, {{@TextLoadFromFile@}}, {{@TextCreate@}}, {{@TextPerform@}}," +
				" {{@TextSetValue@}}"
	)
public class TextSaveToFile extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String fileNameName = "File";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "Object {{$Text$}},which is required to output.")
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = fileNameName, mandatory = true, description = "A path where to save a file and the file name with an extension.")
	protected String 	fileName 	= null;

	public TextSaveToFile()
	{
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		return fileNameName.equals(fieldName) ? HelpKind.ChooseSaveFile : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.text.save(this.fileName));
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
