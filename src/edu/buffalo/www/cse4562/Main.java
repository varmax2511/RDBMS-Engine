package edu.buffalo.www.cse4562;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import edu.buffalo.www.cse4562.util.StringUtils;
import edu.buffalo.www.cse4562.util.TreeProcessor;
import edu.buffalo.www.cse4562.util.TuplePrinter;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;

public class Main {

  /**
   * Entry
   * 
   * @param args
   * @throws Throwable
   */
  public static void main(String[] args) throws Throwable {
  //flush all existing index files
   /* File file = new File(ApplicationConstants.INDEX_DIR_PATH);      
    String[] files;    
        if(file.isDirectory()){
          files = file.list();
            for (int i=0; i<files.length; i++) {
                File file1 = new File(file, files[i]); 
                file1.delete();
            }
         }*/
        
    // prompt
    System.out.println(ApplicationConstants.BASH);
    System.out.flush();
    String dataPath = args[1];
    // use data path if specified
    if (StringUtils.isBlank(dataPath)) {
      ApplicationConstants.DATA_DIR_PATH = dataPath;
    }
        
    final CCJSqlParser parser = new CCJSqlParser(System.in);
    Statement statement = parser.Statement();

    while (statement != null) {

      try {
        // process query to generate Tree
        final QueryVisitor queryVisitor = new QueryVisitor();
        statement.accept(queryVisitor);

        // get the tree
        final Node root = queryVisitor.getRoot();

        // if a SELECT
        if (null != root) {

          final Iterator<Tuple> tupleItr = TreeProcessor.processTree(root)
              .iterator();
          while (tupleItr.hasNext()) {
            TuplePrinter.printTuple(tupleItr.next());
          } // while

        } // if

        // prompt
        System.out.println(ApplicationConstants.BASH);
        System.out.flush();
      } catch (Throwable t) {
        System.err.println("1|1" + "|" + t.getMessage());
//        // prompt
//        System.out.println(ApplicationConstants.BASH);
//        System.out.flush();
      }

      statement = parser.Statement();
    } // while

  }

}
