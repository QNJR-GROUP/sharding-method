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
@SpringBootTest(classes={FloatBasicReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class FloatBasicReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public float listTest1(){
			return 0.1f;
		}
		
		@MapReduce
		public float listTest2(float value){
			return value;
		}
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void listTest1(){
		float agTest1 = agTest.listTest1();
		Assert.assertTrue(agTest1 == 0.1f + 0.1f);
	}
	
	@Test
	public void listTest2(){
		float agTest1 = agTest.listTest2(0.1f);
		Assert.assertTrue(agTest1 == 0.1f+0.1f);
	}
	
}
