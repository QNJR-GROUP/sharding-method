package org.easydevelop.mapreduce.stategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

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
@SpringBootTest(classes={ListReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class ListReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public Collection<Integer> listTest1(){
			return Arrays.asList(1);
		}
		
		@MapReduce
		public Collection<Integer> listTest2(int value){
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			arrayList.add(value);
			return arrayList;
		}
		
		@MapReduce
		public Collection<Integer> listTest3(){
			return null;
		}
		
		@MapReduce
		public Collection<Integer> listTest4(){
			HashSet<Integer> set = new HashSet<Integer>();
			set.add(1);
			return set;
		}
		
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void listTest1(){
		Collection<Integer> agTest1 = agTest.listTest1();
		Assert.assertTrue(agTest1.size() == 2);
	}
	
	@Test
	public void listTest2(){
		Collection<Integer> agTest1 = agTest.listTest2(1);
		Assert.assertTrue(agTest1.size() == 2);
	}
	
	@Test
	public void listTest3(){
		boolean exception = false;
		try {
			agTest.listTest3();
		} catch (Exception e) {
			exception = true;
		}
		Assert.assertTrue(exception);
	}
	
	@Test
	public void listTest4(){
		Collection<Integer> listTest4 = agTest.listTest4();
		Assert.assertTrue(listTest4.size() == 2);
	}
	
	
}
