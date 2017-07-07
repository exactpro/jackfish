////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.common.utils.XpathUtils;

import javafx.application.Platform;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WizardHelper
{
    private static final Logger logger = Logger.getLogger(WizardHelper.class);
    
    private WizardHelper()
    {
    }

    public static void init(AppConnection currentConnection, IControl self,
            BiConsumer<BufferedImage, Document> onSuccess, Consumer<Throwable> onError)
    {
        Thread thread = new Thread(() ->
        {
            try
            {
                Locator selfLocator = self == null ? null : self.locator();
                IRemoteApplication service = currentConnection.getApplication().service();

                // get picture
                Rectangle rectangle = service.getRectangle(null, selfLocator);
                BufferedImage image = service.getImage(null, selfLocator).getImage();

                // get XML document
                byte[] treeBytes = service.getTreeBytes(selfLocator);
                Document document = Converter.convertByteArrayToXmlDocument(treeBytes);
                if (rectangle != null)
                {
                    XpathUtils.applyOffset(document, rectangle.x, rectangle.y);
                }
                
                if (onSuccess != null)
                {
                    Platform.runLater(() ->
                    {
                        onSuccess.accept(image, document);
                    });
                }
            }
            catch (Throwable exception)
            {
                logger.error(exception.getMessage(), exception);
                if (onError != null)
                {
                    Platform.runLater(() ->
                    {
                        onError.accept(exception);
                    });
                }
            }
        });
        thread.start();
    }
}
