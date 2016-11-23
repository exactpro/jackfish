package com.exactprosystems.jf;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

public class Retranslator 
{
	public Retranslator(final int port) throws InterruptedException
	{
		logger.debug("MessagesSender(" + port + ")");
		
		this.sendThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					logger.debug("run sending()");
					
					ServerSocket welcomeSocket = new ServerSocket (port);
			
					while (Retranslator.this.running) 
					{
						Socket connectionSocket = welcomeSocket.accept();
			
						Retranslator.this.inFromClient = new DataInputStream(connectionSocket.getInputStream());
						Retranslator.this.outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
						try
						{
							while (Retranslator.this.running)
							{
								if (!Retranslator.this.messagesToSend.isEmpty())
								{
									if (Retranslator.this.running)
									{
										while (!Retranslator.this.messagesToSend.isEmpty())
										{
											byte[] msg = Retranslator.this.messagesToSend.poll();
											
											logger.debug("send: " + msg.length);
			
											Retranslator.this.outToClient.writeInt(msg.length);
											Retranslator.this.outToClient.write(msg);
										}
									}
								}
									Thread.sleep(10);
							}
						}
						catch(IOException ex)
						{
							ex.printStackTrace();
							logger.error(ex.getMessage(), ex);
						}
			
						connectionSocket.close();
						Retranslator.this.inFromClient 	= null;
						Retranslator.this.outToClient 	= null;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					logger.error(e.getMessage(), e);
				}
			}
		});
		this.sendThread.start();

		this.receiveThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					logger.debug("run receiving()");

					while (Retranslator.this.running)
					{
						if (Retranslator.this.inFromClient != null)
						{
							GatewayCommand command = GatewayCommand.receiveCommand(Retranslator.this.inFromClient);
							
							if (command != null)
							{
								switch (command.getCommandType())
								{
								case ClearAllowed:
									Retranslator.this.allowed.clear();
									break;
									
								case AddAllowed:
									Retranslator.this.allowed.add(command.getData());
									break;
									
								case ClearDisallowed:
									Retranslator.this.disallowed.clear();
									break;
									
								case AddDisallowed:
									Retranslator.this.disallowed.add(command.getData());
									break;
								}
							}
							else
							{
								logger.error("Unknown command: " + command);
							}
						}
						else
						{
							Thread.sleep(10);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					logger.error(e.getMessage(), e);
				}
			}
		});
		this.receiveThread.start();
	}
	
	public synchronized void stop() throws InterruptedException
	{
		logger.debug("stop()");
		if (this.sendThread != null && this.receiveThread != null)
		{
			this.running = false;

			this.sendThread.join(1000);
			this.receiveThread.join(1000);
			this.sendThread = null;
		}
	}
	
	public void sendMessage(byte[] message)
	{
		logger.debug("sendMessage(" +  DatatypeConverter.printHexBinary(message) + ")");
		if (!this.messagesToSend.offer(message))
		{
			logger.error("doesn't send a message");
		}
	}
	
	DataInputStream inFromClient = null;
	DataOutputStream outToClient = null;
	
	private volatile Set<String> allowed 		= new HashSet<String>();
	private volatile Set<String> disallowed 	= new HashSet<String>();
	
	private volatile Queue<byte[]> messagesToSend = new ConcurrentLinkedQueue<byte[]>();
	private Thread sendThread = null;
	private Thread receiveThread = null;

	private volatile boolean running = true; 

	private static final Logger logger = Logger.getLogger(Retranslator.class);
}

