package com.exactprosystems.jf.common.RtfHelp;

import javax.swing.text.BadLocationException;
import java.io.IOException;

public class RtfGenerator
{
    public static void createRTF() throws IOException, BadLocationException
    {
        RTFCreator creator = new RTFCreator();

        creator.prepareDocument();
        creator.getAnnotationsForActions();
        creator.getAnnotationsForItems();
        creator.saveDocument();
    }
}
