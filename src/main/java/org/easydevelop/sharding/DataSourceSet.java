package org.easydevelop.sharding;

import java.util.List;

import javax.sql.DataSource;

/** 
* @author xudeyou 
*/
public class DataSourceSet {
	
	private String datasourceSetName;
	
	private List<DataSource> masterDataSources;
	
	private List<List<DataSource>> slaveDataSources;
	
	public DataSourceSet(String datasourceSetName, List<DataSource> masterDataSources ,List<List<DataSource>> slaveDataSources) {
		super();
		this.datasourceSetName = datasourceSetName;
		this.masterDataSources = masterDataSources;
		this.slaveDataSources = slaveDataSources;
		if(slaveDataSources != null && slaveDataSources.size() != masterDataSources.size()){
			throw new IllegalArgumentException("the master number should be the same with the slave list's number");
		}
	}
	
	public DataSourceSet(String datasourceSetName, List<DataSource> masterDataSources) {
		this(datasourceSetName, masterDataSources, null);
	}

	public String getDatasourceSetName() {
		return datasourceSetName;
	}

	public List<DataSource> getMasterDataSources() {
		return masterDataSources;
	}

	public List<List<DataSource>> getSlaveDataSources() {
		return slaveDataSources;
	}

	public void setSlaveDataSources(List<List<DataSource>> slaveDataSources) {
		this.slaveDataSources = slaveDataSources;
	}
}
