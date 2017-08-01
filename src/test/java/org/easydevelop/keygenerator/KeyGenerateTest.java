package org.easydevelop.keygenerator;

import java.util.concurrent.atomic.AtomicInteger;

import org.easydevelop.business.TestApplicationConfig;
import org.easydevelop.business.domain.User;
import org.easydevelop.business.domain.UserOrder;
import org.easydevelop.keygenerator.annotation.KeyGenerate;
import org.easydevelop.keygenerator.annotation.KeyInject;
import org.easydevelop.keygenerator.strategy.KeyGenerateStrategy;
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
@SpringBootTest(classes={KeyGenerateTest.class,TestApplicationConfig.class})
@Configuration
public class KeyGenerateTest {
	
	@KeyGenerate(defaultKeyEls={"[order].orderId"},defaultStrategy="@intGenerator")
	@Service
	public static class KeyTest{
		
		
		@KeyInject
		public Integer parameterFieldInject(UserOrder order){
			return order.getOrderId();
		}
		
		@KeyInject(keyEls="[orderId]")
		public Integer parameterReplace(String abc,Integer orderId){
			return orderId;
		}
		
		@KeyInject(keyEls="[testUser].name",strategy="@stringGenerator")
		public String strParameterFieldInject(User testUser){
			return testUser.getName();
		}
		
		@KeyInject(keyEls={"[testUser].userId","[testUser].name"},strategy="@multiFieldGenerator")
		public void multiParameterFieldInject(User testUser){
		}
		
		@KeyInject(allowCallerDefinedKeyValue=1)
		public Integer alloUserDefinedKeyTest(UserOrder order){
			return order.getOrderId();
		}
		
		@KeyInject(keyEls="[testUser].name",strategy="@stringGenerator",strategyMetadataEls="[testUser].userId")
		public String metadataCheck(User testUser){
			return testUser.getName();
		}
		
	}
	
	public static final String INT_GENERATOR = "intGenerator";
	@Service(INT_GENERATOR)
	public static class IntKeyGenerateStrategy implements KeyGenerateStrategy{
		
		AtomicInteger atomInt = new AtomicInteger(0);

		@Override
		public Object[] generateKey(Object[] metaData) {
			return new Object[]{atomInt.incrementAndGet()};
		}
	}
	
	public static final String STRING_GENERATOR = "stringGenerator";
	@Service(STRING_GENERATOR)
	public static class StringKeyGenerateStrategy implements KeyGenerateStrategy{
		
		AtomicInteger atomInt = new AtomicInteger(0);

		@Override
		public Object[] generateKey(Object[] metaData) {
			if(metaData.length == 0){
				return new Object[]{"userName-" + atomInt.incrementAndGet()};
			}else{
				return new Object[]{metaData[0]};
			}
		}
	}
	
	public static final String MULTI_FIELD_GENERATOR = "multiFieldGenerator";
	@Service(MULTI_FIELD_GENERATOR)
	public static class MultiFiledKeyGenerateStrategy implements KeyGenerateStrategy{
		
		AtomicInteger atomInt = new AtomicInteger(0);

		@Override
		public Object[] generateKey(Object[] metaData) {
			return new Object[]{atomInt.incrementAndGet(),"userName-" + atomInt.incrementAndGet()};
		}
	}
	
	
	@Autowired
	private KeyTest keyTest;
	
	@Test
	public void parameterReplace(){
		Integer inject = keyTest.parameterReplace(null, null);
		Assert.assertTrue(inject != null);
	}
	
	@Test
	public void parameterFieldInject(){
		UserOrder order = new UserOrder();
		Integer orderId = keyTest.parameterFieldInject(order);
		Assert.assertTrue(orderId != null);
	}
	
	@Test
	public void strParameterFieldInject(){
		User user = new User();
		String userName = keyTest.strParameterFieldInject(user);
		Assert.assertTrue(userName != null);
	}
	
	@Test
	public void multiParameterFieldInject(){
		User user = new User();
		keyTest.multiParameterFieldInject(user);
		Assert.assertTrue(user.getName() != null && user.getUserId() != null);
	}
	
	@Test
	public void alloUserDefinedKeyTest(){
		UserOrder order = new UserOrder();
		order.setAmount(1000l);
		order.setOrderId(1000);
		order.setUserId(1);
		keyTest.alloUserDefinedKeyTest(order);
		Assert.assertTrue(order.getOrderId().equals(1000));
	}
	
	@Test
	public void metadataCheck(){
		User order = new User();
		order.setUserId(1);
		keyTest.metadataCheck(order);
		Assert.assertTrue(order.getName().equals("1"));
	}
	
	
}
