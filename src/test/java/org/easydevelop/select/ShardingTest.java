package org.easydevelop.select;

import org.easydevelop.business.TestApplicationConfig;
import org.easydevelop.business.domain.UserOrder;
import org.easydevelop.select.annotation.SelectDataSource;
import org.easydevelop.sharding.ShardingRoutingDataSource;
import org.easydevelop.sharding.annotation.ShardingContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;



/** 
* @author xudeyou 
*/
@RunWith(SpringRunner.class)
@SpringBootTest(classes={ShardingTest.class,TestApplicationConfig.class})
@Configuration
public class ShardingTest {
	
	@ShardingContext(dataSourceSet="orderSet",shardingKeyEls="[order].userId",shardingStrategy=TestApplicationConfig.BY_USER_ID_MOD)
	@Service
	public static class ShardingServiceInstance{
		
		@Autowired
		private ShardingRoutingDataSource routingDataSource;
		
		@SelectDataSource
		public int saveOrderWithUserId5(UserOrder order){
			return routingDataSource.getCurrentLookupDsSequence();
		}
		
		@SelectDataSource
		public int saveOrderWithUserId6(UserOrder order){
			return routingDataSource.getCurrentLookupDsSequence();
		}
		
		@SelectDataSource
		@Transactional(readOnly=true)
		public int readOnlyTest(UserOrder order){
			return routingDataSource.getCurrentSlavePosition();
		}
		
	}
	

		
	
	@Autowired
	private ShardingServiceInstance shardingServiceInstance;
	
	@Test
	public void saveOrderWithUser5(){
		UserOrder order = new UserOrder();
		order.setUserId(5);
		Integer shardingDatasourceSeq = shardingServiceInstance.saveOrderWithUserId5(order);
		Assert.assertTrue(shardingDatasourceSeq != null && shardingDatasourceSeq.equals(1));
	}
	
	@Test
	public void saveOrderWithUser6(){
		UserOrder order = new UserOrder();
		order.setUserId(6);
		Integer shardingDatasourceSeq = shardingServiceInstance.saveOrderWithUserId6(order);
		Assert.assertTrue(shardingDatasourceSeq != null && shardingDatasourceSeq.equals(0));
	}
	
	@Test
	public void readOnlyTest(){
		UserOrder order = new UserOrder();
		order.setUserId(6);
		Integer shardingDatasourceSeq = shardingServiceInstance.readOnlyTest(order);
		Assert.assertTrue(shardingDatasourceSeq != null && shardingDatasourceSeq.equals(0));
	}
}
