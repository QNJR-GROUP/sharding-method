package org.easydevelop.select.aspect;

import java.util.LinkedHashMap;
import java.util.List;

import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.easydevelop.common.SpElHelper;
import org.easydevelop.select.annotation.SelectDataSource;
import org.easydevelop.select.strategy.SelectDataSourceStrategy;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.easydevelop.sharding.annotation.ShardingContext;
import org.easydevelop.sharding.aspect.ShardingAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/** 
* @author xudeyou 
*/
@Aspect
@Order(-1)
public class SelectDataSourceAspect {
	
	
	@Autowired
	private ShardingRoutingDataSource routingDataSource;
	
	@Autowired
	private SpElHelper spElHelper;
	
	@Around("@annotation(shardingMethod)")
	public Object around(ProceedingJoinPoint jp,SelectDataSource shardingMethod) throws Throwable{
		
		ShardingContext sharding = ShardingAspect.getShardingContext();
		if(sharding == null){
			throw new RuntimeException("the ancestor caller's class shold declare shardingContext");
		}
		
		//get configurations
		String[] keyEls = getFinalKeyEls(shardingMethod, sharding);
		String strategy = getFinalStrategy(shardingMethod, sharding);
		String dsSetKey = getDsSetKey(shardingMethod, sharding);
		
		//get parameters based on keyNames
		MethodSignature signature = (MethodSignature) jp.getSignature();
		LinkedHashMap<String, Object> mapArgs = getArgsLinkedHashMap(signature.getParameterNames(),jp.getArgs());
		
		//get sharding key values
		Object[] keyValues = getElsValues(keyEls,mapArgs);
		
		//calculate the sharding dataSource number
		List<DataSource> dsSet = routingDataSource.getDataSourcesByDsSetName(dsSetKey);
		int num = selectDataSource(strategy,keyValues,dsSet.size());
		
		//check whether the sharding dataSource already set
		String currentDsSet = routingDataSource.getCurrentLookupDsSet();
		Integer currentLookupDsSequence = routingDataSource.getCurrentLookupDsSequence();
		boolean ancestorCallExist = false;
		if(currentDsSet != null || currentLookupDsSequence != null){
			ancestorCallExist = true;
			if(!dsSetKey.equals(currentDsSet) || num != currentLookupDsSequence.intValue()){
				throw new RuntimeException("To guarantee specificed Isolation-level can not switch datasource!");
			}
		}
		
		//set the current lookup key
		if(!ancestorCallExist){
			routingDataSource.setCurrentLookupKey(dsSetKey, num);
		}
		
		//call with the transaction
		try {
			return jp.proceed();
		} catch (Throwable e) {
			throw e;
		} finally{
			if(!ancestorCallExist){
				routingDataSource.setCurrentLookupKey(null, null);
			}
		}
	}

	private int selectDataSource(String strategyStr,Object[] metaData,int datasourceSize) {
		SelectDataSourceStrategy strategy = spElHelper.getValue(strategyStr);
		if(strategy == null){
			throw new RuntimeException("can not find specifc Sharding strategy:" + strategyStr);
		}
		
		return strategy.select(metaData, datasourceSize);
	}


	private Object[] getElsValues(String[] keyEls, LinkedHashMap<String, Object> mapArgs) {
		Object[] result = new Object[keyEls.length];
		for(int i = 0; i < keyEls.length; i++){
			result[i] = spElHelper.getValue(keyEls[i], mapArgs);
		}
		return result;
	}




	private LinkedHashMap<String, Object> getArgsLinkedHashMap(String[] parameterNames, Object[] args) {
		
		Assert.isTrue(parameterNames.length == args.length,"the length should be the same!");
		
		LinkedHashMap<String, Object> map = new LinkedHashMap<>(args.length);
		for(int i = 0; i < args.length; i++){
			map.put(parameterNames[i], args[i]);
		}
		
		return map;
	}



	private String[] getFinalKeyEls(SelectDataSource shardingMethod, ShardingContext sharding) {
		String[] keyEls = shardingMethod.keyNameEls();
		if(keyEls.length == 0){
			keyEls = sharding.shardingKeyEls();
			if(keyEls.length == 0){
				throw new RuntimeException("Class annotation config error,can not find key sharding ELs!");
			}else{
				for(String keyEl:keyEls){
					if(StringUtils.isEmpty(keyEl)){
						throw new RuntimeException("key EL can not be null or empty!");
					}
				}
			}
		}
		return keyEls;
	}
	
	private String getDsSetKey(SelectDataSource shardingMethod, ShardingContext sharding) {
		String dsSet = shardingMethod.dsSet();
		if(StringUtils.isEmpty(dsSet)){
			dsSet = sharding.dataSourceSet();
			if(StringUtils.isEmpty(dsSet)){
				throw new RuntimeException("Class annotation config error,can not find corresponding dbset setting!");
			}
		}
		return dsSet;
	}
	
	private String getFinalStrategy(SelectDataSource shardingMethod, ShardingContext sharding) {
		String strategy = shardingMethod.strategy();
		if(StringUtils.isEmpty(strategy)){
			strategy = sharding.shardingStrategy();
			if(StringUtils.isEmpty(strategy)){
				throw new RuntimeException("Class annotation config error,can not find sharding strategy!");
			}
		}
		return strategy;
	}
}
