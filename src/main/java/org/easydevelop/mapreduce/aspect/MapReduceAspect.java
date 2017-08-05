package org.easydevelop.mapreduce.aspect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.easydevelop.common.SpElHelper;
import org.easydevelop.mapreduce.annotation.MapReduce;
import org.easydevelop.mapreduce.strategy.ReduceStrategy;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.easydevelop.sharding.annotation.ShardingContext;
import org.easydevelop.sharding.aspect.ShardingAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

/** 
* @author xudeyou 
*/
@Aspect
@Order(-1)
public class MapReduceAspect {
	
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	@Value("${sharding.method.aggregation.timeout.seconds:10}")
	private int timeout;
	
	@Autowired
	private ShardingRoutingDataSource routingDataSource;
	
	@Autowired
	private SpElHelper spElHelper;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Around("@annotation(aggregationMethod)")
	public Object around(ProceedingJoinPoint jp,MapReduce aggregationMethod) throws Throwable{
		
		//get Sharding annotation from method's class
		ShardingContext aggregation = ShardingAspect.getShardingContext();
		if(aggregation == null){
			throw new RuntimeException("Class annotation config error,can not find Sharding Annotation!");
		}
		
		//get configurations
		String strategy = getFinalStrategy(aggregationMethod, aggregation);
		String dsSetKey = getDsSetKey(aggregationMethod, aggregation);
		
		//check whether the sharding dataSource already set
		String currentDsSet = routingDataSource.getCurrentLookupDsSet();
		Integer currentLookupDsSequence = routingDataSource.getCurrentLookupDsSequence();
		if(currentDsSet != null || currentLookupDsSequence != null){
			throw new RuntimeException("should not execute this method in another transaction!");
		}
		
		//check result return type
		MethodSignature signature = (MethodSignature) jp.getSignature();
		Class[] parameterTypes = signature.getParameterTypes();
		ReduceResultHolder holder = null;
		boolean returnByResultHolder = false;
		for(int i = 0; i < parameterTypes.length; i++){
			Class<?> clazz = parameterTypes[i];
			if(ReduceResultHolder.class.isAssignableFrom(clazz)){
				holder = (ReduceResultHolder) jp.getArgs()[i];
				returnByResultHolder = true;
				break;
			}
		}
		
		if(holder == null && returnByResultHolder){
			throw new RuntimeException("you must pass in the ResultHolder instance!");
		}
		
		Class returnType = signature.getReturnType();
		if(returnType != void.class && returnByResultHolder){
			throw new RuntimeException("you can only choose to return with RetunValue or with the ResultHolder");
		}
		
		//generate Callable objects
		List<DataSource> listDataSource = routingDataSource.getDataSourcesByDsSetName(dsSetKey);
		List<Future<Object>> listFuture = new ArrayList<>(listDataSource.size());
		final ReduceResultHolder finalHolder = holder;
		if(finalHolder != null){
			finalHolder.init(listDataSource.size());
		}
		
		for(int i = 0; i < listDataSource.size(); i++){
			final int k = i;
			Callable<Object> callable = new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					if(finalHolder != null){
						finalHolder.setShardingResultPosition(k);
					}
					routingDataSource.setCurrentLookupKey(dsSetKey, k);
					try {
						Object proceed = jp.proceed();
						
						if(finalHolder != null && !finalHolder.isShardingResultSet()){
							throw new RuntimeException("you should set the sharding result! Even though it's null!");
						}
						
						return proceed;
					} catch (Exception e) {
						throw e;
					} catch (Throwable e) {
						throw new RuntimeException(e);
					} finally{
						routingDataSource.setCurrentLookupKey(null, null);
					}	
				}
			};
			listFuture.add(executor.submit(callable));
		}
		
		
		//sharding result is not at the returnValue,we decorate it
		if(returnByResultHolder){
			List<Future<Object>> orignFutures = listFuture;
			listFuture = new ArrayList<>(orignFutures.size());
			
			for(int i = 0; i < orignFutures.size(); i++){
				Future<Object> orign = orignFutures.get(i);
				listFuture.add(new ReduceHolderFutureWrapper<>(i, orign,finalHolder));
			}
		}
		
		
		//get and execute aggregation strategy
		ReduceStrategy aggregationStrategy = getStrategy(strategy);
		if(aggregationStrategy == null){
			throw new RuntimeException("can not find specified aggregationStrategy:" + strategy);
		}
		Object aggregationResult = aggregationStrategy.reduce(listFuture);
		
		//check the return form
		if(returnByResultHolder){
			holder.setResult(aggregationResult);
			return null;
		} else {
			return aggregationResult;
		}
		
	}
	
	private static class ReduceHolderFutureWrapper<V> implements Future<V>{
		
		public ReduceHolderFutureWrapper(int shardingSeq,Future<V> orign,ReduceResultHolder<?, ?> reduceResultHolder){
			this.orign = orign;
			this.shardingSeq = shardingSeq;
			this.reduceResultHolder = reduceResultHolder;
		}

		private Future<V> orign;
		private int shardingSeq;
		private ReduceResultHolder<?, ?> reduceResultHolder;

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return orign.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return orign.isCancelled();
		}

		@Override
		public boolean isDone() {
			return orign.isDone();
		}

		@SuppressWarnings("unchecked")
		@Override
		public V get() throws InterruptedException, ExecutionException {
			orign.get();//wait till execute finished
			return (V) reduceResultHolder.getShardingResults().get(shardingSeq);
		}

		@SuppressWarnings("unchecked")
		@Override
		public V get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			orign.get(timeout, unit);//wait till execute finished
			return (V) reduceResultHolder.getShardingResults().get(shardingSeq);
		}
	
	}

	@SuppressWarnings("rawtypes")
	private ReduceStrategy getStrategy(String strategy) {
		return spElHelper.getValue(strategy);
	}
	
	private String getDsSetKey(MapReduce aggregationMethod, ShardingContext shardingContext) {
		String dsSet = aggregationMethod.dsSet();
		if(StringUtils.isEmpty(dsSet)){
			dsSet = shardingContext.dataSourceSet();
			if(StringUtils.isEmpty(dsSet)){
				throw new RuntimeException("Class annotation config error,can not find corresponding dbset setting!");
			}
		}
		return dsSet;
	}
	
	private String getFinalStrategy(MapReduce mapReduce, ShardingContext shardingContext) {
		String strategy = mapReduce.reduceStrategy();
		if(StringUtils.isEmpty(strategy)){
			strategy = shardingContext.reduceStrategy();
			if(StringUtils.isEmpty(strategy)){
				throw new RuntimeException("Class annotation config error,can not find Aggregation strategy!");
			}
		}
		return strategy;
	}
}
