package org.easydevelop.mapreduce.strategy.provider;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(238)
public class FloatBasicReduceProvider extends AbstractReduceProvider {
	
	private Float defaultValue = 0.0f;

	@Override
	protected Object addToReduceResult(Object reduceCollection, Object shardingResult) {
		Float r = (Float) reduceCollection;
		Float s = (Float) shardingResult;
		r = r + s;
		return r;
	}

	@Override
	protected Class<?> getDefaultConcreteClass() {
		return float.class;
	}

	@Override
	protected Class<?> getRootClass() {
		return float.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return defaultValue;
	}
}
