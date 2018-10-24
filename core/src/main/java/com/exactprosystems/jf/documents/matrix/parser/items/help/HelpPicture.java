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

package com.exactprosystems.jf.documents.matrix.parser.items.help;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder.ImageReportMode;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class HelpPicture extends MatrixItem
{
	private String title;
	private InputStream stream = null;
	private int scale = 100;

	//TODO remove this input stream.
	public HelpPicture(String title, InputStream stream, int scale)
	{
	    this.title = title;
        this.stream = stream;
        this.scale = scale;
	}

	@Override
	protected MatrixItem makeCopy()
	{
		//TODO
		return new HelpPicture(this.title, this.stream, this.scale);
	}

	@Override
	public String getItemName()
	{
		return "";
	}
	
	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
        try
        {
            BufferedImage imBuff = ImageIO.read(this.stream);
            ImageWrapper image = new ImageWrapper(imBuff);
            report.outImage(this, null, image.getName(report.getReportDir()), null, this.title, this.scale, ImageReportMode.AsImage);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        return new ReturnAndResult(start, Result.Passed); 
	}
}
