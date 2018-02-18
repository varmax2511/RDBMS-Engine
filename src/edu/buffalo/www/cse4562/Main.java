package edu.buffalo.www.cse4562;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Select;

public class Main {

  public static void main(String[] args)
      throws Throwable {
    System.out.println(ApplicationConstants.BASH);

    final CCJSqlParser parser = new CCJSqlParser(System.in);
    Statement statement = parser.Statement();

    while (statement != null) {
      if (statement instanceof CreateTable) {
        // if create statement
        QueryProcessor.processCreateQuery((CreateTable) statement);
      } else if (statement instanceof Select) {
        // if select statment
        for(Tuple tuple : QueryProcessor.processSelectQuery((Select) statement)){
          System.out.println(tuple);
        }
         
      }

      System.out.println(ApplicationConstants.BASH);
      statement = parser.Statement();

    }
  }

}
