/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
		extension = "ini",
		description = "System variables"
)
public class SystemVars extends AbstractDocument
{
	private final Parameters parameters;

	public SystemVars(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
		this.parameters = new Parameters();
	}

	//region AbstractDocument
	@Override
	public void load(Reader reader) throws Exception
	{
		try (BufferedReader br = new BufferedReader(reader))
		{
			this.parameters.clear();
			String lastDescription = null;
			String line;
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
			this.parameters.evaluateAll(super.getFactory().createEvaluator());
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
		LinkedProperties prop = new LinkedProperties();
		this.parameters.forEach(parameter -> {
			String name = "";
			if (!Str.IsNullOrEmpty(parameter.getDescription()))
			{
				name = "#" + parameter.getDescription() + System.lineSeparator();
			}
			prop.put(name + parameter.getName(), parameter.getExpression());
		});
		prop.store(new FileWriter(fileName), null);
		super.save(fileName);
	}

	@Override
	protected void afterRedoUndo()
	{
		this.parameters.evaluateAll(super.getFactory().createEvaluator());
	}

	//endregion

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.parameters.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.parameters.saved();
	}

	//endregion

	/**
	 * Replace the old parameter to the new passed parameters
	 */
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

	/**
	 * Inject the parameters to the passed evaluator.
	 *
	 * @throws Exception if some of a parameter from the parameters are invalid
	 */
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
