package org.easydevelop;

import org.easydevelop.aggregation.aspect.AggregationAspect;
import org.easydevelop.common.SpElHelper;
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
	public KeyGenerateAspect keyGenerateAspect(){
		return new KeyGenerateAspect();
	}
	
	@Bean
	public ShardingRoutingDataSource routingDataSource(){
		return new ShardingRoutingDataSource();
	}
	
	@Bean
	public ShardingAspect shardingAspect(){
		return new ShardingAspect();
	}
	
	@Bean
	public AggregationAspect aggregationAspect(){
		return new AggregationAspect();
	}
	
	@Bean
	public SpElHelper spElHelper(){
		return new SpElHelper();
	}
	
}
