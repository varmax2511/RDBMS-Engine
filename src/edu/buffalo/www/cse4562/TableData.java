package edu.buffalo.www.cse4562;

import java.util.List;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

public class TableData {
	private String tableName;
	private List<ColumnDefinition> columnDefinitions;

	TableData(String tableName,List<ColumnDefinition> columnDefinitions) {
		this.setTableName(tableName);
		this.setColumnDefinitions(columnDefinitions);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<ColumnDefinition> getColumnDefinitions() {
		return columnDefinitions;
	}

	public void setColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
		this.columnDefinitions = columnDefinitions;
	}
	
	
}
