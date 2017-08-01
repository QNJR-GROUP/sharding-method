package org.easydevelop.aggregation.aspect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.easydevelop.aggregation.annotation.Aggregation;
import org.easydevelop.aggregation.annotation.AggregationMethod;
import org.easydevelop.aggregation.strategy.AggregationStrategy;
import org.easydevelop.common.SpElHelper;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

/** 
* @author xudeyou 
*/
@Aspect
@Order(-1)
public class AggregationAspect {
	
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	@Value("${sharding.method.aggregation.timeout.seconds:10}")
	private int timeout;
	
	@Autowired
	private ShardingRoutingDataSource routingDataSource;
	
	@Autowired
	private SpElHelper spElHelper;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Around("@annotation(aggregationMethod)")
	public Object around(ProceedingJoinPoint jp,AggregationMethod aggregationMethod) throws Throwable{
		
		//get Sharding annotation from method's class
		Class<?> targetClazz = jp.getTarget().getClass();
		Aggregation aggregation = targetClazz.getAnnotation(Aggregation.class);
		if(aggregation == null){
			throw new RuntimeException("Class annotation config error,can not find Sharding Annotation!");
		}
		
		//get configurations
		String strategy = getFinalStrategy(aggregationMethod, aggregation);
		String dsSetKey = getDsSetKey(aggregationMethod, aggregation);
		
		//check whether the sharding dataSource already set
		String currentDsSet = routingDataSource.getCurrentLookupDsSet();
		Integer currentLookupDsSequence = routingDataSource.getCurrentLookupDsSequence();
		boolean ancestorCallExist = false;
		if(currentDsSet != null || currentLookupDsSequence != null){
			throw new RuntimeException("should not execute this method in another transaction!");
		}
		
		//generate Callable objects
		List<DataSource> listDataSource = routingDataSource.getDataSourcesByDsSetName(dsSetKey);
		List<Callable<Object>> listCallable = new ArrayList<>(listDataSource.size());
		for(int i = 0; i < listDataSource.size(); i++){
			final int k = i;
			Callable<Object> callable = new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					routingDataSource.setCurrentLookupKey(dsSetKey, k);
					try {
						return jp.proceed();
					} catch (Exception e) {
						throw e;
					} catch (Throwable e) {
						throw new RuntimeException(e);
					} finally{
						if(!ancestorCallExist){
							routingDataSource.setCurrentLookupKey(null, null);
						}
					}
				}
			};
			listCallable.add(callable);
		}
		
		//get executed result
		List<Future<Object>> invokeAll = executor.invokeAll(listCallable, timeout, TimeUnit.SECONDS);
		
		//aggregation
		AggregationStrategy aggregationStrategy = getStrategy(strategy);
		if(aggregationStrategy == null){
			throw new RuntimeException("can not find specified aggregationStrategy:" + strategy);
		}
		return aggregationStrategy.aggregation(invokeAll);
	}

	@SuppressWarnings("rawtypes")
	private AggregationStrategy getStrategy(String strategy) {
		return spElHelper.getValue(strategy);
	}
	
	private String getDsSetKey(AggregationMethod aggregationMethod, Aggregation aggregation) {
		String dsSet = aggregationMethod.dsSet();
		if(StringUtils.isEmpty(dsSet)){
			dsSet = aggregation.defaultDsSet();
			if(StringUtils.isEmpty(dsSet)){
				throw new RuntimeException("Class annotation config error,can not find corresponding dbset setting!");
			}
		}
		return dsSet;
	}
	
	private String getFinalStrategy(AggregationMethod aggregationMethod, Aggregation aggregation) {
		String strategy = aggregationMethod.strategy();
		if(StringUtils.isEmpty(strategy)){
			strategy = aggregation.defaultStrategy();
			if(StringUtils.isEmpty(strategy)){
				throw new RuntimeException("Class annotation config error,can not find Aggregation strategy!");
			}
		}
		return strategy;
	}
}
