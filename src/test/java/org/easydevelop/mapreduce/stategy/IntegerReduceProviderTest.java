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
@SpringBootTest(classes={IntegerReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class IntegerReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public Integer listTest1(){
			return 1;
		}
		
		@MapReduce
		public Integer listTest2(int value){
			return value;
		}
		
		@MapReduce
		public Integer listTest3(){
			return null;
		}
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void test1(){
		int agTest1 = agTest.listTest1();
		Assert.assertTrue(agTest1 == 2);
	}
	
	@Test
	public void test2(){
		int agTest1 = agTest.listTest2(2);
		Assert.assertTrue(agTest1 == 4);
	}
	
	@Test
	public void test3(){
		boolean exception = false;
		try {
			agTest.listTest3();
		} catch (Exception e) {
			exception = true;
		}
		Assert.assertTrue(exception);
	}
	
}
