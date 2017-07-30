package org.easydevelop.business;

import java.util.List;

import org.easydevelop.business.domain.User;
import org.easydevelop.business.service.UserServceImpl;
import org.eclipse.jdt.internal.core.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;



/** 
* @author xudeyou 
*/
@RunWith(SpringRunner.class)
@SpringBootTest(classes={BusinessTest.class,TestApplicationConfig.class})
@Configuration
public class BusinessTest {
	
	@Autowired
	private UserServceImpl userService;
	
	@Test
	public void testBusiness(){
		
		int deleteAllUsers = userService.deleteAllUsers();
		System.out.println(deleteAllUsers);
		
		User user1 = new User();
		user1.setName("user1");
		userService.saveUser(user1);
		
		User user2 = new User();
		user2.setName("user2");
		userService.saveUser(user2);
		
		User user3 = new User();
		user3.setName("user3");
		userService.saveUser(user3);
		
		userService.deleteUser(user1.getUserId());
		
		user3.setName("user3-new");
		userService.updateUser(user3);
		
		User findUser = userService.findUser(user3.getUserId());
		Assert.isTrue(findUser.getName().equals("user3-new"));
		
		List<User> findAllUsers = userService.findAllUsers();
		Assert.isTrue(findAllUsers.size() == 2);
		
	}
	
	
}
