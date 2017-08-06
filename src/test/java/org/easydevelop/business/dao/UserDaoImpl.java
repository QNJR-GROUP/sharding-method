package org.easydevelop.business.dao;

import java.util.List;

import org.easydevelop.business.TestApplicationConfig;
import org.easydevelop.business.domain.User;
import org.easydevelop.mapreduce.annotation.MapReduce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/** 
* @author xudeyou 
*/
@Component
public class UserDaoImpl {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public void saveUser(User user){
		int update = jdbcTemplate.update("INSERT INTO `user` (`user_id`, `name`) VALUES (?, ?)",user.getUserId(),user.getName());
		Assert.isTrue(update == 1,"it should be inserted!");
	}
	
	public void updateUser(User user){
		int update = jdbcTemplate.update("UPDATE `user` SET `name`=? WHERE `user_id`=?;",user.getName(),user.getUserId());
		Assert.isTrue(update == 1,"it should be updated!");
	}

	public void deleteUser(int userId) {
		int update = jdbcTemplate.update("DELETE FROM `user` WHERE `user_id`=?;",userId);
		Assert.isTrue(update == 1,"it should be deleted!");
	}
	
	private BeanPropertyRowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);
	public User findUser(int userId){
		return jdbcTemplate.queryForObject("SELECT * FROM user WHERE user_id = ?", new Object[]{userId}, rowMapper);
	}
	
	@Transactional
	@MapReduce(reduceStrategy=TestApplicationConfig.AGGREGATION_USER_ORDER_BY_USER_ID)
	public List<User> findAllUsersByMaster(){
		return jdbcTemplate.query("SELECT * FROM user", rowMapper);
	}
	
	@Transactional(readOnly=true)
	@MapReduce(reduceStrategy=TestApplicationConfig.AGGREGATION_USER_ORDER_BY_USER_ID)
	public List<User> findAllUsers(){
		return jdbcTemplate.query("SELECT * FROM user", rowMapper);
	}
	
	@Transactional
	@MapReduce(reduceStrategy=TestApplicationConfig.UPDATE_COUNT_ADD)
	public int deleteAllUsers(){
		int update = jdbcTemplate.update("delete from user");
		System.out.println(update);
		return update;
	}

}
