package com.exactprosystems.jf.api.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginDescription {
    String description() default "";
    String additionalDescription() default "";
    String any() default "";
}
