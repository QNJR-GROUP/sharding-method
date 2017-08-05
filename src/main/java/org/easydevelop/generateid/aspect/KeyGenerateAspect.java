package org.easydevelop.generateid.aspect;

import java.util.LinkedHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.easydevelop.common.SpElHelper;
import org.easydevelop.generateid.annotation.GenerateId;
import org.easydevelop.generateid.strategy.KeyGenerateStrategy;
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
@Order(-2)
public class KeyGenerateAspect {
	
	@Autowired
	private SpElHelper spElHelper;
	
	@Around("@annotation(keyInject)")
	public Object around(ProceedingJoinPoint jp,GenerateId keyInject) throws Throwable{
		
		//get KeyGenerate annotation from method's class
		ShardingContext keyGenerator = ShardingAspect.getShardingContext();
		if(keyGenerator == null){
			throw new RuntimeException("Class annotation config error,can not find KeyGenerator!");
		}
		
		//get configurations
		String[] keyEls = getFinalIdNames(keyInject, keyGenerator);
		String strategy = getFinalStrategy(keyInject, keyGenerator);
		boolean allowUserDefindKeyValue = getAllowUserDefindKeyValue(keyInject, keyGenerator);
		String[] strategyMetadataEls = getStrategyMetadataEls(keyInject, keyGenerator);
		
		//get parameters based on keyNames
		MethodSignature signature = (MethodSignature) jp.getSignature();
		LinkedHashMap<String, Object> mapArgs = getArgsLinkedHashMap(signature.getParameterNames(),jp.getArgs());
		
		//get key values
		Object[] keyValues = getElsValues(keyEls,mapArgs);
		
		//check allowCallerDefinedKeyValue setting
		boolean callerDefinedKeyValue = false;
		for(Object o:keyValues){
			if(o != null){
				callerDefinedKeyValue = true;
			}
		}
		if(!allowUserDefindKeyValue && callerDefinedKeyValue){
			throw new RuntimeException("the key's position should be null!");
		}
		
		//generate keys and set to mapArgs
		if(!callerDefinedKeyValue){
			Object[] metadata = getElsValues(strategyMetadataEls,mapArgs);
			Object[] generatedKeyValues = generateKeys(strategy,metadata);
			setGeneratedKeyValues(mapArgs,generatedKeyValues,keyEls);
		}
		
		//call with the adjusted parameters
		try {
			return jp.proceed(mapArgs.values().toArray());
		} catch (Throwable e) {
			throw e;
		}
	}

	
	private String[] getStrategyMetadataEls(GenerateId keyInject, ShardingContext shardingContext) {
		String[] strategyMetadataEls = keyInject.strategyMetadataEls();
		if(strategyMetadataEls.length == 0){
			strategyMetadataEls = shardingContext.generateIdMetadataEls();
		}
		return strategyMetadataEls;
	}


	private void setGeneratedKeyValues(LinkedHashMap<String, Object> mapArgs, Object[] generatedKeyValues,
			String[] keyEls) {
		for(int i = 0; i < keyEls.length; i++ ){
			spElHelper.setValue(keyEls[i], mapArgs, generatedKeyValues[i]);
		}
	}


	private Object[] generateKeys(String strategyStr,Object[] metaData) {
		KeyGenerateStrategy strategy = spElHelper.getValue(strategyStr);
		if(strategy == null){
			throw new RuntimeException("can not find specifc key generate strategy:" + strategyStr);
		}
		
		return strategy.generateKey(metaData);
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




	private boolean getAllowUserDefindKeyValue(GenerateId keyInject, ShardingContext shardingContext) {
		int allow = keyInject.allowCallerDefinedKeyValue();
		if(allow == 0){
			allow = shardingContext.generateIdByCaller();
			if(allow == 0){
				throw new RuntimeException("Class annotation config error,can not find key generate keyNames!");
			}
		}
		return allow == -1?false:true;
	}

	private String[] getFinalIdNames(GenerateId keyInject, ShardingContext shardingContext) {
		String[] keyNames = keyInject.keyEls();
		if(keyNames.length == 0){
			keyNames = shardingContext.generateIdEls();
			if(keyNames.length == 0){
				throw new RuntimeException("Class annotation config error,can not find key generate keyNames!");
			}else{
				for(String keyName:keyNames){
					if(StringUtils.isEmpty(keyName)){
						throw new RuntimeException("keyName can not be null or empty!");
					}
				}
			}
		}
		return keyNames;
	}
	
	private String getFinalStrategy(GenerateId keyInject, ShardingContext shardingContext) {
		String strategy = keyInject.strategy();
		if(StringUtils.isEmpty(strategy)){
			strategy = shardingContext.generateIdStrategy();
			if(StringUtils.isEmpty(strategy)){
				throw new RuntimeException("Class annotation config error,can not find key generate strategy!");
			}
		}
		return strategy;
	}
}
