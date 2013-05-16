package com.sample.examples.db2.common;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.stereotype.Component;

@Component("customFormErrorTranslator")
public class CustomFormErrorTranslator extends SQLErrorCodeSQLExceptionTranslator {
	@Autowired
	private Properties props; 
	@Override
	public DataAccessException translate(String task, String sql, SQLException sqlEx) {
		int errorCode = sqlEx.getErrorCode();
		String errorString = StringUtils.EMPTY;
		if (props.containsKey(String.valueOf(errorCode))) {
			errorString = props.getProperty(String.valueOf(errorCode));
		}		
		SQLException exception = new SQLException(sqlEx.getMessage().concat("\n ").concat(errorString), sqlEx.getSQLState());
		DataAccessException dataAccessException = super.translate(task, sql, exception);
		return dataAccessException;
	}
};