////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.helper;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.FieldParameter;
import com.exactprosystems.jf.api.common.HideAttribute;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.Common;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class HelperFx
{
	private Matrix matrix;
	private Class<?> clazz;
	private HelperControllerFx controller;
	private AbstractEvaluator evaluator;
	private Comparator<IToString> comparatorAZ;
	private Comparator<IToString> comparatorZA;

	public HelperFx(String title, AbstractEvaluator evaluator, Matrix matrix)
	{
		this(title, evaluator, matrix, null);
	}

	public HelperFx(String title, AbstractEvaluator evaluator, Matrix matrix, Class<?> clazz)
	{
		this.evaluator = evaluator;
		this.matrix = matrix;
		this.clazz = clazz;
		comparatorAZ = Comparator.comparing(IToString::getName);
		comparatorZA = comparatorAZ.reversed();

		this.controller = Common.loadController(HelperFx.class.getResource("HelperFx.fxml"));
		this.controller.init(this, title, this.clazz, this.evaluator != null);
	}

	public String showAndWait(String value) throws IOException
	{
		return this.controller.showAndWait(value);
	}

	public void evaluate(String expression, boolean showVoid, boolean showStatic, boolean ascentingSorting)
	{
		if (this.evaluator != null)
		{
			Object compiled;
			try
			{
				compiled = this.evaluator.compile(expression);
			}
			catch (Exception e)
			{
				this.controller.compileFailed("Compile error.\n" + e.getMessage());
				return;
			}
			try
			{
				Object value = this.evaluator.execute(compiled);
				Class<?> clazz = value.getClass();

				if (value instanceof Class)
				{
					clazz = (Class<?>) value;
				}

				ObservableList<IToString> observableAll = FXCollections.observableArrayList();

				for (Method method : clazz.getMethods())
				{
					if (method.getAnnotation(HideAttribute.class) != null)
					{
						continue;
					}
					SimpleMethod simpleMethod = getStringSimpleMethod(method);
					if (simpleMethod != null)
					{
						boolean add = true;

						if (!showVoid && simpleMethod.getReturnType().equals("void"))
						{
							add = false;
						}

						if (!showStatic && simpleMethod.isStatic())
						{
							add = false;
						}

						if (add)
						{
							observableAll.add(simpleMethod);
						}
					}
				}

				observableAll.addAll(Arrays.stream(clazz.getFields())
						.filter(t -> t.getAnnotation(HideAttribute.class) == null )
						.map(this::getStringsSimpleField)
						.filter(Objects::nonNull)
						.collect(Collectors.toList())
				);
				Collections.sort(observableAll, ascentingSorting ? comparatorAZ : comparatorZA);

				this.controller.displayClass(clazz);
				this.controller.displayMethods(observableAll);
				this.controller.successEvaluate(value.toString());
			}
			catch (Exception e)
			{
				this.controller.evaluateFailed("Can't evaluate.\n" + e.getMessage());
			}
		}
	}

	public void fillVariables(final ObservableList<SimpleVariable> data)
	{
		Common.tryCatch(() -> {
			Variables localVars = this.evaluator.getLocals();
			localVars.getVars().entrySet().forEach((entry) -> data.add(new SimpleVariable(entry.getKey(), entry.getValue())));

			Variables globalVars = this.evaluator.getGlobals();
			data.addAll(globalVars.getVars().entrySet()
					.stream()
					.map(entry -> new SimpleVariable(entry.getKey(), entry.getValue()))
					.filter(simpleVariable -> !data.contains(simpleVariable))
					.collect(Collectors.toList())
			);
			Optional.ofNullable(this.matrix).ifPresent(m -> m.getRoot().bypass(item -> {
				String id = item.getId();
				if (!Str.IsNullOrEmpty(id))
				{
					Object res;
					try
					{
						res = evaluator.evaluate(id + ".Out");
					}
					catch (Exception e)
					{
						res = "Error";
					}
					data.add(new SimpleVariable(id + ".Out", res));
				}
			}));
		}, "Error on set variables");
	}

	//============================================================
	// private methods
	//============================================================
	private SimpleMethod getStringSimpleMethod(Method method)
	{
		if (method.getAnnotation(Deprecated.class) != null)
		{
			return null;
		}
		return new SimpleMethod(method);
	}

	private SimpleField getStringsSimpleField(Field field)
	{
		String modifiers = Modifier.toString(field.getModifiers());
		if (!modifiers.contains("public"))
		{
			return null;
		}
		return new SimpleField(field);
	}


	static class SimpleMethod implements IToString
	{
		private Method method;

		public SimpleMethod(Method method)
		{
			this.method = method;
		}

		public boolean isStatic()
		{
			return (this.method.getModifiers() & Modifier.STATIC) != 0;
		}

		private List<String> getParameters()
		{
			return Arrays.stream(this.method.getParameters())
					.filter(p -> p.getAnnotation(HideAttribute.class) == null)
					.map(p -> {
						String parameterName = Optional.ofNullable(p.getAnnotation(FieldParameter.class))
								.map(FieldParameter::name)
								.orElse("");

						return p.getType().getSimpleName() + " " + parameterName;
					})
					.collect(Collectors.toList());
		}

		private List<String> getParameterTypes()
		{
			return Arrays.stream(this.method.getParameterTypes())
					.filter(t -> t.getAnnotation(HideAttribute.class) == null )
					.map(Class::getSimpleName)
					.collect(Collectors.toList());
		}

		@Override
		public String getName()
		{
			return this.method.getName();
		}

		public String getReturnType()
		{
			return this.method.getReturnType().getSimpleName();
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(isStatic() ? "S" : "").append('\t').append(getName()).append('(');

			String parameters = getParameters()
					.stream()
					.collect(Collectors.joining(", "));
			sb.append(parameters);

			sb.append(") : ").append(getReturnType());

			return sb.toString();
		}

		public String getMethodWithParams()
		{
			StringBuilder s = new StringBuilder(getName());
			s.append("(");
			String comma = "";
			for (String parameter : getParameterTypes())
			{
				s.append(comma);
				s.append(parameter);
				comma = ", ";
			}
			s.append(")");
			return s.toString();
		}

		@Override
		public String getDescription()
		{
			return Optional.ofNullable(this.method.getAnnotation(DescriptionAttribute.class))
					.map(DescriptionAttribute::text)
					.orElse(null);
		}
	}

	static class SimpleField implements IToString
	{
		private Field field;

		public SimpleField(Field field)
		{
			this.field = field;
		}

		public String getName()
		{
			return this.field.getName();
		}

		@Override
		public String toString()
		{
			return "\t" + this.field.getName();
		}

		@Override
		public String getDescription()
		{
			return Optional.ofNullable(this.field.getAnnotation(DescriptionAttribute.class))
					.map(DescriptionAttribute::text)
					.orElse(null);
		}
	}

	interface IToString
	{
		String toString();
		String getName();
		String getDescription();
	}
}
