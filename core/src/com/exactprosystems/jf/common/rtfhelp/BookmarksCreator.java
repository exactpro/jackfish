package com.exactprosystems.jf.common.rtfhelp;

import java.io.*;

public class BookmarksCreator extends FileWriter{

    private String createCell(String text, boolean last)
    {
        String insertText =  text;
        if (text.contains("\\{\\{*") && text.contains("*\\}\\}"))
        {
            StringBuilder sb = new StringBuilder();
            for (String s : text.split("\\s+"))
            {
                if (s.contains("\\{\\{*") && s.contains("*\\}\\}"))
                {
                    sb.append(s.replace(s, "{\\b " + s.replace("\\{\\{*", "").replace("*\\}\\}", "") + '}')).append(" ");
                }
                else
                {
                    sb.append(s).append(" ");
                }
            }
            insertText = sb.toString().replace("\\tab", "");
        }
        if (last){
            return "\\s20\\ql\\nowidctlpar\\hyphpar0\\ltrpar\\cf1\\kerning1\\dbch\\af5\\langfe1081\\dbch\\af6\\afs24\\loch\\f3\\fs"
                    + 20 + "\\lang1033\\intbl{\\rtlch \\ltrch\\loch "
                    + insertText + "}\\cell\\row\\pard";
        } else {
            return "\\s20\\ql\\nowidctlpar\\hyphpar0\\ltrpar\\cf1\\kerning1\\dbch\\af5\\langfe1081\\dbch\\af6\\afs24\\loch\\f3\\fs"
                    + 20 + "\\lang1033\\intbl{\\rtlch \\ltrch\\loch "
                    + insertText + "}\\cell\\pard\\plain";
        }
    }

    private String createRows(String initialText)
    {
        final String twoCells = "\\trowd\\trql\\ltrrow\\trpaddft3\\trpaddt0\\trpaddfl3\\trpaddl0\\trpaddfb3\\trpaddb0\\trpaddfr3\\trpaddr0\\clbrdrt\\brdrhair\\brdrw1\\brdrcf1\\clbrdrl"
                + "\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair\\brdrw1\\brdrcf1\\cellx4819\\clbrdrt\\brdrhair\\brdrw1\\brdrcf1\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair"
                + "\\brdrw1\\brdrcf1\\clbrdrr\\brdrhair\\brdrw1\\brdrcf1\\cellx9638\\pgndec\\pard\\plain \n";
        final String threeCells = "\\trowd\\trql\\trleft0\\ltrrow\\trpaddft3\\trpaddt0\\trpaddfl3\\trpaddl0\\trpaddfb3\\trpaddb0\\trpaddfr3\\trpaddr0\\clbrdrt\\brdrs\\brdrw2\\brdrcf17"
                + "\\clbrdrl\\brdrs\\brdrw2\\brdrcf17\\clbrdrb\\brdrs\\brdrw2\\brdrcf17\\cellx3212\\clbrdrt\\brdrs\\brdrw2\\brdrcf17\\clbrdrl\\brdrs\\brdrw2\\brdrcf17\\clbrdrb\\brdrs\\brdrw2"
                + "\\brdrcf17\\cellx6425\\clbrdrt\\brdrs\\brdrw2\\brdrcf17\\clbrdrl\\brdrs\\brdrw2\\brdrcf17\\clbrdrb\\brdrs\\brdrw2\\brdrcf17\\clbrdrr\\brdrs\\brdrw2\\brdrcf17\\cellx9638\\pgndec\\pard\\plain \n";
        final String fourCells = "\\trowd\\trql\\ltrrow\\trpaddft3\\trpaddt0\\trpaddfl3\\trpaddl0\\trpaddfb3\\trpaddb0\\trpaddfr3\\trpaddr0\\clbrdrt\\brdrhair\\brdrw1\\brdrcf1\\clbrdrl\\brdrhair"
                + "\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair\\brdrw1\\brdrcf1\\cellx2409\\clbrdrt\\brdrhair\\brdrw1\\brdrcf1\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair\\brdrw1\\brdrcf1"
                + "\\cellx4819\\clbrdrt\\brdrhair\\brdrw1\\brdrcf1\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair\\brdrw1\\brdrcf1\\cellx7228\\clbrdrt\\brdrhair\\brdrw1\\brdrcf1"
                + "\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair\\brdrw1\\brdrcf1\\clbrdrr\\brdrhair\\brdrw1\\brdrcf1\\cellx9638\\pgndec\\pard\\plain \n";
        final String twoSeparator = "\\trowd\\trql\\ltrrow\\trpaddft3\\trpaddt0\\trpaddfl3\\trpaddl0\\trpaddfb3\\trpaddb0\\trpaddfr3\\trpaddr0\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb"
                + "\\brdrhair\\brdrw1\\brdrcf1\\cellx4819\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair\\brdrw1\\brdrcf1\\clbrdrr\\brdrhair\\brdrw1\\brdrcf1\\cellx9638\\pard\\plain";
        final String threeSeparator = "\\trowd\\trql\\trleft0\\ltrrow\\trpaddft3\\trpaddt0\\trpaddfl3\\trpaddl0\\trpaddfb3\\trpaddb0\\trpaddfr3\\trpaddr0\\clbrdrl\\brdrs\\brdrw15\\brdrcf17"
                + "\\clbrdrb\\brdrs\\brdrw15\\brdrcf17\\cellx3212\\clbrdrl\\brdrs\\brdrw15\\brdrcf17\\clbrdrb\\brdrs\\brdrw15\\brdrcf17\\cellx6425\\clbrdrl\\brdrs\\brdrw15\\brdrcf17\\clbrdrb"
                + "\\brdrs\\brdrw15\\brdrcf17\\clbrdrr\\brdrs\\brdrw15\\brdrcf17\\cellx9638\\pard\\plain \n";
        final String fourSeparator = "\\trowd\\trql\\ltrrow\\trpaddft3\\trpaddt0\\trpaddfl3\\trpaddl0\\trpaddfb3\\trpaddb0\\trpaddfr3\\trpaddr0\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb"
                + "\\brdrhair\\brdrw1\\brdrcf1\\cellx2409\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair\\brdrw1\\brdrcf1\\cellx4819\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair"
                + "\\brdrw1\\brdrcf1\\cellx7228\\clbrdrl\\brdrhair\\brdrw1\\brdrcf1\\clbrdrb\\brdrhair\\brdrw1\\brdrcf1\\clbrdrr\\brdrhair\\brdrw1\\brdrcf1\\cellx9638\\pard\\plain";
        int constant = 0;
        StringBuilder sb = new StringBuilder();
        String [] rows = initialText.split("\\\\\\{\\\\\\{-");
        if (rows.length >= 1){
            String[] c = rows[1].split("\\\\\\{\\\\\\{\\+");
            switch (c.length-1){
                case 2:
                    sb.append(twoCells);
                    constant = 2;
                    break;
                case 3:
                    sb.append(threeCells);
                    constant = 3;
                    break;
                case 4:
                    sb.append(fourCells);
                    constant = 4;
                    break;

                default:
                    break;
            }
        }
        for (int i = 1; i < rows.length; i++)
        {
            String[] cells = rows[i].replace("-\\}\\}", "").split("\\\\\\{\\\\\\{\\+");
            for (int j = 1; j < cells.length; j++)
            {
                if (j != cells.length-1){
                    sb.append(createCell(cells[j].replace("+\\}\\}", ""), false));
                }
                else
                {
                    sb.append(createCell(cells[j].replace("+\\}\\}", ""), true));
                    if (i == rows.length -1)
                    {
                        sb.append("\\pard\\plain");
                    }
                    else
                    {
                        switch (constant){
                            case 2:
                                sb.append(twoSeparator);
                                break;
                            case 3:
                                sb.append(threeSeparator);
                                break;
                            case 4:
                                sb.append(fourSeparator);
                                break;

                            default:
                                break;
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

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
        if (csq.toString().contains("\\{\\{=") && csq.toString().contains("=\\}\\}"))
        {
            String initialText = csq.toString().replace("\\{\\{=", "").replace("=\\}\\}", "");
            return super.append(createRows(initialText));
        }
        String[] strs = csq.toString().split("\\s+");
        boolean addSpace = true;
        if (strs.length > 1){
            StringBuilder result = new StringBuilder();
            for (int k = 0; k < strs.length; k++){
                String s = strs[k];
                addSpace = true;
                if (s.contains("123456789"))
                {
                    int indexOne = s.indexOf("1");
                    String sBookmark = s.substring(0, indexOne);
                    String r = s.replace(s, "{\\bkmkstart " +sBookmark+ "}{\\bkmkend " + sBookmark +"}" + s.replace("123456789", ""));
                    result.append(r);
                }
                else if (s.contains("987654321"))
                {
                    String r = s.replace("987654321", "");
                    result.append(r);
                    addSpace = false;
                }
                else if (s.contains("WriteLine"))
                {
                    result.append(s.replace("WriteLine", "\\pard \\brdrb \\brdrs\\brdrw20\\brsp20\\brdrcf0 "));
                }
                else if (s.contains("WriteTopLine"))
                {
                    result.append(s.replace("WriteTopLine", "\\pard \\brdrt \\brdrs\\brdrw30\\brsp20\\brdrcf0 "));
                }
                else if (s.contains("FirstPageLine"))
                {
                    result.append(s.replace("FirstPageLine", "\\pard \\brdrb \\brdrs\\brdrw50\\brsp20\\brdrcf0 "));
                }
                else if (s.contains("WriteFonts"))
                {
                    result.append(s.replace("WriteFonts", "\\fswiss Arial;}{\\fmodern \\f1\\fcharset1 Courier;}{\\fscript \\f2\\fcharset1 Cursive"));
                }
                /*else if (s.equals("\\par"))
                {
                    result.append(s.replace("\\par", "\\par\\qd"));
                }*/
                else if (s.contains("\\{\\{*") && s.contains("*\\}\\}"))
                {
                    result.append(s.replace(s, "{\\b " + s.replace("\\{\\{*", "").replace("*\\}\\}", "") + '}'));
                }
                else if(s.contains("\\{\\{`") || s.contains("`\\}\\}") || s.contains("`\\}\\}\\{\\{`"))
                {
                    result.append(s.replace("\\{\\{`", " \\par ").replace("`\\}\\}", " \\par ").replace("`\\}\\}\\{\\{`", " \\par "));
                }
                else
                {
                    result.append(s);
                }
                if ((k != strs.length -1) && addSpace)
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
                return super.append(s.replace("WriteFonts", "\\fswiss Arial;}{\\fmodern \\f1\\fcharset1 Courier;}{\\fscript \\f2\\fcharset1 Cursive"));
            }
            else if (s.contains("FirstPageLine"))
            {
                return super.append(s.replace("FirstPageLine", "\\pard \\brdrb \\brdrs\\brdrw50\\brsp20\\brdrcf0 "));
            }
            /*else if (s.equals("\\par"))
            {
                return super.append(s.replace("\\par", "\\par\\qd"));
            }*/
            else if (s.contains("\\{\\{*") && s.contains("*\\}\\}"))
            {
                return super.append(s.replace(s, "{\\b " + s.replace("\\{\\{*", "").replace("*\\}\\}", "") + '}'));
            }
            else if(s.contains("\\{\\{`") || s.contains("`\\}\\}") || s.contains("`\\}\\}\\{\\{`"))
            {
                return super.append(s.replace("\\{\\{`", " \\par ").replace("`\\}\\}", " \\par ").replace("`\\}\\}\\{\\{`", " \\par "));
            }
            else if (s.contains("987654321"))
            {
                return super.append(s.replace("987654321", ""));
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