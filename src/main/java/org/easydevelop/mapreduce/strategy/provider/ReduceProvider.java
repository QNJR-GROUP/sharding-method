package org.easydevelop.mapreduce.strategy.provider;

import java.util.List;


/**
 * 
 * @author deyou
 *
 * @param <R> for final aggregation result class
 * @param <S> for sharding result class
 */
public interface ReduceProvider<R,S> {
	R reduce(List<S> shardingResultList,Class<S> shardingResultClass,Class<R> reduceResultClass);
	boolean support(Class<?> shardingResultClass,Class<?> reduceResultClass);
}
