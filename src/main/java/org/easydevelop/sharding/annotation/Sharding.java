package org.easydevelop.sharding.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * to declare some methods in the class should be sharding
 * @author deyou
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Sharding {
	
	/**
	 * declare which parameter(s) should use for sharding judgment by default.
	 * SpEL expressions
	 * ex: 
	 * void test(String str1,Integer id) : defaultKeyNames={"['id']"}
	 * void test(String str1,Order order) : defaultKeyNames={"['order'].id"}
	 * @return
	 */
	String[] defaultKeyEls() default {};
	
	/**
	 * SpEl referent to a instance that implements ShardingStrategy
	 * @return
	 */
	String defaultStrategy() default "";
	
	/**
	 * declare the default sharding data-sources
	 * @return
	 */
	String defaultDsSet() default "";
}
