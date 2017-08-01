package org.easydevelop.sharding.strategy;

/** 
* @author xudeyou 
*/
public interface ShardingStrategy {
	
	int select(Object[] shardingMetadata,int datasourceSize);

}
