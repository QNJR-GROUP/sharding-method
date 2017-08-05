package org.easydevelop.business;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.easydevelop.EnableShardingMethod;
import org.easydevelop.business.domain.User;
import org.easydevelop.generateid.strategy.KeyGenerateStrategy;
import org.easydevelop.mapreduce.strategy.ReduceStrategy;
import org.easydevelop.select.strategy.SelectDataSourceStrategy;
import org.easydevelop.sharding.DataSourceSet;
import org.easydevelop.sharding.ShardingRoutingDataSource;
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
	public SelectDataSourceStrategy modUserId(){
		return new SelectDataSourceStrategy() {

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
	public ReduceStrategy<List<User>,List<User>> aggOrderByUserId(){
		return new ReduceStrategy<List<User>,List<User>>() {

			@Override
			public List<User> reduce(List<Future<List<User>>> subFutrueList) {
				
				List<User> result = new ArrayList<>();
				subFutrueList.forEach(listFuture->{
					try {
						result.addAll(listFuture.get());
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
	public ReduceStrategy<Integer,Integer> aggUpdateCountAdd(){
		return new ReduceStrategy<Integer,Integer>() {

			@Override
			public Integer reduce(List<Future<Integer>> subFutrueList) {
				return subFutrueList.stream().map(future -> {
					try {
						return future.get();
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				}).mapToInt(i->i).sum();
			}
		};
		
	}
	
	
}
