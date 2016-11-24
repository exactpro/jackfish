package com.exactprosystems.jf;

public enum GatewayCommandType
{
	ClearAllowed	((byte)0), 
	AddAllowed		((byte)1), 
	ClearDisallowed	((byte)2), 
	AddDisallowed	((byte)3);
	
	GatewayCommandType(byte command)
	{
		this.command = command;
	}
	
	public static GatewayCommandType byCommand(int command)
	{
		for(GatewayCommandType value : values())
		{
			if(value.getCommand() == command)
			{
				return value;
			}
		}
		return null;
	}

	public byte getCommand()
	{
		return this.command;
	}
	
	private byte command;
}
