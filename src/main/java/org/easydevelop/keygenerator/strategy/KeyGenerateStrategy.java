package org.easydevelop.keygenerator.strategy;
/** 
* @author xudeyou 
*/
public interface KeyGenerateStrategy {
	
	String getStrategyName();
	Object[] generateKey(Object[] metadata);

}
