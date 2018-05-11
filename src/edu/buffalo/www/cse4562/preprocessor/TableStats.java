package edu.buffalo.www.cse4562.preprocessor;

import java.util.HashMap;

import edu.buffalo.www.cse4562.model.Pair;

public class TableStats {
  
  private int cardinality;
  private HashMap<Integer, Pair<Integer,Integer>> columnStats;
  
  public int getCardinality() {
    return cardinality;
  }
  public void setCardinality(int cardinality) {
    this.cardinality = cardinality;
  }
  public HashMap<Integer, Pair<Integer, Integer>> getColumnStats() {
    return columnStats;
  }
  public void setColumnStats(HashMap<Integer, Pair<Integer, Integer>> columnStats) {
    this.columnStats = columnStats;
  }
  
  

}
