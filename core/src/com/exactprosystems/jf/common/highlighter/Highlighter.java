////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.common.highlighter;

import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum Highlighter
{
	None
	{
		@Override
		protected Map<String, String> groupPatternMap()
		{
			return new HashMap<>();
		}

		@Override
		protected Map<String, String> groupStyleMap()
		{
			return new HashMap<>();
		}
	},

	Matrix
	{
		private final String[] KEYWORDS = Arrays.stream(Tokens.values())
				.map(Enum::name)
				.collect(Collectors.toList())
				.toArray(new String[Tokens.values().length]);

		private final String KEYWORD_PATTERN       = "#(" + String.join("|", KEYWORDS) + ")\\b";
		private final String PAREN_PATTERN         = "\\(|\\)";
		private final String BRACE_PATTERN         = "\\{|\\}";
		private final String BRACKET_PATTERN       = "\\[|\\]";
		private final String SEMICOLON_PATTERN     = ";";
		private final String STRING_PATTERN        = "\"([^\"\n])*\"";
		private final String STRING_PATTERN2       = "'([^\'\n])*\'";
		private final String COMMENT_PATTERN       = "//.*";
		private final String ACTIONS_PATTERN       = "\\b(" + String.join("|", Arrays.stream(ActionsList.actions).map(Class::getSimpleName).collect(Collectors.toList())) + ")\\b";

		@Override
		protected Map<String, String> groupPatternMap()
		{
			Map<String, String> map = new HashMap<>();
			map.put("KEYWORD", KEYWORD_PATTERN);
			map.put("PAREN", PAREN_PATTERN);
			map.put("BRACE", BRACE_PATTERN);
			map.put("BRACKET", BRACKET_PATTERN);
			map.put("SEMICOLON", SEMICOLON_PATTERN);
			map.put("STRING", STRING_PATTERN);
			map.put("STRING2", STRING_PATTERN2);
			map.put("COMMENT", COMMENT_PATTERN);
			map.put("ACTIONS", ACTIONS_PATTERN);
			return map;
		}

		@Override
		protected Map<String, String> groupStyleMap()
		{
			//TODO replace to CssVariables.
			Map<String, String> map = new HashMap<>();
			map.put("KEYWORD", "keyword");
			map.put("PAREN", "paren");
			map.put("BRACE", "brace");
			map.put("BRACKET", "bracket");
			map.put("SEMICOLON", "semicolon");
			map.put("STRING", "string");
			map.put("STRING2", "string");
			map.put("COMMENT", "comment");
			map.put("ACTIONS", "actions");
			return map;
		}
	},

	Sql
	{
		private final String[] KEYWORDS = new String[]{
				"TABLE", "FROM", "DEFAULT", "ALTER TABLE",
				"AND", "AS", "AVG", "BETWEEN",
				"COUNT", "CREATE TABLE", "DELETE",
				"GROUP BY", "INNER JOIN", "NULL",
				"INSERT", "LIKE", "LIMIT",
				"MAX", "MIN", "OR", "ORDER BY",
				"JOIN", "ROUND", "SELECT",
				"SELECT DISTINCT", "SUM", "UPDATE", "WHERE"
		};

		private final String KEYWORD_PATTERN       = "(" + String.join("|", KEYWORDS) + ")\\b";
		private final String PAREN_PATTERN         = "\\(|\\)";
		private final String BRACKET_PATTERN       = "\\[|\\]";
		private final String SEMICOLON_PATTERN     = ";";
		private final String STRING_PATTERN        = "\"([^\"\n])*\"";
		private final String STRING_PATTERN2       = "'([^\'\n])*\'";

		@Override
		protected Map<String, String> groupPatternMap()
		{
			Map<String, String> map = new HashMap<>();
			map.put("KEYWORD", KEYWORD_PATTERN);
			map.put("PAREN", PAREN_PATTERN);
			map.put("BRACKET", BRACKET_PATTERN);
			map.put("SEMICOLON", SEMICOLON_PATTERN);
			map.put("STRING", STRING_PATTERN);
			map.put("STRING2", STRING_PATTERN2);
			return map;
		}

		@Override
		protected Map<String, String> groupStyleMap()
		{
			Map<String, String> map = new HashMap<>();
			map.put("KEYWORD", "keyword");
			map.put("PAREN", "paren");
			map.put("BRACKET", "bracket");
			map.put("SEMICOLON", "semicolon");
			map.put("STRING", "string");
			map.put("STRING2", "string");
			return map;
		}
	},

	Xml
	{
		private final Pattern XML_TAG = Pattern.compile(
				"(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*/?>))"
				+"|(?<COMMENT><!--[^<>]+-->)");

		private final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");

		private final int GROUP_OPEN_BRACKET = 2;
		private final int GROUP_ELEMENT_NAME = 3;
		private final int GROUP_ATTRIBUTES_SECTION = 4;
		private final int GROUP_CLOSE_BRACKET = 5;
		private final int GROUP_ATTRIBUTE_NAME = 1;
		private final int GROUP_EQUAL_SYMBOL = 2;
		private final int GROUP_ATTRIBUTE_VALUE = 3;

		@Override
		protected Map<String, String> groupPatternMap()
		{
			return null;
		}

		@Override
		protected Map<String, String> groupStyleMap()
		{
			return null;
		}

		@Override
		public List<StyleWithRange> getStyles(String text)
		{
			Matcher matcher = XML_TAG.matcher(text);
			int last = 0;
			List<StyleWithRange> list = new ArrayList<>();

			while (matcher.find())
			{
				list.add(new StyleWithRange(null, matcher.start() - last));
				if (matcher.group("COMMENT") != null)
				{
					list.add(new StyleWithRange("comment", matcher.end() - matcher.start()));
				}
				else if (matcher.group("ELEMENT") != null)
				{
					String attributesText = matcher.group(GROUP_ATTRIBUTES_SECTION);
					list.add(new StyleWithRange("tagmart", matcher.end(GROUP_OPEN_BRACKET) - matcher.start(GROUP_OPEN_BRACKET)));
					list.add(new StyleWithRange("anytag", matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET)));
					if (!attributesText.isEmpty())
					{
						last = 0;
						Matcher aMatcher = ATTRIBUTES.matcher(attributesText);
						while (aMatcher.find())
						{
							list.add(new StyleWithRange(null, aMatcher.start() - last));
							list.add(new StyleWithRange("attribute", aMatcher.end(GROUP_ATTRIBUTE_NAME) - aMatcher.start(GROUP_ATTRIBUTE_NAME)));
							list.add(new StyleWithRange("tagmark", aMatcher.end(GROUP_EQUAL_SYMBOL) - aMatcher.end(GROUP_ATTRIBUTE_NAME)));
							list.add(new StyleWithRange("avalue", aMatcher.end(GROUP_ATTRIBUTE_VALUE) - aMatcher.end(GROUP_EQUAL_SYMBOL)));
							last = aMatcher.end();
						}
						if (attributesText.length() > last)
						{
							list.add(new StyleWithRange(null, attributesText.length() - last));
						}
					}
					last = matcher.end(GROUP_ATTRIBUTES_SECTION);
					list.add(new StyleWithRange("tagmark", matcher.end(GROUP_CLOSE_BRACKET) - last));
				}
				last = matcher.end();
			}
			list.add(new StyleWithRange(null, text.length() - last));
			return list;
		}
	},

	Java
	{
		private final String[] KEYWORDS = new String[]{
				"new", "with", "assert", "isdef",
				"def", "return",
				"contains", "is", "instanceof",
				"in"
		};

		private final String[] SYMBOLS = new String[] {
				"!","==", "!=", ">", "<", ">=", "<=",
				"\\|", "or", "~=",
				"&", "\\|", "^", "\\+", "\\-", "/", "\\*","%", ":"
		};

		private final String KEYWORD_PATTERN       = "(" + String.join("|", KEYWORDS) + ")\\b";
		private final String SYMBOLS_PATTERN       = "(" + String.join("|", SYMBOLS) + ")";
		private final String PAREN_PATTERN         = "\\(|\\)";
		private final String BRACE_PATTERN       = "\\[|\\]";
		private final String BRACKET_PATTERN       = "\\{|\\}";
		private final String SEMICOLON_PATTERN     = ";";
		private final String STRING_PATTERN        = "\"([^\"\n])*\"";
		private final String STRING_PATTERN2       = "'([^\'\n])*\'";

		@Override
		protected Map<String, String> groupPatternMap()
		{
			Map<String, String> map = new HashMap<>();
			map.put("KEYWORD", KEYWORD_PATTERN);
			map.put("SYMBOLS", SYMBOLS_PATTERN);
			map.put("PAREN", PAREN_PATTERN);
			map.put("BRACE", BRACE_PATTERN);
			map.put("BRACKET", BRACKET_PATTERN);
			map.put("SEMICOLON", SEMICOLON_PATTERN);
			map.put("STRING", STRING_PATTERN);
			map.put("STRING2", STRING_PATTERN2);
			return map;
		}

		@Override
		protected Map<String, String> groupStyleMap()
		{
			Map<String, String> map = new HashMap<>();
			map.put("KEYWORD", "keyword");
			map.put("SYMBOLS", "symbols");
			map.put("PAREN", "paren");
			map.put("BRACE", "brace");
			map.put("BRACKET", "bracket");
			map.put("SEMICOLON", "semicolon");
			map.put("STRING", "string");
			map.put("STRING2", "string");
			return map;
		}
	},

	Gherkin
	{
		private GherkinDialect dialect = new GherkinDialectProvider().getDefaultDialect();

		private List<String> quote(List<String> list, Function<String, String> mapper)
		{
			return list.stream().map(mapper).map(Pattern::quote).collect(Collectors.toList());
		}

		private Function<String, String> function = s -> s + ":";

		private final String FUTURE_PATTERN = "(" + String.join("|", quote(dialect.getFeatureKeywords(), function)) + ")";
		private final String SCENARIO_PATTERN = "(" + String.join("|", quote(dialect.getScenarioKeywords(), function)) + ")";
		private final String STEPS_PATTERN = "(" + String.join("|", quote(dialect.getStepKeywords(), Function.identity())) + ")";

		@Override
		protected Map<String, String> groupPatternMap()
		{
			Map<String, String> map = new HashMap<>();

			map.put("FUTURE", FUTURE_PATTERN);
			map.put("SCENARIO", SCENARIO_PATTERN);
			map.put("STEPS", STEPS_PATTERN);

			return map;
		}

		@Override
		protected Map<String, String> groupStyleMap()
		{
			Map<String, String> map = new HashMap<>();

			map.put("FUTURE", "keyword");
			map.put("SCENARIO", "paren");
			map.put("STEPS", "comment");

			return map;
		}
	};

	public static Highlighter byName(String name)
	{
		return Arrays.stream(values()).filter(h -> h.name().toLowerCase().equals(name.toLowerCase())).findFirst().orElseThrow(() -> new RuntimeException("Unknown name : " + name));
	}

	public List<StyleWithRange> getStyles(String text)
	{
		String patternString = groupPatternMap().entrySet()
				.stream()
				.map(entry -> String.format("(?<%s>%s)", entry.getKey(), entry.getValue()))
				.collect(Collectors.joining("|"));

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);
		int last = 0;
		ArrayList<StyleWithRange> list = new ArrayList<>();
		Map<String, String> groupStyleMap = groupStyleMap();
		while (matcher.find())
		{
			String style = groupStyleMap.entrySet()
					.stream()
					.filter(entry -> matcher.group(entry.getKey()) != null)
					.findFirst()
					.map(Map.Entry::getValue)
					.orElse(null);

			list.add(new StyleWithRange(null, matcher.start() - last));
			list.add(new StyleWithRange(style, matcher.end() - matcher.start()));
			last = matcher.end();
		}
		list.add(new StyleWithRange(null, text.length() - last));
		return list;
	}

	protected abstract Map<String, String> groupPatternMap();
	protected abstract Map<String, String> groupStyleMap();
}
