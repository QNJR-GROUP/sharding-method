package org.easydevelop.sharding.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.easydevelop.sharding.annotation.ShardingContext;
import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Aspect
@Order(-3)
public class ShardingAspect {
	
	private static ThreadLocal<ShardingContext> shardingContext = new ThreadLocal<>();
	
	public static ShardingContext getShardingContext() {
		return shardingContext.get();
	}
	


	@Around("@within(shardingContext)")
	public Object around(ProceedingJoinPoint jp,ShardingContext shardingContext) throws Throwable{
		
		//为带有ShardingContext注解类的方法调用时带上ShardingContext注解
		ShardingContext lastShardingContext = ShardingAspect.shardingContext.get();
		
		try {
			ShardingAspect.shardingContext.set(shardingContext);
			return jp.proceed();
		} catch (Throwable e) {
			throw e;
		} finally{
			ShardingAspect.shardingContext.set(lastShardingContext);
		}
	}
}
