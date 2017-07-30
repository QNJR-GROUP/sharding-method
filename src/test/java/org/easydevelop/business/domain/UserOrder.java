package org.easydevelop.business.domain;

import lombok.Data;

/** 
* @author xudeyou 
*/
@Data
public class UserOrder {
	private Integer userId;
	private Integer orderId;
	private Long amount;
}
