package org.easydevelop.mapreduce.strategy.provider;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(10)
public class SetReduceProvider extends AbstractReduceProvider {

	@SuppressWarnings("unchecked")
	@Override
	protected Object addToReduceResult(Object reduceCollection, Object shardingResult) {
		Set<Object> r = (Set<Object>) reduceCollection;
		Set<Object> s = (Set<Object>) shardingResult;
		r.addAll(s);
		return r;
	}

	@Override
	protected Class<?> getRootClass() {
		return Set.class;
	}

	@Override
	protected Class<?> getDefaultConcreteClass() {
		return HashSet.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return new HashSet<>();
	}

}
