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
@SpringBootTest(classes={IntReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class IntReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public int listTest1(){
			return 1;
		}
		
		@MapReduce
		public int listTest2(int value){
			return value;
		}
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void listTest1(){
		int agTest1 = agTest.listTest1();
		Assert.assertTrue(agTest1 == 2);
	}
	
	@Test
	public void listTest2(){
		int agTest1 = agTest.listTest2(2);
		Assert.assertTrue(agTest1 == 4);
	}
	
}
