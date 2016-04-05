////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import java.util.Arrays;

public class UIProxyJNA
{
    public static final String SEPARATOR = ",";
	private int[] id;

    public static void main(String[] args) {
        UIProxyJNA jna = new UIProxyJNA(new int[]{42,393658});
        System.out.println(jna.getIdString());
    }

	public UIProxyJNA(int[] id)
	{
		this.id = id;
	}

	public int[] getId()
	{
		return id;
	}

    public String getIdString() {
        if (this.id == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for (int i : this.id) {
            b.append(i).append(SEPARATOR);
        }
        return b.deleteCharAt(b.length() - 1).toString();
    }

    public long[] convertIdToLong() {
        long[] r = new long[this.id.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = id[i];
        }
        return r;
    }

    @Override
    public String toString() {
        return "UIProxyJNA{" +"id=" + Arrays.toString(id) +'}';
    }
}
