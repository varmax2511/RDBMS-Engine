/**
 * 
 */
package edu.buffalo.www.cse4562.preprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;

/**
 * Precomputes certain details of the table
 * 
 * - cardinality
 * - primary & candidate keys
 * - min and max of indexable columns
 * - kind of indexing required
 * 
 * For now just creating indices based on range assuming equal distribution
 * 
 * h(x) = x/1000
 * 
 * @author Sneha Mehta
 *
 */
public class Preprocessor {
  
  //configurable bucket count
  public static int BUCKETS=10;
  
  
  public static void preprocess(TableSchema tableSchema, List<Index> indices) throws IOException{
    
     FileReader reader = new FileReader(ApplicationConstants.DATA_DIR_PATH + tableSchema.getTableName()
    + ApplicationConstants.SUPPORTED_FILE_EXTENSION);
     BufferedReader br1 = new BufferedReader(reader);
     
    TableStats stats = new TableStats();
    HashMap<Integer, Pair<Integer,Integer>> columnStats = new HashMap<>();
    
    //This nonsense works only when there is a composite primary key of form PRIMARY KEY (L_ORDERKEY, L_LINENUMBER)
    if(indices != null) {
    for(Index index: indices) {
      if(index.getType().equals("PRIMARY KEY")) {
        //add column details to the map
        
        //:TODO ignoring composite key for now
        index.getColumnsNames();
      }
    }
    }
    // Checking for simple primary key if not composite
      for(ColumnDefinition colDef: tableSchema.getColumnDefinitions()) {
        if(colDef.getColumnSpecStrings()!=null && (colDef.getColumnSpecStrings().get(0).equals("PRIMARY")|| colDef.getColumnSpecStrings().get(0).equals("REFERENCES"))) {
          //add column details to the map
          SchemaManager.addIndexToColumn(tableSchema.getTableName(), colDef.getColumnName());
          //System.out.println(tableSchema.getTableName()+""+colDef.getColumnName());
          //System.out.println("sgd " + colDef.getColumnName());
        }
      }
    
    int cardinality=0;
    Map<Integer,Integer> col2Index = SchemaManager.getIndexedColumnsMap(SchemaManager.getTableId(tableSchema.getTableName()));

    //Phase 1: Scan for stats
    while(br1.ready() && col2Index!=null) {
      cardinality++;
      //br1.readLine();
      String line = br1.readLine();
      final String[] record = line.split("\\|");
      for(Integer columnId : col2Index.keySet()) {
        int val=Integer.parseInt(record[columnId-1]);
        if(columnStats.get(columnId)==null) {
          columnStats.put(columnId,new Pair(val, val));
          continue;
        }
        //checking for max
        if(columnStats.get(columnId).getKey()<val){
          columnStats.put(columnId, new Pair<Integer, Integer>(val, columnStats.get(columnId).getValue()));
        }
        //checking for min
        if(columnStats.get(columnId).getValue()>val) {
          columnStats.put(columnId, new Pair<Integer, Integer>(columnStats.get(columnId).getKey(), val));
        }

      }
    }
    //reset the iterator
    reader.close();
    stats.setCardinality(cardinality);
    stats.setColumnStats(columnStats);
    //System.out.println(tableSchema.getTableName() + "   "+ cardinality);

    tableSchema.setTableStats(stats);
    reader = new FileReader(ApplicationConstants.DATA_DIR_PATH + tableSchema.getTableName() + ApplicationConstants.SUPPORTED_FILE_EXTENSION);

    BufferedReader br2 = new BufferedReader(reader);

    //Phase 2: Scan for bucketing
    //int count=0, bucketNumber= 0;
    

    while(br2.ready() && col2Index!=null) {
//      count++;
//      if(count == BUCKETSIZE) {
//        //reset count
//        count = 0;
//        bucketNumber++;
//      }
//      addToBucket(tableSchema.getTableName(), br2.readLine(), bucketNumber);
      String line = br2.readLine();
      final String[] record = line.split("\\|");
      
      for(Integer columnId : col2Index.keySet()) {
        int bucket = hashedValue(columnStats.get(columnId).getKey(),columnStats.get(columnId).getValue(),Integer.parseInt(record[columnId-1]));
        addToBucket(tableSchema.getTableName(), col2Index.get(columnId), bucket, line);
      }

      
    }
    br1.close();
    br2.close();
    reader.close();
    System.gc();
  }

  private static void addToBucket(String tableName, int indexValue,int bucket ,String line) {
    try {
      File file = new File(getDestination(tableName,indexValue, bucket));
      if(!file.exists()) file.createNewFile();
      //append = true
      FileWriter fwriter = new FileWriter(file,true);
      BufferedWriter writer = new BufferedWriter(fwriter);
      writer.write(line+"\n");
      writer.close();
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String getDestination(String tableName, int indexNumber, int bucketNumber) {
   return ApplicationConstants.INDEX_DIR_PATH + indexNumber+"_" + bucketNumber+"_"+tableName + ApplicationConstants.SUPPORTED_FILE_EXTENSION;
  }
  
  public static int hashedValue(int max, int min, int columnValue) {
    int bucketSize = (int)Math.ceil((double)(max-min+1)/BUCKETS);
    return (columnValue-min)/bucketSize;
  }

}
