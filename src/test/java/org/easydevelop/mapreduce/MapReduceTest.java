package org.easydevelop.mapreduce;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.easydevelop.business.TestApplicationConfig;
import org.easydevelop.business.domain.UserOrder;
import org.easydevelop.mapreduce.annotation.MapReduce;
import org.easydevelop.mapreduce.aspect.ReduceResultHolder;
import org.easydevelop.mapreduce.strategy.ReduceStrategy;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.easydevelop.sharding.annotation.ShardingContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;



/** 
* @author xudeyou 
*/
@RunWith(SpringRunner.class)
@SpringBootTest(classes={MapReduceTest.class,TestApplicationConfig.class})
@Configuration
public class MapReduceTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@Autowired
		private ShardingRoutingDataSource routingDataSource;
		
		@Bean
		public ReduceStrategy<Integer, Integer> agNumberAdd(){
			return new ReduceStrategy<Integer, Integer>() {
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
		
		
		@MapReduce(reduceStrategy="@agNumberAdd")
		public int agTest1(){
			return 1;
		}
		
		@MapReduce(reduceStrategy="@agNumberAdd")
		public int agTest2(int value){
			return value;
		}
		
		@MapReduce(reduceStrategy="@agNumberAdd")
		public void agTest3(int value,ReduceResultHolder<Integer, Integer> resultHolder){
			resultHolder.setShardingResult(value);
		}
		
		@Transactional(readOnly=true)
		@MapReduce(reduceStrategy="@agNumberAdd")
		public int readOnlyTest(){
			return routingDataSource.getCurrentSlavePosition();
		}
		
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void agTest1(){
		int agTest1 = agTest.agTest1();
		Assert.assertTrue(agTest1 == 2);
	}
	
	@Test
	public void agTest2(){
		int agTest1 = agTest.agTest2(3);
		Assert.assertTrue(agTest1 == 6);
	}
	
	@Test
	public void agTest3(){
		ReduceResultHolder<Integer, Integer> resultHolder = new ReduceResultHolder<>();
		agTest.agTest3(3,resultHolder);
		Assert.assertTrue(resultHolder.getResult() == 6);
	}
	
	@Test
	public void readOnlyTest(){
		UserOrder order = new UserOrder();
		order.setUserId(6);
		Integer shardingDatasourceSeq = agTest.readOnlyTest();
		Assert.assertTrue(shardingDatasourceSeq != null && shardingDatasourceSeq.equals(0));
	}
	
}
