package org.easydevelop.mapreduce.strategy.provider;

import java.util.Map;

import org.springframework.core.annotation.Order;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

/** 
* @author xudeyou 
*/
@Order(199)
public class KeyValueMapReduceProvider extends AbstractReduceProvider {

	@SuppressWarnings("unchecked")
	@Override
	protected Object addToReduceResult(Object reduceCollection, Object shardingResult) {
		Map<Object,Object> r = (Map<Object,Object>) reduceCollection;
		Map<Object,Object> s = (Map<Object,Object>) shardingResult;
		r.putAll(s);
		return r;
	}


	@Override
	protected Class<?> getRootClass() {
		return Map.class;
	}
	
	@Override
	protected Class<?> getDefaultConcreteClass() {
		return ConcurrentHashMap.class;
	}

	@Override
	protected Object createDefaultConcreteObject() {
		return new ConcurrentHashMap<>();
	}


}
