package org.easydevelop.keygenerator.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KeyGenerate {

	/**
	 * SpEL expressions
	 * ex: 
	 * void test(String str1,Integer id) : defaultKeyNames={"['id']"}
	 * void test(String str1,Order order) : defaultKeyNames={"['order'].id"}
	 * @return
	 */
	String[] defaultKeyEls() default {};

	/**
	 * SpEl referent to a instance that implements KeyGenerateStrategy
	 * @return
	 */
	String defaultStrategy() default "";
	
	/**
	 * -1 not allowd
	 * 0 not defined
	 * 1 allowd
	 * @return
	 */
	int allowCallerDefinedKeyValue() default -1;
	
	/**
	 * provide more information for KeyGenerateStrategy
	 * 
	 * SpEL expressions
	 * ex: 
	 * void test(String str1,Integer id) : defaultKeyNames={"['id']"}
	 * void test(String str1,Order order) : defaultKeyNames={"['order'].id"}
	 * @return
	 */
	String[] strategyMetadataEls() default {};
}
