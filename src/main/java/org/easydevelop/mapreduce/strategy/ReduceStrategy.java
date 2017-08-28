package org.easydevelop.mapreduce.strategy;

import java.util.List;
import java.util.concurrent.Future;


/**
 * 
 * @author deyou
 *
 * @param <R> for final aggregation result class
 * @param <S> for sharding result class
 */
public interface ReduceStrategy<R,S> {
	R reduce(List<Future<S>> shardingFutrueList,Class<S> shardingResultClass,Class<R> reduceResultClass);
}
