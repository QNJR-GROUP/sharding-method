package org.easydevelop.business.service;

import java.util.List;

import org.easydevelop.business.TestApplicationConfig;
import org.easydevelop.business.dao.UserDaoImpl;
import org.easydevelop.business.domain.User;
import org.easydevelop.generateid.annotation.GenerateId;
import org.easydevelop.select.annotation.SelectDataSource;
import org.easydevelop.sharding.annotation.ShardingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 
* @author xudeyou 
*/
@Service
@ShardingContext(dataSourceSet="orderSet",shardingKeyEls="[user].userId",shardingStrategy=TestApplicationConfig.BY_USER_ID_MOD,generateIdStrategy=TestApplicationConfig.INT_INCREASE,generateIdEls="[user].userId")
public class UserServceImpl {
	
	@Autowired
	private UserDaoImpl userDao;
	
	@Transactional
	@SelectDataSource
	public void updateUser(User user){
		userDao.updateUser(user);
	}
	
	@Transactional
	@SelectDataSource
	@GenerateId
	public void saveUser(User user){
		userDao.saveUser(user);
	}
	
	@Transactional
	@SelectDataSource(keyNameEls="[userId]")
	public void deleteUser(int userId){
		userDao.deleteUser(userId);
	}
	
	@Transactional(readOnly=true)
	@SelectDataSource(keyNameEls="[userId]")
	public User findUser(int userId){
		return userDao.findUser(userId);
	}
	
	public List<User> findAllUsers(){
		return userDao.findAllUsers();
	}
	
	public double calcUserAvgAge(){
		List<User> allUsers = userDao.findAllUsers();
		return allUsers.stream().mapToInt(u->u.getUserId()).average().getAsDouble();
	}
	
	public int deleteAllUsers(){
		return userDao.deleteAllUsers();
	}

}
