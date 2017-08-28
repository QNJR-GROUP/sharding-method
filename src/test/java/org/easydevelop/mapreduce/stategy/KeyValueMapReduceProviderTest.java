package org.easydevelop.mapreduce.stategy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
@SpringBootTest(classes={KeyValueMapReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class KeyValueMapReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public Map<Integer,Integer> test1(){
			Map<Integer,Integer> hashSet = new HashMap<>();
			hashSet.put(1,1);
			return hashSet;
		}
		
		@MapReduce
		public HashMap<Integer,Integer> test2(int value){
			LinkedHashMap<Integer,Integer> hashSet = new LinkedHashMap<Integer,Integer>();
			hashSet.put(value,value);
			return hashSet;
		}
		
		@MapReduce
		public Map<Integer,Integer> setTest3(){
			return null;
		}
		
		@MapReduce
		public LinkedHashMap<Integer,Integer> test4(){
			LinkedHashMap<Integer,Integer> arrayList = new LinkedHashMap<Integer,Integer>();
			arrayList.put(1,1);
			return arrayList;
		}
		
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void test1(){
		Map<Integer, Integer> agTest1 = agTest.test1();
		Assert.assertTrue(agTest1.size() == 1);
	}
	
	@Test
	public void test2(){
		HashMap<Integer, Integer> agTest1 = agTest.test2(1);
		Assert.assertTrue(agTest1.size() == 1);
	}
	
	@Test
	public void test3(){
		boolean exception = false;
		try {
			agTest.setTest3();
		} catch (Exception e) {
			exception = true;
		}
		Assert.assertTrue(exception);
	}
	
	@Test
	public void test4(){
		LinkedHashMap<Integer, Integer> test4 = agTest.test4();
		Assert.assertTrue(test4.size() == 1);
	}
	
	
}
