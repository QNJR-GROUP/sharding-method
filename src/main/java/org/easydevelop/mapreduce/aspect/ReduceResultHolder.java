package org.easydevelop.mapreduce.aspect;

import java.util.Arrays;
import java.util.List;

/** 
* when using Aggregation,if your aggregation result type different from sharding result type, 
* you can place this instance in the method parameter and set the method's return type to void
* and then the aggregation result will return by getResult()
*/
public class ReduceResultHolder<R,S> {
	
	private Class<R> reduceResultClass;
	private Class<S> shardingResultClass;
	
	public ReduceResultHolder(Class<R> reduceResultClass, Class<S> shardingResultClass) {
		super();
		this.reduceResultClass = reduceResultClass;
		this.shardingResultClass = shardingResultClass;
	}
	
	public Class<R> getReduceResultClass() {
		return reduceResultClass;
	}

	public Class<S> getShardingResultClass() {
		return shardingResultClass;
	}

	private ThreadLocal<Integer> shardingResultPosition = new ThreadLocal<>();
	private ThreadLocal<Boolean> isResultSet = new ThreadLocal<>();
	
//	private ArrayList<S> shardingResults;
	private Object[] shardingResults;
	
	private R result;
	
	public R getResult(){
		return result;
	}
	
	public void setShardingResult(S obj){
		Integer pos = shardingResultPosition.get();
		shardingResults[pos] = obj;
		isResultSet.set(true);
	}
	
	@SuppressWarnings("unchecked")
	public List<S> getShardingResults(){
		return (List<S>) Arrays.asList(shardingResults);
	}
	
	@SuppressWarnings("unchecked")
	void setResult(Object result){
		this.result = (R) result;
	}
	
	void setShardingResultPosition(Integer position){
		shardingResultPosition.set(position);
	}
	
	void init(int ShardingCount){
		shardingResults = new Object[ShardingCount];
	}
	
	boolean isShardingResultSet(){
		Boolean set = isResultSet.get();
		if(set == null || !set){
			return false;
		}else{
			return true;
		}
	}
	
}
