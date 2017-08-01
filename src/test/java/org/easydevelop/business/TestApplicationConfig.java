package org.easydevelop.business;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.easydevelop.EnableShardingMethod;
import org.easydevelop.aggregation.strategy.AggregationStrategy;
import org.easydevelop.business.domain.User;
import org.easydevelop.keygenerator.strategy.KeyGenerateStrategy;
import org.easydevelop.sharding.DataSourceSet;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.easydevelop.sharding.strategy.ShardingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/** 
* @author xudeyou 
*/
@Configuration
@EnableShardingMethod
@EnableTransactionManagement
@ComponentScan("org.easydevelop.business")
public class TestApplicationConfig {
	
	public static final String UPDATE_COUNT_ADD = "@aggUpdateCountAdd";
	public static final String BY_USER_ID_MOD = "@modUserId";
	public static final String INT_INCREASE = "@intIncrease";
	public static final String AGGREGATION_USER_ORDER_BY_USER_ID = "@aggOrderByUserId";

	@Bean
	public DataSourceSet getDataSourceSet(){
		
		MysqlDataSource ds1 = new MysqlDataSource();
		ds1.setURL("jdbc:mysql://192.168.92.134:3306/sharding_test_0");
		ds1.setUser("root");
		ds1.setPassword("xdy1117");
		
		MysqlDataSource ds2 = new MysqlDataSource();
		ds2.setURL("jdbc:mysql://192.168.92.134:3306/sharding_test_1");
		ds2.setUser("root");
		ds2.setPassword("xdy1117");
		
		ArrayList<DataSource> arrayList = new ArrayList<DataSource>(2);
		arrayList.add(ds1);
		arrayList.add(ds2);
		
		DataSourceSet dataSourceSet = new DataSourceSet("orderSet",arrayList);
		return dataSourceSet;
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate(ShardingRoutingDataSource dataSource){
		return new JdbcTemplate(dataSource);
	}
	
	@Bean
	public DataSourceTransactionManager transactionManager(ShardingRoutingDataSource dataSource){
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
		return dataSourceTransactionManager;
	}
	
	@Bean
	public ShardingStrategy modUserId(){
		return new ShardingStrategy() {

			@Override
			public int select(Object[] shardingMetadata, int datasourceSize) {
				Integer userId = (Integer) shardingMetadata[0];
				return userId % datasourceSize;
			}
		};
	}
	
	@Bean
	public KeyGenerateStrategy intIncrease(){
		return new KeyGenerateStrategy() {
			
			AtomicInteger atomicInteger = new AtomicInteger(0);
			
			@Override
			public Object[] generateKey(Object[] metadata) {
				return new Object[]{atomicInteger.incrementAndGet()};
			}
		};
		
	}
	
	@Bean
	public AggregationStrategy<List<User>,List<User>> aggOrderByUserId(){
		return new AggregationStrategy<List<User>,List<User>>() {

			@Override
			public List<User> aggregation(List<Future<List<User>>> subFutrueList) {
				
				List<User> result = new ArrayList<>();
				subFutrueList.forEach(future->{
					try {
						List<User> list = future.get();
						result.addAll(list);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				result.sort((first,second)->Integer.compare(first.getUserId(), second.getUserId()));
				return result;
			}
			
		};
		
	}
	
	
	@Bean
	public AggregationStrategy<Integer,Integer> aggUpdateCountAdd(){
		return new AggregationStrategy<Integer,Integer>() {

			@Override
			public Integer aggregation(List<Future<Integer>> subFutrueList) {
				
				AtomicInteger count = new AtomicInteger(0);
				subFutrueList.forEach(future->{try {
					Integer integer = future.get();
					count.addAndGet(integer);
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}});
				return count.get();
			}
		};
		
	}
	
	
}
