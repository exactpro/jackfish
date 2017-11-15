////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class TabConsole extends PrintStream
{
	private Consumer<String> consumer;

	public TabConsole(OutputStream out)
	{
		super(out);
	}

	public TabConsole makeCopy()
	{
		TabConsole res = new TabConsole(this.out);
		res.consumer = this.consumer;
		return res;
	}

	@Override
	public void print(final String s)
	{
		if (this.consumer == null)
		{
			super.print(s);
		}
		else
		{
			this.consumer.accept(s);
		}
	}

	@Override
	public void println(final String x)
	{
		if (this.consumer == null)
		{
			super.println(x);
		}
		else
		{
			this.consumer.accept(x);
		}
	}

	public void setConsumer(Consumer<String> consumer)
	{
		this.consumer = consumer;
	}
}