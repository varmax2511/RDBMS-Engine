package edu.buffalo.www.cse4562.model;

/**
 * 
 * @author snehameh
 *
 * @param <K>
 * @param <V>
 */
public class Pair<K, V> {

  private final K key;
  private final V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

}
