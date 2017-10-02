////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.File;


@ActionAttribute(
		group					= ActionGroups.GUI,
		suffix					= "IMGSV",
		generalDescription 		= "The purpose of this action is to save the image.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Full path to file", 
		outputType 				= String.class,
		examples 				= "{{`1. Connect to web application`}}"
				+ "{{`2. Get the images of the whole application.`}}"
				+ "{{`3. Save the file to the user's home directory.`}}"
				+ "{{#\n" +
				"#Id;#Action;#Browser;#URL;#AppId\n"
				+ "APPSTR1;ApplicationStart;'Chrome';'https://google.com';'WEB'\n"
				+ "#Id;#Action;#AppConnection\n"
				+ "IMGGET1;ImageGet;APPSTR1.Out\n"
				+ "#Id;#Action;#Dir;#Image\n"
				+ "IMGSV1;ImageSave;currentUser/home;IMGGET1.Out#}}"
	)
public class ImageSave extends AbstractAction
{
	public final static String	imageName	= "Image";
	public final static String	dirName		= "Dir";
	public final static String	fileName	= "File";
	
	@ActionFieldAttribute(name = imageName, mandatory = true, description = "The object of the Image type which needs to be saved.")
	protected ImageWrapper		image;

	@ActionFieldAttribute(name = dirName, mandatory = false, def = DefaultValuePool.Null, description = "Directory where the file should be saved.  In this case the filename will be generated.")
	protected String			dir;

	@ActionFieldAttribute(name = fileName, mandatory = false, def = DefaultValuePool.Null, description = "The path to the file where the image will be saved.")
	protected String			file;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		switch(fieldName)
		{
			case dirName:
				return HelpKind.ChooseFolder;
				
			case fileName:
				return HelpKind.ChooseSaveFile;
		}
		
		return null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		if (this.file != null)
		{
			this.image.saveToFile(this.file);
	        super.setResult(this.file);
		}
		else if (this.dir != null)
		{
		    File dir = new File(this.dir);
		    if (!dir.exists())
		    {
		        super.setError("Directory '" + this.dir + "' does not exist", ErrorKind.WRONG_PARAMETERS);
		        return;
		    }
		    if (!dir.isDirectory())
		    {
                super.setError("'" + this.dir + "' is not a directory", ErrorKind.WRONG_PARAMETERS);
                return;
		    }
			File path = this.image.saveToDir(this.dir);
	        super.setResult(path.getAbsolutePath());
		}
		else
		{
			super.setError("Either dir or file should be filled.", ErrorKind.WRONG_PARAMETERS);
		}
	}

}
