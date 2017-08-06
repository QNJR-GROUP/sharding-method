package org.easydevelop.sharding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/** 
* @author xudeyou 
*/
public class ShardingRoutingDataSource extends AbstractRoutingDataSource {
	
	@Autowired
	private List<DataSourceSet> dataSourceSetList;
	
	private Map<String,DataSourceSet> mapDataSourceSet;
	
	@PostConstruct
	private void init(){
		
		HashMap<Object, Object> mapDataSource = new HashMap<>();
		mapDataSourceSet = new HashMap<>();
		for(DataSourceSet set:dataSourceSetList){
			String datasourceSetName = set.getDatasourceSetName();
			mapDataSourceSet.put(datasourceSetName, set);
			List<List<DataSource>> slaveDataSources = set.getSlaveDataSources();
			for(int shardingPartitionPos = 0; shardingPartitionPos < set.getMasterDataSources().size(); shardingPartitionPos++){
				//master
				mapDataSource.put(calcLookupKey(datasourceSetName, shardingPartitionPos,null), set.getMasterDataSources().get(shardingPartitionPos));
				
				//slaves
				List<DataSource> list = slaveDataSources.get(shardingPartitionPos);
				if(list != null && list.size() != 0){
					for(int slavePosition = 0; slavePosition < list.size(); slavePosition++){
						mapDataSource.put(calcLookupKey(datasourceSetName, shardingPartitionPos,slavePosition),list.get(slavePosition) );
					}
				}
			}
		}
		
		super.setTargetDataSources(mapDataSource);
	}
	
	private ThreadLocal<String> currentLookupDsSet = new ThreadLocal<>();
	private ThreadLocal<Integer> currentLookupDsSequence = new ThreadLocal<>();
	private ThreadLocal<Integer> currentSlavePosition = new ThreadLocal<>();
	
	
	public DataSourceSet getDataSourcesByDsSetName(String name){
		return mapDataSourceSet.get(name);//TODO generate a unmodifiable one
	}
	
	private String getCurrentLookupKey() {
		String dsSet = currentLookupDsSet.get();
		Integer sequence = currentLookupDsSequence.get();
		Integer slavePosition = currentSlavePosition.get();
		
		if(dsSet != null && sequence != null){
			return calcLookupKey(dsSet, sequence,slavePosition);
		}else{
			return null;
		}
	}
	
	
	

	public String getCurrentLookupDsSet() {
		return currentLookupDsSet.get();
	}




	public Integer getCurrentLookupDsSequence() {
		return currentLookupDsSequence.get();
	}
	
	public Integer getCurrentSlavePosition() {
		return currentSlavePosition.get();
	}




	public void setCurrentLookupKey(String dbSetName,Integer shardingPartitionPos,Integer slavePositon) {
		Assert.isTrue(dbSetName == null || !StringUtils.isEmpty(dbSetName),"can not be empty!");
		currentLookupDsSet.set(dbSetName);
		currentLookupDsSequence.set(shardingPartitionPos);
		currentSlavePosition.set(slavePositon);
	}

	private String calcLookupKey(String dbSetName, int shardingPartitionPos,Integer slavePositon) {
		return dbSetName+"-"+shardingPartitionPos+(slavePositon==null?"":("-" + slavePositon));
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return getCurrentLookupKey();
	}
	
	@Override
	@Deprecated
	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		throw new RuntimeException("you can not call this method by yourself!");
	}
	
	
}
