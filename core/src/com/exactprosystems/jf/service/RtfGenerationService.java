package com.exactprosystems.jf.service;

import javax.swing.text.BadLocationException;
import java.io.IOException;

public class RtfGenerationService {

   public RtfGenerationService()
   {
   }

    private static RTFCreator creator;

    static {
        creator = new RTFCreator();
    }

   public static void main(String[] args)
   {
       //TODO
       try
       {
           new RtfGenerationService().createRTF();
       }
       catch (Exception e)
       {
            e.printStackTrace();
       }
   }

   public void createRTF() throws IOException, BadLocationException {
       creator.prepareDocument();
       creator.createContents();
       creator.createDescription();
       creator.mvelDocumentation();
       creator.getAnnotationsForActions();
       creator.getAnnotationsForItems();
       creator.saveDocument();
   }
}
