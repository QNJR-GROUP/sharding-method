package org.easydevelop.select.strategy;

/** 
* @author xudeyou 
*/
public interface SelectDataSourceStrategy {
	
	int select(Object[] shardingMetadata,int datasourceSize);

}
