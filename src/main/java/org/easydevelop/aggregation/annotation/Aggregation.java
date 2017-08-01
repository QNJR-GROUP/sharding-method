package org.easydevelop.aggregation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Aggregation {
	
	/**
	 * SpEl referent to a instance that implements AggregationStrategy
	 * @return
	 */
	String defaultStrategy() default "";

	String defaultDsSet() default "";
}
