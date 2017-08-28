package org.easydevelop.mapreduce.strategy.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.core.annotation.Order;

/** 
* @author xudeyou 
*/
@Order(99)
public class CollectionReduceProvider extends AbstractReduceProvider {

	@SuppressWarnings("unchecked")
	@Override
	protected Object addToReduceResult(Object reduceCollection, Object shardingResult) {
		Collection<Object> r = (Collection<Object>) reduceCollection;
		Collection<Object> s = (Collection<Object>) shardingResult;
		r.addAll(s);
		return r;
	}

	@Override
	protected Class<?> getRootClass() {
		return Collection.class;
	}

	@Override
	protected Class<?> getDefaultConcreteClass() {
		return ArrayList.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return new ArrayList<>();
	}

}
