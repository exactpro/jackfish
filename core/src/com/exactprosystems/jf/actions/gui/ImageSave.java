/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.File;

@ActionAttribute(
		group					   = ActionGroups.GUI,
		suffix					   = "IMGSV",
		constantGeneralDescription = R.IMAGE_SAVE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.IMAGE_SAVE_OUTPUT_DESC,
		outputType 				   = String.class,
		constantExamples 		   = R.IMAGE_SAVE_EXAMPLE
	)
public class ImageSave extends AbstractAction
{
	public static final String imageName = "Image";
	public static final String dirName   = "Dir";
	public static final String fileName  = "File";

	@ActionFieldAttribute(name = imageName, mandatory = true, constantDescription = R.IMAGE_SAVE_IMAGE)
	protected ImageWrapper image;

	@ActionFieldAttribute(name = dirName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.IMAGE_SAVE_DIR)
	protected String dir;

	@ActionFieldAttribute(name = fileName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.IMAGE_SAVE_FILE)
	protected String file;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		switch(fieldName)
		{
			case dirName:
				return HelpKind.ChooseFolder;
				
			case fileName:
				return HelpKind.ChooseSaveFile;
			default:
				return null;
		}
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
			File savedDir = new File(this.dir);
			if (!savedDir.exists())
			{
				super.setError("Directory '" + this.dir + "' does not exist", ErrorKind.WRONG_PARAMETERS);
				return;
			}
			if (!savedDir.isDirectory())
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
