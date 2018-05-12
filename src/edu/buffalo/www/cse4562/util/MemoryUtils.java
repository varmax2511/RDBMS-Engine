/**
 * 
 */
package edu.buffalo.www.cse4562.util;

/**
 * @author Sneha Mehta
 *
 */
public class MemoryUtils {
  
  public static long getAvailableMemory() {
    Runtime runtime = Runtime.getRuntime();
    return runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
  }
  
}
