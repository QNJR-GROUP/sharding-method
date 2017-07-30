package org.easydevelop.business.dao;

import org.easydevelop.business.domain.UserOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 
* @author xudeyou 
*/
@Component
public class OrderDaoImpl {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public void saveOrder(UserOrder order){
		int update = jdbcTemplate.update("INSERT INTO `user_order` (`user_id`, `order_id`, `amount`) VALUES (?, ?, ?);",order.getUserId(),order.getOrderId(),order.getAmount());
		if(update != 1){
			throw new RuntimeException("update error!");
		}
	}

}
