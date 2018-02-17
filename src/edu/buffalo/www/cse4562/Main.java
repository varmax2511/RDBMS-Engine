package edu.buffalo.www.cse4562;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import edu.buffalo.www.cse4562.operator.Node;
import edu.buffalo.www.cse4562.operator.SelectionOperator;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Select;

public class Main {

    public static void main (String[] args) throws FileNotFoundException, ParseException {
        System.out.println("Hello, World");
        
        HashMap<String,TableData> tables= new HashMap<>();
        
        FileReader input = new FileReader(args[0]);
        CCJSqlParser parser = new CCJSqlParser(input);
        Statement statement = parser.Statement();
        while(statement!=null) {
        	if(statement instanceof CreateTable) {
        		CreateTable createStatement = (CreateTable) statement;
        		String tableName = createStatement.getTable().getName();
        		
        		TableData newTable = new TableData(tableName,createStatement.getColumnDefinitions());
        		tables.put(tableName, newTable);
        	}
        	else if(statement instanceof Select) {
        		SelectionOperator.evaluateSelect((Select)statement, tables);
        		
        	}
        }
    }
   
}
