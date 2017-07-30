package org.easydevelop;

import org.easydevelop.aggregation.aspect.AggregationAspect;
import org.easydevelop.keygenerator.aspect.KeyGenerateAspect;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.easydevelop.sharding.aspect.ShardingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/** 
* @author xudeyou 
*/
@Configuration
@EnableAspectJAutoProxy
public class ShardingConfiguaration {

	@Bean
	public KeyGenerateAspect getKeyGenerateAspect(){
		return new KeyGenerateAspect();
	}
	
	@Bean
	public ShardingRoutingDataSource getRoutingDataSource(){
		return new ShardingRoutingDataSource();
	}
	
	@Bean
	public ShardingAspect getShardingAspect(){
		return new ShardingAspect();
	}
	
	@Bean
	public AggregationAspect getAggregationAspect(){
		return new AggregationAspect();
	}
	
}
