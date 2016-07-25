////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error;

public enum ErrorKind
{
	EXCEPTION				("Exception"),
	EXPRESSION_ERROR		("Expression error"),
	ASSERT					("Assert is false"), 
	EMPTY_PARAMETER			("Empty parameter"), 
	WRONG_PARAMETERS		("Wrong parameters"),
	LOCATOR_NOT_FOUND		("Locator is not found in a dictionary"),
	DIALOG_NOT_FOUND		("Dialog is not found"),
	ELEMENT_NOT_FOUND		("Element is not found"),
	FEATURE_NOT_SUPPORTED	("Feature is not supported"),
	OPERATION_NOT_ALLOWED	("Operation is not allowed"),
	OPERATION_FAILED		("Operation failed"), 
	NOT_EQUAL				("Actual and expected are missmatched"), 
	SQL_ERROR				("SQL error"),
	SERVICE_ERROR			("Service error"),
	APPLICATION_ERROR		("Application error"), 
	CLIENT_ERROR			("Client error"),
	TIMEOUT					("Timeout"),
	MANY_ERRORS				("Many errors"),
	FAIL					("Fail"), 
	OTHER					("Other"), 
	;

	
	ErrorKind(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	private String name;
}
