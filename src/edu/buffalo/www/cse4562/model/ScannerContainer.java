package edu.buffalo.www.cse4562.model;

public class ScannerContainer implements Container{

  
  Class<? extends Node> sender;
  Class<? extends Node> receiver;
  private String tableName;
  private int columnId;
  public int getColumnId() {
    return columnId;
  }

  public void setColumnId(int columnId) {
    this.columnId = columnId;
  }

  private int indexNo;
  private int value;
  
  public ScannerContainer(Class sender, Class receiver,String tableName, int columnId,int indexNo) {
    this.sender = sender;
    this.receiver = receiver;
    this.tableName = tableName;
    this.columnId = columnId;
    this.indexNo = indexNo;
  }

  public Class<? extends Node> getSender() {
    return sender;
  }

  public void setSender(Class<? extends Node> sender) {
    this.sender = sender;
  }

  public Class<? extends Node> getReceiver() {
    return receiver;
  }

  public void setReceiver(Class<? extends Node> receiver) {
    this.receiver = receiver;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public int getIndexNo() {
    return indexNo;
  }

  public void setIndexNo(int indexNo) {
    this.indexNo = indexNo;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
  
  
  
}
