package org.easydevelop.mapreduce.strategy.provider;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(228)
public class DoubleBasicReduceProvider extends AbstractReduceProvider {
	
	private Double defaultValue = 0.0;

	@Override
	protected Object addToReduceResult(Object reduceCollection, Object shardingResult) {
		Double r = (Double) reduceCollection;
		Double s = (Double) shardingResult;
		r = r + s;
		return r;
	}

	@Override
	protected Class<?> getDefaultConcreteClass() {
		return double.class;
	}

	@Override
	protected Class<?> getRootClass() {
		return double.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return defaultValue;
	}
}
