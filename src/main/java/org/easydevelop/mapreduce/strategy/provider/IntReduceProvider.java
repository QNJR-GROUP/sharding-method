package org.easydevelop.mapreduce.strategy.provider;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(208)
public class IntReduceProvider extends AbstractReduceProvider {
	
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
		return int.class;
	}

	@Override
	protected Class<?> getRootClass() {
		return int.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return defaultValue;
	}
}
