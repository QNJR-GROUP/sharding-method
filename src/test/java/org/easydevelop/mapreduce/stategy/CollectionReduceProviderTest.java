package org.easydevelop.mapreduce.stategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
@SpringBootTest(classes={CollectionReduceProviderTest.class,TestApplicationConfig.class})
@Configuration
public class CollectionReduceProviderTest {
	
	
	@Service("agTest")
	@ShardingContext(dataSourceSet="orderSet")
	public static class AgTest{
		
		@MapReduce
		public List<Integer> listTest1(){
			return Arrays.asList(1);
		}
		
		@MapReduce
		public List<Integer> listTest2(int value){
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			arrayList.add(value);
			return arrayList;
		}
		
		@MapReduce
		public List<Integer> listTest3(){
			return null;
		}
		
		@MapReduce
		public LinkedList<Integer> listTest4(){
			LinkedList<Integer> arrayList = new LinkedList<Integer>();
			arrayList.add(1);
			return arrayList;
		}
		
	}
	
	
	@Autowired
	private AgTest agTest;
	
	@Test
	public void listTest1(){
		List<Integer> agTest1 = agTest.listTest1();
		Assert.assertTrue(agTest1.size() == 2);
	}
	
	@Test
	public void listTest2(){
		List<Integer> agTest1 = agTest.listTest2(1);
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
		LinkedList<Integer> listTest4 = agTest.listTest4();
		Assert.assertTrue(listTest4.size() == 2);
	}
	
	
}
