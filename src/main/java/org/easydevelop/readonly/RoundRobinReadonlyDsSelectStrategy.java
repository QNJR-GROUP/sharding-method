package org.easydevelop.readonly;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** 
* @author xudeyou 
*/
public class RoundRobinReadonlyDsSelectStrategy implements ReadOnlyDsSelectStrategy {
	
	private static final int CHANGE_THRESHOLD = Integer.MAX_VALUE -100000;
	private ConcurrentHashMap<Integer, AtomicInteger> map = new ConcurrentHashMap<>();

	@Override
	public Integer select(int partionNumber, int slaveCount) {
		if(slaveCount == 0){
			return null;
		}
		
		AtomicInteger computeIfAbsent = map.computeIfAbsent(partionNumber, k->new AtomicInteger(0));
		
		int incrementAndGet = computeIfAbsent.incrementAndGet();
		
		if(incrementAndGet > CHANGE_THRESHOLD){
			boolean compareAndSet = computeIfAbsent.compareAndSet(incrementAndGet, 0);
			if(!compareAndSet){
				//ignore it,do it in next loop
			}
		}
		
		return incrementAndGet % slaveCount;
	}
	
}
