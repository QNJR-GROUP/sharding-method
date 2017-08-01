package org.easydevelop.aggregation.strategy;

import java.util.List;
import java.util.concurrent.Future;


/**
 * 
 * @author deyou
 *
 * @param <F> for final aggregation result class
 * @param <S> for sharding result class
 */
public interface AggregationStrategy<F,S> {
	F aggregation(List<Future<S>> subFutrueList);
}
