////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.vars;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.LinkedProperties;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

@DocumentInfo(
        kind = DocumentKind.SYSTEM_VARS,
        newName = "NewVars",
		extentioin = "ini",
		description = "System variables"
)
public class SystemVars extends AbstractDocument
{
    private Parameters parameters;

    public SystemVars(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);

		parameters = new Parameters();
	}

	//------------------------------------------------------------------------------------------------------------------
	// interface Document
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public void load(Reader reader) throws Exception
	{
		try (BufferedReader br = new BufferedReader(reader))
		{
			String line = null;
			this.parameters.clear();
			String lastDescription = null;
			while ((line = br.readLine()) != null)
			{
				if (line.isEmpty())
				{
					continue;
				}
				if (line.trim().startsWith("#"))
				{
					lastDescription = line.substring(1);
					continue;
				}

				int firstEq = line.indexOf('=');
				if (firstEq >= 0)
				{
					this.parameters.add(line.substring(0, firstEq), line.substring(firstEq + 1));
				}
				else
				{
					this.parameters.add(line, null);
				}
				if (lastDescription != null)
				{
					this.parameters.getByIndex(this.parameters.size() - 1).setDescription(lastDescription);
					lastDescription = null;
				}
			}
		}
	}

	@Override
	public boolean canClose() throws Exception
	{
		return true;
	}

    @Override
    public void save(String fileName) throws Exception
    {
        super.save(fileName);

        LinkedProperties prop = new LinkedProperties();
        this.parameters.forEach(parameter ->
        {
            String name = "";
            if (!Str.IsNullOrEmpty(parameter.getDescription()))
            {
                name = "#" + parameter.getDescription() + System.lineSeparator();
            }
            prop.put(name + parameter.getName(), parameter.getExpression());
        });
        prop.store(new FileWriter(fileName), null);
    }

	//------------------------------------------------------------------------------------------------------------------
	// interface Mutable
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		return this.parameters.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.parameters.saved();
	}

	//------------------------------------------------------------------------------------------------------------------
	public void setParameters(List<Parameter> parameters)
	{
		this.parameters.clear();
		parameters.stream()
				.collect(Collectors.toMap(Parameter::getName, Parameter::getExpression))
				.forEach(this.parameters::add);
	}

	public Parameters getParameters()
	{
		return this.parameters;
	}

	public void injectVariables(AbstractEvaluator evaluator) throws Exception
	{
		for (Parameter entry : this.parameters)
		{
			String key = entry.getName();
			String value = entry.getExpression();
			Object result = evaluator.evaluate(value);

			evaluator.init(key, result);
		}

	}
}
