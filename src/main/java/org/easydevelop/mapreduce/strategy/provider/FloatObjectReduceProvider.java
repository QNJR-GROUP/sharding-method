package org.easydevelop.mapreduce.strategy.provider;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(239)
public class FloatObjectReduceProvider extends AbstractReduceProvider {
	
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
		return Float.class;
	}

	@Override
	protected Class<?> getRootClass() {
		return Float.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return defaultValue;
	}
}
