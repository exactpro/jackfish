/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items.help;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.wizard.Wizard;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder.ImageReportMode;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class HelpWizardItem extends MatrixItem
{
	private Class<? extends Wizard> wizardClazz;
	private WizardManager manager;

	public HelpWizardItem(WizardManager manager, Class<? extends Wizard> itemClazz)
	{
		this.manager = manager;
		this.wizardClazz = itemClazz;
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new HelpWizardItem(this.manager, this.wizardClazz);
	}

	@Override
	public String getItemName()
	{
		return this.manager.nameOf(this.wizardClazz);
	}

	public void actionReport(ReportBuilder report)
	{
		String picture = this.manager.pictureOf(this.wizardClazz);
		String shortDescription = this.manager.shortDescriptionOf(this.wizardClazz);
		String category = this.manager.categoryOf(this.wizardClazz).toString();
		String detailedDescription = this.manager.detailedDescriptionOf(this.wizardClazz);

		report.itemIntermediate(this);

		byte[] bytes = null;
		try (InputStream inputStream = this.wizardClazz.getResourceAsStream(picture);
			 ByteArrayOutputStream buffer = new ByteArrayOutputStream()
		)
		{
			int read;
			byte[] data = new byte[1024 * 4];

			while ((read = inputStream.read(data, 0, data.length)) != -1)
			{
				buffer.write(data, 0, read);
			}

			buffer.flush();
			bytes = buffer.toByteArray();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		report.outLine(this, null, "{{`" + shortDescription + "`}}", null);
		report.outLine(this, null, "{{`{{*" + R.HELP_WIZARD_ITEM_WIZARD_CATEGORY.get() + " *}} " + category + "`}}", null);
		report.outImage(this, null, null, bytes, "{{* " + R.HELP_WIZARD_ITEM_VIEW_EXAMPLE.get() + " *}}", -1, ImageReportMode.AsEmbeddedImage);
		report.outLine(this, null, "{{`" + detailedDescription + "`}}", null);
	}


	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			if (this.wizardClazz != null)
			{
				actionReport(report);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		return new ReturnAndResult(start, Result.Passed);
	}
}
