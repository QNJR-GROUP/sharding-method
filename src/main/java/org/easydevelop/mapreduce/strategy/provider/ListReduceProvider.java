package org.easydevelop.mapreduce.strategy.provider;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(0)
public class ListReduceProvider extends AbstractReduceProvider {

	@SuppressWarnings("unchecked")
	@Override
	protected Object addToReduceResult(Object reduceCollection, Object shardingResult) {
		List<Object> r = (List<Object>) reduceCollection;
		List<Object> s = (List<Object>) shardingResult;
		r.addAll(s);
		return r;
	}

	@Override
	protected Class<?> getRootClass() {
		return List.class;
	}

	@Override
	protected Class<?> getDefaultConcreteClass() {
		return ArrayList.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Object createDefaultConcreteObject() {
		return new ArrayList();
	}

}
