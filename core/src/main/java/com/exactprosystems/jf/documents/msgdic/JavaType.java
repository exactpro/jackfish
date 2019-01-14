/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.documents.msgdic;

import com.exactprosystems.jf.api.client.IType;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;


@XmlType(name = "JavaType")
@XmlEnum
public enum JavaType implements IType
{
	@XmlEnumValue("java.lang.Boolean")
	JAVA_LANG_BOOLEAN(Boolean.class),
	@XmlEnumValue("java.lang.Short")
	JAVA_LANG_SHORT(Short.class),
	@XmlEnumValue("java.lang.Integer")
	JAVA_LANG_INTEGER(Integer.class),
	@XmlEnumValue("java.lang.Long")
	JAVA_LANG_LONG(Long.class),
	@XmlEnumValue("java.lang.Byte")
	JAVA_LANG_BYTE(Byte.class),
	@XmlEnumValue("java.lang.Float")
	JAVA_LANG_FLOAT(Float.class),
	@XmlEnumValue("java.lang.Double")
	JAVA_LANG_DOUBLE(Double.class),
	@XmlEnumValue("java.lang.String")
	JAVA_LANG_STRING(String.class),
//	@XmlEnumValue("java.util.Date")
//	JAVA_UTIL_DATE(Date.class),
	@XmlEnumValue("java.lang.Character")
	JAVA_LANG_CHARACTER(Character.class),
	@XmlEnumValue("java.math.BigDecimal")
	JAVA_MATH_BIG_DECIMAL(BigDecimal.class),
	@XmlEnumValue("java.time.LocalDateTime")
	JAVA_TIME_LOCAL_DATE_TIME(LocalDateTime.class),
	@XmlEnumValue("java.time.LocalDate")
	JAVA_TIME_LOCAL_DATE(LocalDate.class),
	@XmlEnumValue("java.time.LocalTime")
	JAVA_TIME_LOCAL_TIME(LocalTime.class)
	;

	private final Class<?> type;

	JavaType(Class<?> type)
	{
		this.type = type; 
	}

	@Override
	public Class<?> getJavaClass()
	{
		return this.type;
	}

	public static JavaType fromValue(String v)
	{
		return Arrays.stream(JavaType.values())
				.filter(type -> type.type.getCanonicalName().equals(v))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(v));
	}
}
