package org.easydevelop.mapreduce.stategy;

import org.easydevelop.business.TestApplicationConfig;
import org.easydevelop.mapreduce.annotation.MapReduce;
import org.easydevelop.sharding.annotation.ShardingContext;
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
@SpringBootTest(classes={LongObjectReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class LongObjectReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public Long listTest1(){
			return 1l;
		}
		
		@MapReduce
		public Long listTest2(Long value){
			return value;
		}
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void listTest1(){
		long agTest1 = agTest.listTest1();
		Assert.assertTrue(agTest1 == 2);
	}
	
	@Test
	public void listTest2(){
		Long agTest1 = agTest.listTest2(2l);
		Assert.assertTrue(agTest1 == 4);
	}
	
}
