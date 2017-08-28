package org.easydevelop.mapreduce.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.easydevelop.mapreduce.strategy.provider.ReduceProvider;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/** 
* @author xudeyou 
*/
public class ReduceProviderManager<R, S> implements ReduceStrategy<R, S> {
	
	public static enum OnFutureExceptionOrTimeout{
		THROW_EXCEPTION,
		RETURN_NULL;
	}
	
	public static class ShardingGetException extends RuntimeException{

		private static final long serialVersionUID = 1L;

		public ShardingGetException(Throwable cause) {
			super(cause);
		}
	}
	
	private OnFutureExceptionOrTimeout onFutureExceptionOrTimeOut;
	private int futureTimeOutMills; 
	private List<ReduceProvider<R, S>> orderedReduceProviderList;
	public ReduceProviderManager(OnFutureExceptionOrTimeout onFutureExceptionOrTimeOut,int futureTimeOutMills,List<ReduceProvider<R, S>> reduceProviderList){
		this.onFutureExceptionOrTimeOut = onFutureExceptionOrTimeOut;
		this.futureTimeOutMills = futureTimeOutMills;
		orderedReduceProviderList = new ArrayList<>(reduceProviderList);
		AnnotationAwareOrderComparator.sort(orderedReduceProviderList);
	}
	
	
	protected List<S> getResults(List<Future<S>> shardingFutrueList){
		
		ArrayList<S> arrayList = new ArrayList<>(shardingFutrueList.size());
		for(Future<S> f: shardingFutrueList){
			S shardingResult = null;
				try {
					if(futureTimeOutMills == 0){
						shardingResult = f.get();
					} else {
						shardingResult = f.get(futureTimeOutMills, TimeUnit.MILLISECONDS);
					}
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					switch(onFutureExceptionOrTimeOut){
						case THROW_EXCEPTION:
							throw new ShardingGetException(e);
						case RETURN_NULL:
							//do nothing
							continue;
						default:
							throw new RuntimeException("Unkown choice!");
					}
				}
				arrayList.add(shardingResult);
		}
		
		return arrayList;
	}

	@Override
	public R reduce(List<Future<S>> shardingFutrueList, Class<S> shardingResultClass, Class<R> reduceResultClass) {
		
		List<S> results = this.getResults(shardingFutrueList);
		
		for(ReduceProvider<R, S> provider :orderedReduceProviderList){
			if(provider.support(shardingResultClass, reduceResultClass)){//TODO cache and speed up
				return provider.reduce(results,shardingResultClass, reduceResultClass);
			}
		}
		
		throw new RuntimeException("can not find suitable reduce provider,please add a provider or use a custom ReduceStrategy");
	}
	
	
	
}
