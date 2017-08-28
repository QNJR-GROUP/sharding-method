package org.easydevelop.mapreduce.strategy.provider;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(219)
public class LongObjectReduceProvider extends AbstractReduceProvider {
	
	private Long defaultValue = 0l;

	@Override
	protected Object addToReduceResult(Object reduceCollection, Object shardingResult) {
		Long r = (Long) reduceCollection;
		Long s = (Long) shardingResult;
		r = r + s;
		return r;
	}

	@Override
	protected Class<?> getDefaultConcreteClass() {
		return Long.class;
	}

	@Override
	protected Class<?> getRootClass() {
		return Long.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return defaultValue;
	}
}
