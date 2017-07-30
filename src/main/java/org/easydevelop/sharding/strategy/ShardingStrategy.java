package org.easydevelop.sharding.strategy;

/** 
* @author xudeyou 
*/
public interface ShardingStrategy {
	
	String getStrategyName();
	
	int select(Object[] shardingMetadata,int datasourceSize);

}
