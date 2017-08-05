package org.easydevelop.mapreduce.strategy;

import java.util.List;
import java.util.concurrent.Future;


/**
 * 
 * @author deyou
 *
 * @param <F> for final aggregation result class
 * @param <S> for sharding result class
 */
public interface ReduceStrategy<F,S> {
	F reduce(List<Future<S>> shardingFutrueList);
}
