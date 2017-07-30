package org.easydevelop.business;

import java.util.ArrayList;
import java.util.List;
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
	
	public static final String UPDATE_COUNT_ADD = "UPDATE_COUNT_ADD";
	public static final String BY_USER_ID_MOD = "byUserIdMod";
	public static final String INT_INCREASE = "intIncrease";
	public static final String AGGREGATION_USER_ORDER_BY_USER_ID = "AGGREGATION_USER_ORDER_BY_USER_ID";

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
	public JdbcTemplate getJdbcTemplate(ShardingRoutingDataSource dataSource){
		return new JdbcTemplate(dataSource);
	}
	
	@Bean
	public DataSourceTransactionManager getTransactionManager(ShardingRoutingDataSource dataSource){
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
		return dataSourceTransactionManager;
	}
	
	@Bean
	public ShardingStrategy getShardingStrategy(){
		return new ShardingStrategy() {
			@Override
			public String getStrategyName() {
				return BY_USER_ID_MOD;
			}

			@Override
			public int select(Object[] shardingMetadata, int datasourceSize) {
				Integer userId = (Integer) shardingMetadata[0];
				return userId % datasourceSize;
			}
		};
	}
	
	@Bean
	public KeyGenerateStrategy getKeyGenerateStrategy(){
		return new KeyGenerateStrategy() {
			
			AtomicInteger atomicInteger = new AtomicInteger(0);
			
			@Override
			public String getStrategyName() {
				return INT_INCREASE;
			}
			
			@Override
			public Object[] generateKey(Object[] metadata) {
				return new Object[]{atomicInteger.incrementAndGet()};
			}
		};
		
	}
	
	@Bean
	public AggregationStrategy geAggregationStrategy(){
		return new AggregationStrategy() {
			@Override
			public String getStrategyName() {
				return AGGREGATION_USER_ORDER_BY_USER_ID;
			}

			@Override
			public Object aggregation(List<Object> subFutrueList) {
				@SuppressWarnings("rawtypes")
				List raw = subFutrueList;
				@SuppressWarnings("unchecked")
				List<List<User>> listUserList = raw;
				
				ArrayList<User> userList = new ArrayList<>();
				for(List<User> list:listUserList){
					userList.addAll(list);
				}
				
				userList.sort((first,second)->Integer.compare(first.getUserId(), second.getUserId()));
				
				return userList;
			}
			
		};
		
	}
	
	
	@Bean
	public AggregationStrategy geAggregationStrategy2(){
		return new AggregationStrategy() {
			@Override
			public String getStrategyName() {
				return UPDATE_COUNT_ADD;
			}

			@Override
			public Object aggregation(List<Object> subFutrueList) {
				@SuppressWarnings("rawtypes")
				List raw = subFutrueList;
				@SuppressWarnings("unchecked")
				List<Integer> listUserList = raw;
				
				int result = 0;
				for(Integer count:listUserList){
					result += count.intValue();
				}
				
				return result;
			}
			
		};
		
	}
	
	
}
