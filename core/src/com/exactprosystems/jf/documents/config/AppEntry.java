////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class AppEntry extends Entry
{
	@XmlElement(name = Configuration.appDescription)
	protected String descriptionValue;
	
	@XmlElement(name = Configuration.appDicPath)
	protected String appDicPathValue;
	
	@XmlElement(name = Configuration.appJar)
	protected String appJarNameValue;

	@XmlElement(name = Configuration.appWorkDir)
	protected String appWorkDirValue;
	
	@XmlElement(name = Configuration.appStartPort)
	protected String appStartPortValue;

	@Override
	protected String getDerived(String name) throws Exception
	{
		switch (name)
		{
			case Configuration.appDescription: 	return this.descriptionValue;
			case Configuration.appDicPath: 		return this.appDicPathValue;
			case Configuration.appJar: 			return this.appJarNameValue;
			case Configuration.appWorkDir: 		return this.appWorkDirValue;
			case Configuration.appStartPort:		return this.appStartPortValue;
		}
		return null;
	}

	@Override
	protected void setDerived(String name, Object value) throws Exception
	{
		switch (name)
		{
			case Configuration.appDescription: 	this.descriptionValue	= "" + value;	return;
			case Configuration.appDicPath: 		this.appDicPathValue	= "" + value;	return;
			case Configuration.appJar: 			this.appJarNameValue	= "" + value;	return;
			case Configuration.appWorkDir: 		this.appWorkDirValue	= "" + value;	return;
			case Configuration.appStartPort:	this.appStartPortValue	= "" + value;	return;
		}
	}
}