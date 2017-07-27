package com.exactprosystems.jf.common.report;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Marker
{
	protected boolean isWhite = true;

	private static final String OM = "{{";
	private static final String CM = "}}";

	private static final String ALL_SYMBOLS = "(?s).*?";

	private static final String OM_REG = "\\{\\{";
	private static final String CM_REG = "\\}\\}";

	private enum Keys
	{
		A("$", true),
		B("#", false),
		C("@", false),
		D("^", true),
		E("`", false),
		F("_", false),
		G("*", true),
		H("/", false),
		I("&", false),
		M("1", false),
		N("2", false),
		O("3", false),
		P("4", false),
		Q("5", false),;
		String  key;
		boolean needEscape;

		Keys(String key, boolean needEscape)
		{
			this.key = key;
			this.needEscape = needEscape;
		}

		String groupName()
		{
			return this.name();
		}

		String key()
		{
			return this.needEscape ? "\\" + this.key : this.key;
		}

		String toRegexp()
		{
			return "(?<" + this.groupName() + ">" + OM_REG + this.key() + ALL_SYMBOLS + this.key() + CM_REG + ")";
		}
	}

	private final String REG_EXP = Arrays.stream(Keys.values()).map(Keys::toRegexp).collect(Collectors.joining("|"));

	public Marker(boolean isWhiteTheme)
	{
		this.isWhite = isWhiteTheme;
	}

	public final String process(String source)
	{
		source = this.beforeProcess(source);
		Pattern pattern = Pattern.compile(REG_EXP);
		Matcher matcher = pattern.matcher(source);
		StringBuffer sb = new StringBuffer(source.length());
		while (matcher.find())
		{
			for (Keys key : Keys.values())
			{
				String name = key.name();
				String group = matcher.group(name);
				if (group != null)
				{
					String value = convertValue(group, key);
					if (value.contains(OM) && value.contains(CM))
					{
						value = process(value);
					}
					matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
					break;
				}
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	protected abstract String convertValue(String group, Keys key);

	protected String beforeProcess(String source)
	{
		return source;
	}

	public static class HTMLMaker extends Marker
	{
		private Map<Keys, Tmp> map = new HashMap<Keys, Tmp>()
		{{
			put(Keys.A, new Tmp("<font color=\"" + (HTMLMaker.super.isWhite ? "#006400" : "#ffe30f") + "\">", "</font>"));
			put(Keys.B, new Tmp("<pre style=\"padding-left: 10px; color:" + (HTMLMaker.super.isWhite ? "#006400" : "#ffe30f") + "; margin: 10px; border-left: 1px solid #CCC;\"><code>", "</code></pre>"));
			put(Keys.C, new Tmp("<a style=\"color:#00bfff\">", "</a>"));
			put(Keys.D, new Tmp("<div class=\"rotate\"><div>", "</div></div>"));
			put(Keys.E, new Tmp("<p>", "</p>"));
			put(Keys.F, new Tmp("<u>", "</u>"));
			put(Keys.G, new Tmp("<b>", "</b>"));
			put(Keys.H, new Tmp("<i>", "</i>"));
			put(Keys.I, new Tmp("<p>", "</p>"));
			put(Keys.M, new Tmp("<h1>", "</h1>"));
			put(Keys.N, new Tmp("<h2>", "</h2>"));
			put(Keys.O, new Tmp("<h3>", "</h3>"));
			put(Keys.P, new Tmp("<h4>", "</h4>"));
			put(Keys.Q, new Tmp("<h5>", "</h5>"));
		}};

		public HTMLMaker(boolean isWhiteTheme)
		{
			super(isWhiteTheme);
		}

		@Override
		protected String convertValue(String group, Keys key)
		{
			Tmp value = this.map.get(key);
			if (value != null)
			{
				return group.replace(OM + key.key, value.begin).replace(key.key + CM, value.end);
			}
			return group;
		}
	}

	/*
	//TODO think about it
	public static class TexMarker extends Marker
	{
		private Map<Keys, Tmp> map = new HashMap<Keys, Tmp>()
		{{
			put(Keys.A, new Tmp("{\\\\color{codecolor} \\\\verb+", "+}"));
			put(Keys.B, new Tmp("\\\\begingroup\n" + "    \\\\fontsize{12pt}{10pt}\\\\selectfont\\\\color{codecolor}\n" + "    \\\\begin{verbatim}  ", "  \\\\end{verbatim}\n" + "\\\\endgroup"));
			put(Keys.C, new Tmp("\\\\superhyperlink{", "}"));
			put(Keys.D, new Tmp("\\\\rotatebox{90}{", "}"));
			put(Keys.E, new Tmp("", "\\\\newline{}"));
			put(Keys.F, new Tmp("\\\\underline{", "}"));
			put(Keys.G, new Tmp("\\\\textbf{", "}"));
			put(Keys.H, new Tmp("\\\\textit{", "}"));
			put(Keys.I, new Tmp("", "\\\\newpage \\\\pagestyle{allpages}"));
			put(Keys.M, new Tmp("\\\\crule[ExactColor]{\\\\textwidth}{3pt} \\\\newline \\\\section{ ", "}"));
			put(Keys.N, new Tmp("\\\\subsection{ ", "}"));
			put(Keys.O, new Tmp("\\\\subsubsection{ ", "}"));
			put(Keys.P, new Tmp("\\\\normalsize{", "}"));
			put(Keys.Q, new Tmp("\\\\large{", "}"));
		}};

		public TexMarker()
		{
			super(true);
		}

		@Override
		protected String convertValue(String group, Keys key)
		{
			Tmp value = this.map.get(key);
			if (value != null)
			{
				return group.replace(OM + key.key, value.begin).replace(key.key + CM, value.end);
			}
			return group;
		}

		@Override
		protected String beforeProcess(String source)
		{
			Pattern patt = Pattern.compile("([^\\\\]_)");
			Matcher m = patt.matcher(source);
			StringBuffer sb = new StringBuffer(source.length());
			while (m.find())
			{
				String text = m.group(1);
				text = text.replace("_", "\\\\_");
				m.appendReplacement(sb, text);
			}
			m.appendTail(sb);
			return sb.toString();
		}
	}
	*/

	private class Tmp
	{
		String begin;
		String end;

		public Tmp(String begin, String end)
		{
			this.begin = begin;
			this.end = end;
		}
	}

}
