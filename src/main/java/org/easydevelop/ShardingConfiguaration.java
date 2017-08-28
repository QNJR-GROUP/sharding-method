package org.easydevelop;

import java.util.ArrayList;
import java.util.List;

import org.easydevelop.common.SpElHelper;
import org.easydevelop.common.TransactionStatusHelper;
import org.easydevelop.generateid.aspect.KeyGenerateAspect;
import org.easydevelop.mapreduce.aspect.MapReduceAspect;
import org.easydevelop.mapreduce.strategy.ReduceProviderManager;
import org.easydevelop.mapreduce.strategy.ReduceProviderManager.OnFutureExceptionOrTimeout;
import org.easydevelop.mapreduce.strategy.provider.CollectionReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.DoubleBasicReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.DoubleObjectReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.FloatBasicReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.FloatObjectReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.IntReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.IntegerReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.KeyValueMapReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.ListReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.LongBasicReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.LongObjectReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.ReduceProvider;
import org.easydevelop.mapreduce.strategy.provider.SetReduceProvider;
import org.easydevelop.readonly.RoundRobinReadonlyDsSelectStrategy;
import org.easydevelop.select.aspect.SelectDataSourceAspect;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.easydevelop.sharding.aspect.ShardingAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.OrderComparator;

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
	
	
	@ConditionalOnMissingBean(name = "defalutReduceStrategy")
	@Configuration
	public static class ReduceProviderManagerConfiguration{
		
		@Autowired
		@SuppressWarnings("rawtypes")
		private List<ReduceProvider> reduceProviders = new ArrayList<>();
		
		@Bean
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ReduceProviderManager defalutReduceStrategy(){
			OrderComparator.sort(reduceProviders);
			return new ReduceProviderManager(onFutureExceptionOrTimeOut, futureTimeOutMills, reduceProviders);
		}
		
		@Value("${sharding-method.mapreduce.reduce.future.exception:THROW_EXCEPTION}")
		private OnFutureExceptionOrTimeout onFutureExceptionOrTimeOut;
		
		@Value("${sharding-method.mapreduce.reduce.future.timeout-mills:30000}")
		private int futureTimeOutMills; 
		
		@Bean
		public ListReduceProvider listReduceProvider(){
			return new ListReduceProvider();
		}
		
		@Bean
		public SetReduceProvider setReduceProvider(){
			return new SetReduceProvider();
		}
		
		@Bean
		public CollectionReduceProvider collectionReduceProvider(){
			return new CollectionReduceProvider();
		}
		
		@Bean
		public KeyValueMapReduceProvider keyValueMapReduceProvider(){
			return new KeyValueMapReduceProvider();
		}
		
		@Bean
		public IntReduceProvider intReduceProvider(){
			return new IntReduceProvider();
		}
		
		@Bean
		public IntegerReduceProvider integerReduceProvider(){
			return new IntegerReduceProvider();
		}
		
		@Bean
		public LongBasicReduceProvider longBasicReduceProvider(){
			return new LongBasicReduceProvider();
		}
		
		@Bean
		public LongObjectReduceProvider longObjectReduceProvider(){
			return new LongObjectReduceProvider();
		}
		
		@Bean
		public DoubleBasicReduceProvider doubleBasicReduceProvider(){
			return new DoubleBasicReduceProvider();
		}
		
		@Bean
		public DoubleObjectReduceProvider doubleObjectReduceProvider(){
			return new DoubleObjectReduceProvider();
		}
		
		@Bean
		public FloatBasicReduceProvider floatBasicReduceProvider(){
			return new FloatBasicReduceProvider();
		}
		
		@Bean
		public FloatObjectReduceProvider floatObjectReduceProvider(){
			return new FloatObjectReduceProvider();
		}
		
	}
}
