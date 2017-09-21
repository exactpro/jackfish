package com.exactprosystems.jf.api.app;

import java.util.Set;

public abstract class AbstractApplicationFactory implements IApplicationFactory
{
	public static final String trimTextName = "TrimText";

	private IGuiDictionary dictionary = null;

	//region IApplicationFactory
	@Override
	public void init(IGuiDictionary dictionary)
	{
		this.dictionary = dictionary;
	}

	@Override
	public IGuiDictionary getDictionary()
	{
		return this.dictionary;
	}

	@Override
	public boolean isAllowed(ControlKind kind, OperationKind operation)
	{
		return getInfo().isAllowed(kind, operation);
	}

	@Override
	public boolean isSupported(ControlKind kind)
	{
		return getInfo().isSupported(kind);
	}

	@Override
	public Set<ControlKind> supportedControlKinds()
	{
		return getInfo().supportedControlKinds();
	}

	//endregion

	public final String createHelp()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.introduction())
				.append(this.knownParameters())
				.append(this.supportedControls());
		for (String info : this.additionalInfo())
		{
			sb.append(info);
		}



		return sb.toString();
	}

	//TODO need be abstract, override this
	protected String introduction()
	{
		return "";
	}

	//TODO need be abstract, override this
	protected String knownParameters()
	{
		return "";
	}

	//TODO need be abstract, override this
	protected String supportedControls()
	{
		return "";
	}

	//TODO need be abstract, override this
	protected String[] additionalInfo()
	{
		return new String[]{""};
	}


	/*
	Help structure

	1. Introduction. (from file or annotation. Annotation preferred)
	2. Known parameters, with example and for what this parameters need. e.g. for web plugin, describe supported browsers. ( generated )
	3. Supported controls; ( generated)
	4. Additional info ( list of info's) . e.g. for web plugin, describe that ApplicationMove can't move window. (from file or annotation. Annotation is preferred)
	5. Any one another info
	*/
}
