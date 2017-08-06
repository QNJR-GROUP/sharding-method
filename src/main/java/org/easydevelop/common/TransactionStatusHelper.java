package org.easydevelop.common;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;

/** 
* @author xudeyou 
*/
public class TransactionStatusHelper {
	
	@Autowired
	private TransactionAspectSupport transactionAspectSupport;
	
	public Boolean isMethodReadOnly(Method method, Class<?> clazz){
		
		TransactionAttribute transactionAttribute = transactionAspectSupport.getTransactionAttributeSource().getTransactionAttribute(method, clazz);
		if(transactionAttribute != null){
			return transactionAttribute.isReadOnly();
		}
		
		return null;
	}

}
