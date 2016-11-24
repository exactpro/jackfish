package com.exactprosystems.jf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;

public class GatewayCommand
{
	public GatewayCommand(GatewayCommandType commandType, String data)
	{
		this.commandType = commandType;
		this.data = data;
	}
	
	@Override
	public String toString()
	{
		return "GatewayCommand [commandType=" + this.commandType + ", data=" + (this.data == null ? "[]" : Arrays.toString(this.data.getBytes())) + "]";
	}

	public void sendCommand(DataOutputStream out) throws Exception
	{
		logger.debug("sendCommand() command=" + this.toString());

		out.writeByte(this.commandType.getCommand());

		if (this.data == null)
		{
			out.writeByte(0);
		}
		else
		{
			out.writeByte(this.data.length());
			out.writeBytes(this.data);
		}
	}
	
	public static GatewayCommand receiveCommand(DataInputStream in) throws Exception
	{
		logger.debug("receiveCommand()");
		
		String data = null;
		
		byte command = in.readByte();
		GatewayCommandType commandType = GatewayCommandType.byCommand(command);
		if (commandType != null)
		{
			byte[] buff = new byte[256];
			int dataReaded = 0;
			
			int dataLength = in.readByte();
			if (dataLength > 0)
			{
				dataReaded = in.read(buff, 0, Math.min(buff.length, dataLength));
//				data = AbstractClient.convertToString(buff, dataReaded); // TODO: 
			}
			
			if (dataReaded != dataLength)
			{
				throw new Exception("wrong data. expected " + dataLength + " actual readed " + dataReaded);
			}
		}

		GatewayCommand ret = new GatewayCommand(commandType, data);
		logger.debug("command received: " + ret.toString());
		
		
		return ret;
	}
	
	public GatewayCommandType getCommandType()
	{
		return this.commandType;
	}
	
	public String getData()
	{
		return this.data;
	}
	

	GatewayCommandType commandType = null;
	String data = null;

	private static final Logger logger = Logger.getLogger(GatewayCommand.class);
}
