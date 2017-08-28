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
@SpringBootTest(classes={DoubleObjectReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class DoubleObjectReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public Double listTest1(){
			return 0.1;
		}
		
		@MapReduce
		public Double listTest2(Double value){
			return value;
		}
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void listTest1(){
		double agTest1 = agTest.listTest1();
		Assert.assertTrue(agTest1 == 0.1 + 0.1);
	}
	
	@Test
	public void listTest2(){
		double agTest1 = agTest.listTest2(0.1);
		Assert.assertTrue(agTest1 == 0.1+0.1);
	}
	
}
