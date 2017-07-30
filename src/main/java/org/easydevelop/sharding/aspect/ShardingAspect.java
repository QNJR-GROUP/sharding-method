package org.easydevelop.sharding.aspect;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.easydevelop.sharding.annotation.Sharding;
import org.easydevelop.sharding.annotation.ShardingMethod;
import org.easydevelop.sharding.strategy.ShardingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

/** 
* @author xudeyou 
*/
@Aspect
@Order(-1)
public class ShardingAspect {
	
	private ExpressionParser parser = new SpelExpressionParser();
	
	@Autowired(required = false)
	private List<ShardingStrategy> listStrategy = Collections.emptyList();;
	
	@Autowired
	private ShardingRoutingDataSource routingDataSource;
	
	private Map<String,ShardingStrategy> mapStrategy;
	
	@PostConstruct
	private void init(){
		mapStrategy = new HashMap<>(listStrategy.size());
		for(ShardingStrategy strategy:listStrategy){
			ShardingStrategy orign = mapStrategy.put(strategy.getStrategyName(), strategy);
			if(orign != null){
				throw new RuntimeException("same name sharding-strategy exsist!");
			}
		}
	}
	
	
	@Around("@annotation(shardingMethod)")
	public Object around(ProceedingJoinPoint jp,ShardingMethod shardingMethod) throws Throwable{
		
		//get Sharding annotation from method's class
		Class<?> targetClazz = jp.getTarget().getClass();
		Sharding sharding = targetClazz.getAnnotation(Sharding.class);
		if(sharding == null){
			throw new RuntimeException("Class annotation config error,can not find Sharding Annotation!");
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
		ShardingStrategy strategy = mapStrategy.get(strategyStr);
		if(strategy == null){
			throw new RuntimeException("can not find specifc Sharding strategy:" + strategyStr);
		}
		
		return strategy.select(metaData, datasourceSize);
	}


	private Object[] getElsValues(String[] keyEls, LinkedHashMap<String, Object> mapArgs) {
		Object[] result = new Object[keyEls.length];
		for(int i = 0; i < keyEls.length; i++){
			Expression exp = getExpression(keyEls[i]);
			result[i] = exp.getValue(mapArgs);
		}
		return result;
	}


	private ConcurrentHashMap<String,Expression> mapExpression = new ConcurrentHashMap<>();
	private Expression getExpression(String expressionStr) {
		Expression expression = mapExpression.get(expressionStr);
		if(expression == null){
			expression = parser.parseExpression(expressionStr);
			mapExpression.put(expressionStr, expression);
		} 
		return expression;
	}


	private LinkedHashMap<String, Object> getArgsLinkedHashMap(String[] parameterNames, Object[] args) {
		
		Assert.isTrue(parameterNames.length == args.length,"the length should be the same!");
		
		LinkedHashMap<String, Object> map = new LinkedHashMap<>(args.length);
		for(int i = 0; i < args.length; i++){
			map.put(parameterNames[i], args[i]);
		}
		
		return map;
	}



	private String[] getFinalKeyEls(ShardingMethod shardingMethod, Sharding sharding) {
		String[] keyEls = shardingMethod.keyNameEls();
		if(keyEls.length == 0){
			keyEls = sharding.defaultKeyEls();
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
	
	private String getDsSetKey(ShardingMethod shardingMethod, Sharding sharding) {
		String dsSet = shardingMethod.dsSet();
		if(StringUtils.isEmpty(dsSet)){
			dsSet = sharding.defaultDsSet();
			if(StringUtils.isEmpty(dsSet)){
				throw new RuntimeException("Class annotation config error,can not find corresponding dbset setting!");
			}
		}
		return dsSet;
	}
	
	private String getFinalStrategy(ShardingMethod shardingMethod, Sharding sharding) {
		String strategy = shardingMethod.strategy();
		if(StringUtils.isEmpty(strategy)){
			strategy = sharding.defaultStrategy();
			if(StringUtils.isEmpty(strategy)){
				throw new RuntimeException("Class annotation config error,can not find sharding strategy!");
			}
		}
		return strategy;
	}
}
