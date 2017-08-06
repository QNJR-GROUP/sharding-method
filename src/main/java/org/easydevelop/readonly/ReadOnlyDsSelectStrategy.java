package org.easydevelop.readonly;
/** 
* @author xudeyou 
*/
public interface ReadOnlyDsSelectStrategy {
	Integer select(int partionNumber,int slaveCount);
}
