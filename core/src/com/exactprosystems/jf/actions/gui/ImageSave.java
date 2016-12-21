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
		generalDescription 		= "Get an image from screen",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Full path to file", 
		outputType 				= String.class
	)
public class ImageSave extends AbstractAction
{
	public final static String	imageName	= "Image";
	public final static String	dirName		= "Dir";
	public final static String	fileName	= "File";
	
	@ActionFieldAttribute(name = imageName, mandatory = true, description = "Image to save.")
	protected ImageWrapper		image		= null;

	@ActionFieldAttribute(name = dirName, mandatory = false, description = "Directory to save this image. File name will be get automaticly.")
	protected String			dir;

	@ActionFieldAttribute(name = fileName, mandatory = false, description = "File to save this image.")
	protected String			file;

	public ImageSave()
	{
	}
	
	@Override
	public void initDefaultValues()
	{
		dir			= null;
		file		= null;
	}
	
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
			File path = this.image.saveToDir(this.dir);
	        super.setResult(path.getAbsolutePath());
		}
		else
		{
			super.setError("Either dir or file should be filled.", ErrorKind.WRONG_PARAMETERS);
		}
	}

}
