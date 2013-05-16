package com.sample.examples.swing.qframe;


import java.sql.SQLException;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

/**
 * This class takes a JDBC ResultSet object and implements the TableModel
 * interface in terms of it so that a Swing JTable component can display the
 * contents of the ResultSet.  Note that it requires a scrollable JDBC 2.0 
 * ResultSet.  Also note that it provides read-only access to the results
 **/
public class ResultSetTableModel implements TableModel {

	private static final String NA = "<N/A>";
	private static final String SEPERATOR = ".";

	private SqlRowSet results; // The ResultSet to interpret

	private int numcols, numrows; // How many rows and columns in the table

	private SqlRowSetMetaData sqlRowSetMetaData;

	ResultSetTableModel(SqlRowSet sqlRowSet) throws SQLException {
		// this(((ResultSetWrappingSqlRowSet) sqlRowSet).getResultSet());
		results = ((ResultSetWrappingSqlRowSet) sqlRowSet);
		sqlRowSet.last(); // Move to last row
		numrows = sqlRowSet.getRow() + 1; // How many rows?
		sqlRowSetMetaData = results.getMetaData();
		numcols = sqlRowSetMetaData.getColumnCount(); // How many columns?
	}

	protected String getCatalogueName() {
		return sqlRowSetMetaData.getCatalogName(1);
	}

	protected String getSchemaName() {
		return sqlRowSetMetaData.getSchemaName(1);
	}

	protected String getTableName() {
		return sqlRowSetMetaData.getTableName(1);
	}
	
	protected String getConnectionDetails() {
		return getCatalogueName().concat(SEPERATOR).concat(getSchemaName()).concat(SEPERATOR).concat(getTableName());
	}

	// These two TableModel methods return the size of the table
	public int getColumnCount() {
		return numcols;
	}

	public int getRowCount() {
		return numrows;
	}

	// This TableModel method returns columns names from the ResultSetMetaData
	public String getColumnName(int column) {
		return sqlRowSetMetaData.getColumnLabel(column + 1);
	}

	// This TableModel method specifies the data type for each column.  
	// We could map SQL types to Java types, but for this example, we'll just
	// convert all the returned data to strings.
	public Class getColumnClass(int column) {
		return String.class;
	}

	/**
	 * This is the key method of TableModel: it returns the value at each cell
	 * of the table.  We use strings in this case.  If anything goes wrong, we
	 * return the exception as a string, so it will be displayed in the table.
	 * Note that SQL row and column numbers start at 1, but TableModel column
	 * numbers start at 0.
	 **/
	public Object getValueAt(int row, int column) {
		if (row == 0) {
			return getColumnName(column);
		} else {
			results.absolute(row); // Go to the specified row
			Object o = results.getObject(column + 1); // Get value of the column
			if (o == null)
				return null;
			else
				return StringUtils.trimToEmpty(o.toString()); // Convert it to a string
		}		
	}

	// Our table isn't editable
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	// Since its not editable, we don't need to implement these methods
	public void setValueAt(Object value, int row, int column) {
	}

	public void addTableModelListener(TableModelListener l) {
	}

	public void removeTableModelListener(TableModelListener l) {
	}
	
	
	public static TableModel EMPTY_MODEL = new TableModel() {

		public void addTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub
			
		}

		public Class<?> getColumnClass(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getColumnName(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getRowCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub
			
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			
		}
		
	};
}
