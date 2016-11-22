package com.exactprosystems.jf;

public enum OrderSide 
{
	BUY('B'), 
	SELL('S');
	
	OrderSide (char ch)
	{
		this.value = ch;
	}
	
	public char getChar()
	{
		return this.value;
	}
	
	private char value;
}
