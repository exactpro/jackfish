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
}
