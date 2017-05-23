package com.exactprosystems.jf.tool.custom;

public interface SupplierWithException<T>
{
	T get() throws Exception;
}
