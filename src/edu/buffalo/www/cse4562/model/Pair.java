package edu.buffalo.www.cse4562.model;

/**
 * 
 * @author Sneha Mehta
 *
 * @param <K>
 * @param <V>
 */
public class Pair<K, V> implements Cloneable{

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pair other = (Pair) obj;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.key == null ? "" : this.key);
    builder.append("|");
    builder.append(this.value == null ? "" : this.value);
    return builder.toString();
  }
  
  @Override
  protected Object clone() throws CloneNotSupportedException {
    final Pair<K, V> pair= new Pair<K, V>(this.getKey(), this.getValue());
    return pair;
  }
}
