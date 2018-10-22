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

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;


@ActionAttribute(
		group					   = ActionGroups.GUI,
		suffix					   = "DSK",
		constantGeneralDescription = R.DESKTOP_SCREENSHOT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.DESKTOP_SCREENSHOT_OUTPUT_DESC,
		outputType 				   = ImageWrapper.class,
		constantExamples 		   = R.DESKTOP_SCREENSHOT_EXAMPLE
	)
public class DesktopScreenshot extends AbstractAction
{
	public static final String descriptionName = "Description";

	@ActionFieldAttribute(name = descriptionName, mandatory = false, def = DefaultValuePool.EmptyString, constantDescription = R.DESKTOP_SCREENSHOT_DESCRIPTION_PARAM)
	protected String description;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Rectangle desktopRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage image = new java.awt.Robot().createScreenCapture(desktopRect);

		ImageWrapper imageWrapper = new ImageWrapper(image);

		if (!Str.IsNullOrEmpty(this.description))
		{
			imageWrapper.setDescription(this.description);
		}
		super.setResult(imageWrapper);
	}
}
