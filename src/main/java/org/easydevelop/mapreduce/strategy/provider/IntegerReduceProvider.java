package org.easydevelop.mapreduce.strategy.provider;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(209)
public class IntegerReduceProvider extends AbstractReduceProvider {
	
	private Integer defaultValue = 0;

	@Override
	protected Object addToReduceResult(Object reduceCollection, Object shardingResult) {
		Integer r = (Integer) reduceCollection;
		Integer s = (Integer) shardingResult;
		r = r + s;
		return r;
	}

	@Override
	protected Class<?> getDefaultConcreteClass() {
		return Integer.class;
	}

	@Override
	protected Class<?> getRootClass() {
		return Integer.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return defaultValue;
	}
}
