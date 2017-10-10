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
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.Common;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class HelperFx
{
	private Matrix matrix;
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
		comparatorAZ = Comparator.comparing(IToString::getName);
		comparatorZA = comparatorAZ.reversed();

		this.controller = Common.loadController(HelperFx.class.getResource("HelperFx.fxml"));
		this.controller.init(this, title, clazz, this.evaluator != null);
	}
	
	public String showAndWait(String value)
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

				Arrays.stream(clazz.getMethods())
						.filter(m -> m.getAnnotation(HideAttribute.class) == null)
						.map(this::getStringSimpleMethod)
						.filter(simpleMethod -> !showVoid && simpleMethod.getReturnType().equalsIgnoreCase(Void.TYPE.getSimpleName()) || !showStatic && simpleMethod.isStatic())
						.forEach(observableAll::add);

				Arrays.stream(clazz.getFields())
						.filter(t -> t.getAnnotation(HideAttribute.class) == null )
						.map(this::getStringsSimpleField)
						.filter(Objects::nonNull)
						.forEach(observableAll::add);

				observableAll.sort(ascentingSorting ? comparatorAZ : comparatorZA);

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

	void fillVariables(final List<SimpleVariable> data)
	{
		this.evaluator.getLocals().getVars()
				.forEach((key, value) -> data.add(new SimpleVariable(key, value)));

		this.evaluator.getGlobals().getVars().entrySet()
				.stream()
				.map(entry -> new SimpleVariable(entry.getKey(), entry.getValue()))
				.filter(simpleVariable -> !data.contains(simpleVariable))
				.forEach(data::add);

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
	}

	//region private methods
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
		if (!Modifier.isPublic(field.getModifiers()))
		{
			return null;
		}
		return new SimpleField(field);
	}

	//endregion

	static class SimpleMethod implements IToString
	{
		private Method method;

		public SimpleMethod(Method method)
		{
			this.method = method;
		}

		public boolean isStatic()
		{
			return Modifier.isStatic(this.method.getModifiers());
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
			return getParameterTypes()
					.stream()
					.collect(Collectors.joining(",", "(", ")"));
		}

		@Override
		public String getDescription()
		{
			//TODO add all annotations
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
