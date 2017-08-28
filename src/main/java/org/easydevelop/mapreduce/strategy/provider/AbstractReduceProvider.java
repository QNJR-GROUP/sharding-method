package org.easydevelop.mapreduce.strategy.provider;

import java.util.List;

import org.easydevelop.common.ReflectHelper;

/** 
* @author xudeyou 
*/
public abstract class AbstractReduceProvider implements ReduceProvider<Object,Object> {
	
	@Override
	public Object reduce(List<Object> shardingResultList, Class<Object> shardingResultClass, Class<Object> reduceResultClass) {
		
		Object reduceCollection = createReduceClassObject(reduceResultClass);
		for(Object shardingResult :shardingResultList){
			reduceCollection = addToReduceResult(reduceCollection, shardingResult);
		}
		return reduceCollection;
	}

	protected abstract Object addToReduceResult(Object reduceCollection, Object shardingResult);

	@Override
	public boolean support(Class<?> shardingResultClass, Class<?> reduceResultClass) {
		if (getRootClass().isAssignableFrom(shardingResultClass)
				&& getRootClass().isAssignableFrom(reduceResultClass)
				&& (createReduceClassObject(reduceResultClass) != null)
				) {
			return true;
		}
		return false;
	}
	
	protected Object createReduceClassObject(Class<?> reduceResultClass) {
		if(ReflectHelper.canInitInstanceWithNoParameter(reduceResultClass)){
			return ReflectHelper.createInstance(reduceResultClass);
		}else{
			if(reduceResultClass.isAssignableFrom(getDefaultConcreteClass())){
				return createDefaultConcreteObject();
			}else{
				return null;
			}
		}
	}

	protected abstract Class<?> getDefaultConcreteClass();
	
	protected abstract Object createDefaultConcreteObject();

	protected abstract Class<?> getRootClass();



}
