package com.exactprosystems.jf.service;

import java.io.*;

public class BookmarksCreator extends FileWriter{

    public BookmarksCreator(String fileName, boolean append) throws IOException {
        super(fileName, append);
    }

    public BookmarksCreator(File file) throws IOException {
        super(file);
    }

    public BookmarksCreator(File file, boolean append) throws IOException {
        super(file, append);
    }

    public BookmarksCreator(FileDescriptor fd) {
        super(fd);
    }

    public BookmarksCreator(String fileName) throws IOException {
        super(fileName);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        String[] strs = csq.toString().split("\\s+");
        if (strs.length > 1){
            StringBuilder result = new StringBuilder();
            for (int k = 0; k < strs.length; k++){
                String s = strs[k];
                if (s.contains("123456789"))
                {
                    int indexOne = s.indexOf("1");
                    String sBookmark = s.substring(0, indexOne);
                    String r = s.replace(s, "{\\bkmkstart " +sBookmark+ "}{\\bkmkend " + sBookmark +"}" + s.replace("123456789", ""));
                    result.append(r);
                }
                else if (s.contains("WriteLine"))
                {
                    result.append(s.replace("WriteLine", "\\pard \\brdrb \\brdrs\\brdrw20\\brsp20\\brdrcf0 "));
                }
                else if (s.contains("WriteTopLine"))
                {
                    result.append(s.replace("WriteTopLine", "\\pard \\brdrt \\brdrs\\brdrw30\\brsp20\\brdrcf0 "));
                }
                else if (s.contains("WriteFonts"))
                {
                    result.append(s.replace("WriteFonts", "Arial;}{\\f1\\fcharset1 Courier;}{\\f2\\fcharset2 FS Lola ExtraBold"));
                }
                else if (s.equals("\\par"))
                {
                    result.append(s.replace("\\par", "\\par\\qd"));
                }
                else if (s.contains("\\{\\{*") && s.contains("*\\}\\}"))
                {
                    result.append(s.replace(s, "{\\b " + s.replace("\\{\\{*", "").replace("*\\}\\}", "") + '}'));
                }
                else
                {
                    result.append(s);
                }
                if (k != strs.length -1)
                {
                    result.append(" ");
                }
            }
            return super.append(result);
        } else if (strs.length == 1)
        {
            String s = strs[0];
            if (s.contains("123456789"))
            {
                int index = s.indexOf("1");
                String sBookmark = s.substring(0, index);
                String res =  s.replace(s, "{\\bkmkstart " + sBookmark+ "}{\\bkmkend " + sBookmark +"}" + s.replace("123456789", ""));
                return super.append(res);
            }
            else if (s.contains("WriteLine"))
            {
                return super.append(s.replace("WriteLine", "\\pard \\brdrb \\brdrs\\brdrw20\\brsp20\\brdrcf0 "));
            }
            else if (s.contains("WriteTopLine"))
            {
                return super.append(s.replace("WriteTopLine", "\\pard \\brdrt \\brdrs\\brdrw30\\brsp20\\brdrcf0 "));
            }
            else if (s.contains("WriteFonts"))
            {
                return super.append(s.replace("WriteFonts", "Arial;}{\\f1\\fcharset1 Courier;}{\\f2\\fcharset2 FS Lola ExtraBold"));
            }
            else if (s.equals("\\par"))
            {
                return super.append(s.replace("\\par", "\\par\\qd"));
            }
            else if (s.contains("\\{\\{*") && s.contains("*\\}\\}"))
            {
                return super.append(s.replace(s, "{\\b " + s.replace("\\{\\{*", "").replace("*\\}\\}", "") + '}'));
            }
            else
            {
                return super.append(s);
            }
        } else
        {
            return super.append(csq);
        }
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        return super.append(csq, start, end);
    }

    @Override
    public Writer append(char c) throws IOException {
        return super.append(c);
    }
}