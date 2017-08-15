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
import javax.xml.bind.annotation.XmlRootElement;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;

@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class ConfigurationBean implements Mutable
{
    public static final Class<?>[] jaxbContextClasses =
        {
            ConfigurationBean.class,
            SqlEntry.class,
            ClientEntry.class,
            ServiceEntry.class,
            AppEntry.class,
            GlobalHandler.class,
        };

    
    @XmlElement(name = Configuration.time)
    protected MutableString timeValue;

    @XmlElement(name = Configuration.date)
    protected MutableString dateValue;

    @XmlElement(name = Configuration.dateTime)
    protected MutableString dateTimeValue;

    @XmlElement(name = Configuration.formats)
    protected MutableArrayList<MutableString> formatsValue;

    @XmlElement(name = Configuration.reports)
    protected MutableString reportsValue;

    @XmlElement(name = Configuration.version)
    protected MutableString versionValue;

    @XmlElement(name = Configuration.imports)
    protected MutableArrayList<MutableString> importsValue;

    @XmlElement(name = Configuration.vars)
    protected MutableString varsValue;

    @XmlElement(name = Configuration.userVars)
    protected MutableArrayList<MutableString> userVarsValue;

    @XmlElement(name = Configuration.matrix)
    protected MutableArrayList<MutableString> matricesValue;

    @XmlElement(name = Configuration.appDict)
    protected MutableArrayList<MutableString> appDictionariesValue;

    @XmlElement(name = Configuration.clientDict)
    protected MutableArrayList<MutableString> clientDictionariesValue;

    @XmlElement(name = Configuration.library)
    protected MutableArrayList<MutableString> librariesValue;

    @XmlElement(name = Configuration.globalHandler)
    public GlobalHandler globalHandlerValue;

    @XmlElement(name = Configuration.sqlEntry)
    public MutableArrayList<SqlEntry> sqlEntriesValue;

    @XmlElement(name = Configuration.clientEntry)
    public MutableArrayList<ClientEntry> clientEntriesValue;

    @XmlElement(name = Configuration.serviceEntry)
    public MutableArrayList<ServiceEntry> serviceEntriesValue;

    @XmlElement(name = Configuration.appEntry)
    public MutableArrayList<AppEntry> appEntriesValue;


    public ConfigurationBean()
    {
        this.timeValue                  = new MutableString();
        this.dateValue                  = new MutableString();
        this.dateTimeValue              = new MutableString();
        this.formatsValue               = new MutableArrayList<MutableString>();
        this.reportsValue               = new MutableString();
        this.versionValue               = new MutableString();

        this.globalHandlerValue         = new GlobalHandler();

        this.sqlEntriesValue            = new MutableArrayList<SqlEntry>();
        this.clientEntriesValue         = new MutableArrayList<ClientEntry>();
        this.serviceEntriesValue        = new MutableArrayList<ServiceEntry>();
        this.appEntriesValue            = new MutableArrayList<AppEntry>();
        this.importsValue               = new MutableArrayList<MutableString>();
        this.varsValue                  = new MutableString();
        this.userVarsValue              = new MutableArrayList<MutableString>();
        this.matricesValue              = new MutableArrayList<MutableString>();
        this.appDictionariesValue       = new MutableArrayList<MutableString>();
        this.clientDictionariesValue    = new MutableArrayList<MutableString>();
        this.librariesValue             = new MutableArrayList<MutableString>();
    }


    @Override
    public boolean isChanged()
    {
        return this.timeValue.isChanged()
                || this.dateValue.isChanged()
                || this.dateTimeValue.isChanged()
                || this.formatsValue.isChanged()
                || this.versionValue.isChanged()
                || this.reportsValue.isChanged()
                || this.sqlEntriesValue.isChanged()
                || this.clientEntriesValue.isChanged()
                || this.serviceEntriesValue.isChanged()
                || this.appEntriesValue.isChanged()
                || this.importsValue.isChanged()
                || this.varsValue.isChanged()
                || this.userVarsValue.isChanged()
                || this.matricesValue.isChanged()
                || this.appDictionariesValue.isChanged()
                || this.clientDictionariesValue.isChanged()
                || this.librariesValue.isChanged();
    }


    @Override
    public void saved()
    {
        this.timeValue.saved();
        this.dateValue.saved();
        this.dateTimeValue.saved();
        this.formatsValue.saved();
        this.versionValue.saved();
        this.reportsValue.saved();
        this.appEntriesValue.saved();
        this.clientEntriesValue.saved();
        this.serviceEntriesValue.saved();
        this.sqlEntriesValue.saved();
        this.importsValue.saved();
        this.varsValue.saved();
        this.userVarsValue.saved();
        this.matricesValue.saved();
        this.appDictionariesValue.saved();
        this.clientDictionariesValue.saved();
        this.librariesValue.saved();
    }
    
}