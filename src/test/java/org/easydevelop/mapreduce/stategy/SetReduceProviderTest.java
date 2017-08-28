package org.easydevelop.mapreduce.stategy;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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
@SpringBootTest(classes={SetReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class SetReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public Set<Integer> test1(){
			HashSet<Integer> hashSet = new HashSet<>();
			hashSet.add(1);
			return hashSet;
		}
		
		@MapReduce
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public HashSet<Integer> test2(int value){
			Set hashSet = new LinkedHashSet<Integer>();
			hashSet.add(value);
			return (HashSet<Integer>) hashSet;
		}
		
		@MapReduce
		public Set<Integer> setTest3(){
			return null;
		}
		
		@MapReduce
		public LinkedHashSet<Integer> test4(){
			LinkedHashSet<Integer> arrayList = new LinkedHashSet<Integer>();
			arrayList.add(1);
			return arrayList;
		}
		
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void test1(){
		Set<Integer> agTest1 = agTest.test1();
		Assert.assertTrue(agTest1.size() == 1);
	}
	
	@Test
	public void test2(){
		HashSet<Integer> agTest1 = agTest.test2(1);
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
		LinkedHashSet<Integer> test4 = agTest.test4();
		Assert.assertTrue(test4.size() == 1);
	}
	
	
}
