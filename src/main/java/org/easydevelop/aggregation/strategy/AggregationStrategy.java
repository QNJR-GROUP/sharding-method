package org.easydevelop.aggregation.strategy;

import java.util.List;

/** 
* @author xudeyou 
*/
public interface AggregationStrategy {
	
	String getStrategyName();
	
	Object aggregation(List<Object> subFutrueList);

}
