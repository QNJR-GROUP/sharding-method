package org.easydevelop.sharding;

import java.util.List;

import javax.sql.DataSource;

/** 
* @author xudeyou 
*/
public class DataSourceSet {
	
	private String datasourceSetName;
	
	private List<DataSource> dataSources;
	
	public DataSourceSet(String datasourceSetName, List<DataSource> dataSources) {
		super();
		this.datasourceSetName = datasourceSetName;
		this.dataSources = dataSources;
	}

	public String getDatasourceSetName() {
		return datasourceSetName;
	}

	public List<DataSource> getDataSources() {
		return dataSources;
	}
}
