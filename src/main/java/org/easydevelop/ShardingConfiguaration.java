package org.easydevelop;

import org.easydevelop.common.SpElHelper;
import org.easydevelop.common.TransactionStatusHelper;
import org.easydevelop.generateid.aspect.KeyGenerateAspect;
import org.easydevelop.mapreduce.aspect.MapReduceAspect;
import org.easydevelop.readonly.RoundRobinReadonlyDsSelectStrategy;
import org.easydevelop.select.aspect.SelectDataSourceAspect;
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
	public ShardingRoutingDataSource shardingRoutingDataSource(){
		return new ShardingRoutingDataSource();
	}
	
	@Bean
	public SelectDataSourceAspect selectDataSourceAspect(){
		return new SelectDataSourceAspect();
	}
	
	@Bean
	public MapReduceAspect mapReduceAspect(){
		return new MapReduceAspect();
	}
	
	@Bean
	public SpElHelper spElHelper(){
		return new SpElHelper();
	}
	
	@Bean
	public ShardingAspect shardingAspect(){
		return new ShardingAspect();
	}
	
	@Bean
	public TransactionStatusHelper transactionStatusHelper(){
		return new TransactionStatusHelper();
	}
	
	@Bean
	public RoundRobinReadonlyDsSelectStrategy readOnlyDsSelectStrategy(){
		return new RoundRobinReadonlyDsSelectStrategy();
	}
	
}
