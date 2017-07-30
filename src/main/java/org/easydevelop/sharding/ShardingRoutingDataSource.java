package org.easydevelop.sharding;

import java.util.ArrayList;
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
			for(int i = 0; i < set.getDataSources().size(); i++){
				mapDataSource.put(calcLookupKey(datasourceSetName, i), set.getDataSources().get(i));
			}
		}
		
		super.setTargetDataSources(mapDataSource);
	}
	
	private ThreadLocal<String> currentLookupDsSet = new ThreadLocal<>();
	private ThreadLocal<Integer> currentLookupDsSequence = new ThreadLocal<>();
	
	
	public List<DataSource> getDataSourcesByDsSetName(String name){
		DataSourceSet dataSourceSet = mapDataSourceSet.get(name);
		if(dataSourceSet == null){
			return null;
		}
		
		return new ArrayList<>(dataSourceSet.getDataSources());
	}
	
	private String getCurrentLookupKey() {
		String dsSet = currentLookupDsSet.get();
		Integer sequence = currentLookupDsSequence.get();
		
		if(dsSet != null && sequence != null){
			return calcLookupKey(dsSet, sequence);
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




	public void setCurrentLookupKey(String dbSetName,Integer pos) {
		Assert.isTrue(dbSetName == null || !StringUtils.isEmpty(dbSetName),"can not be empty!");
		currentLookupDsSet.set(dbSetName);
		currentLookupDsSequence.set(pos);
	}

	private String calcLookupKey(String dbSetName, int pos) {
		return dbSetName+"-"+pos;
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
