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
public @interface ShardingContext {
	
	/**
	 * declare the default sharding data-sources
	 * @return
	 */
	String dataSourceSet() default "";
	
	
	/**
	 * SpEl referent to a instance that implements ShardingStrategy
	 * @return
	 */
	String shardingStrategy() default "";
	
	/**
	 * declare which parameter(s) should use for sharding judgment by default.
	 * SpEL expressions
	 * ex: 
	 * void test(String str1,Integer id) : defaultKeyNames={"['id']"}
	 * void test(String str1,Order order) : defaultKeyNames={"['order'].id"}
	 * @return
	 */
	String[] shardingKeyEls() default {};

	/**
	 * SpEl referent to a instance that implements GenerateKeyStrategy
	 * @return
	 */
	String generateIdStrategy() default "";
	
	/**
	 * SpEL expressions
	 * ex: 
	 * void test(String str1,Integer id) : defaultKeyNames={"['id']"}
	 * void test(String str1,Order order) : defaultKeyNames={"['order'].id"}
	 * @return
	 */
	String[] generateIdEls() default {};
	
	/**
	 * 当传入的参数的ID对应的位置已经有值，是否允许使用传入来的值作为ID
	 * -1 not allowd
	 * 0 not defined
	 * 1 allowd
	 * @return
	 */
	int generateIdByCaller() default -1;
	
	/**
	 * provide more information for KeyGenerateStrategy
	 * 
	 * SpEL expressions
	 * ex: 
	 * void test(String str1,Integer id) : defaultKeyNames={"['id']"}
	 * void test(String str1,Order order) : defaultKeyNames={"['order'].id"}
	 * @return
	 */
	String[] generateIdMetadataEls() default {};
	
	/**
	 * SpEl referent to a instance that implements ReduceStrategy
	 * @return
	 */
	String reduceStrategy() default "";
}
