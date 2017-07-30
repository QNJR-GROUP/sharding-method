package org.easydevelop.sharding;

import org.easydevelop.business.TestApplicationConfig;
import org.easydevelop.business.domain.UserOrder;
import org.easydevelop.sharding.annotation.Sharding;
import org.easydevelop.sharding.annotation.ShardingMethod;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;



/** 
* @author xudeyou 
*/
@RunWith(SpringRunner.class)
@SpringBootTest(classes={ShardingTest.class,TestApplicationConfig.class})
@Configuration
public class ShardingTest {
	
	@Sharding(defaultDsSet="orderSet",defaultKeyEls="[order].userId",defaultStrategy=TestApplicationConfig.BY_USER_ID_MOD)
	@Service
	public static class ShardingServiceInstance{
		
		@Autowired
		private ShardingRoutingDataSource routingDataSource;
		
		@ShardingMethod
		public int saveOrderWithUserId5(UserOrder order){
			return routingDataSource.getCurrentLookupDsSequence();
		}
		
		@ShardingMethod
		public int saveOrderWithUserId6(UserOrder order){
			return routingDataSource.getCurrentLookupDsSequence();
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
}
