package com.sample.examples.db2.common;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

@Component
public class AreaRoutingDataSource extends AbstractRoutingDataSource {

	private UsernamePasswordCredentials credentials;
	
	@Override
	protected Object determineCurrentLookupKey() {
		return LayerContextHolder.getAreaType();
	}
	@Override
	public Connection getConnection() throws SQLException {
		return super.getConnection(credentials.getUserName(), credentials.getPassword());
	}	
	public void setCredentials(UsernamePasswordCredentials aCredentials) {
		credentials = aCredentials;
	}
	
}