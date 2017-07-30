package org.easydevelop.business.service;

import java.util.List;

import org.easydevelop.business.TestApplicationConfig;
import org.easydevelop.business.dao.UserDaoImpl;
import org.easydevelop.business.domain.User;
import org.easydevelop.keygenerator.annotation.KeyGenerate;
import org.easydevelop.keygenerator.annotation.KeyInject;
import org.easydevelop.sharding.annotation.Sharding;
import org.easydevelop.sharding.annotation.ShardingMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 
* @author xudeyou 
*/
@Service
@Sharding(defaultDsSet="orderSet",defaultKeyEls="[user].userId",defaultStrategy=TestApplicationConfig.BY_USER_ID_MOD)
@KeyGenerate(defaultKeyEls="[user].userId",defaultStrategy=TestApplicationConfig.INT_INCREASE)
public class UserServceImpl {
	
	
	@Autowired
	private UserDaoImpl userDao;
	
	@Transactional
	@ShardingMethod
	public void updateUser(User user){
		userDao.updateUser(user);
	}
	
	@Transactional
	@ShardingMethod
	@KeyInject
	public void saveUser(User user){
		userDao.saveUser(user);
	}
	
	@Transactional
	@ShardingMethod(keyNameEls="[userId]")
	public void deleteUser(int userId){
		userDao.deleteUser(userId);
	}
	
	@Transactional
	@ShardingMethod(keyNameEls="[userId]")
	public User findUser(int userId){
		return userDao.findUser(userId);
	}
	
	public List<User> findAllUsers(){
		return userDao.findAllUsers();
	}
	
	public int deleteAllUsers(){
		return userDao.deleteAllUsers();
	}

}
