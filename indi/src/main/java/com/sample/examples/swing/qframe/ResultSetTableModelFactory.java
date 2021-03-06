package com.sample.examples.swing.qframe;


import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Component;

import com.sample.examples.db2.common.Area;
import com.sample.examples.db2.common.AreaRoutingDataSource;
import com.sample.examples.db2.common.LayerContextHolder;

/**
 * This class encapsulates a JDBC database connection and, given a SQL query
 * as a string, returns a ResultSetTableModel object suitable for display
 * in a JTable Swing component
 **/
@Component
public class ResultSetTableModelFactory {

	@Autowired
	private @Qualifier("dataSource") BasicDataSource dataSource;
	@Autowired
	private @Qualifier("areaRoutingDataSource") AreaRoutingDataSource areaRoutingDataSource;
	@Autowired(required=true)
	private SQLExceptionTranslator customFormErrorTranslator;

	private JdbcTemplate jdbcTemplate = null;
	private JdbcTemplate routingTemplate = null;
	/** The constructor method uses the arguments to create db Connection 
	 * @throws Exception */
	public ResultSetTableModelFactory(String driverClassName, String dbname, String username, String password) throws Exception {
		Properties properties = new Properties();
		properties.setProperty("username", username);
		properties.setProperty("driverClassName", driverClassName);
		properties.setProperty("password", password);
		properties.setProperty("url", dbname);
		dataSource = (BasicDataSource) BasicDataSourceFactory.createDataSource(properties);
		init();
	}

	@PostConstruct
	protected void init() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		routingTemplate = new JdbcTemplate(areaRoutingDataSource);

		jdbcTemplate.setExceptionTranslator(customFormErrorTranslator);
		routingTemplate.setExceptionTranslator(customFormErrorTranslator);
	}

	public ResultSetTableModelFactory() {

	}

	/**
	 * This method takes a SQL query, passes it to the database, obtains the
	 * results as a ResultSet, and returns a ResultSetTableModel object that
	 * holds the results in a form that the Swing JTable component can use.
	 * @throws SQLException 
	 **/
	public ResultSetTableModel getResultSetTableModel(String query) throws SQLException {
		if ((StringUtils.contains(StringUtils.upperCase(query), "LDBA") || LayerContextHolder.getAreaType() == Area.LDBA)&& !StringUtils.contains(StringUtils.upperCase(query), "WITH UR")
				&& !StringUtils.contains(StringUtils.upperCase(query), "WITH HIRR")) {
			throw new IllegalStateException("Please use select query with uncommitted reads."); 
		} else if (jdbcTemplate != null) {
			return new ResultSetTableModel(routingTemplate.queryForRowSet(query));
			// return new ResultSetTableModel(jdbcTemplate.queryForRowSet(query));
		} else {
			throw new IllegalStateException("Connection already closed.");
		}
	}
	
	public void checkDBCredentials(String anUID, String aPWD) throws SQLException {	
		dataSource.setUsername(anUID);
		dataSource.setPassword(aPWD);
		DataSourceUtils.doReleaseConnection(DataSourceUtils.doGetConnection(dataSource), dataSource);
		init();
		areaRoutingDataSource.setCredentials(new UsernamePasswordCredentials(anUID, aPWD));
	}
}
