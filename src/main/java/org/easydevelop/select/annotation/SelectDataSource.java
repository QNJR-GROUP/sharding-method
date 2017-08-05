package org.easydevelop.select.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * to declare the method should be sharding
 * @author deyou
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectDataSource {
	
	/**
	 * declare which parameter(s) should use for sharding judgment.
	 * if not specified,use the default values declare in the class
	 * SpEL expressions
	 * ex: 
	 * void test(String str1,Integer id) : defaultKeyNames={"['id']"}
	 * void test(String str1,Order order) : defaultKeyNames={"['order'].id"}
	 * @return
	 */
	String[] keyNameEls() default {};
	
	/**
	 * SpEl referent to a instance that implements ShardingStrategy
	 * @return
	 */
	String strategy() default "";
	
	/**
	 * declare the sharding data-sources
	 * if not specified,use the default values declare in the class
	 * @return
	 */
	String dsSet() default "";
}
