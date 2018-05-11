/**
 * 
 */
package edu.buffalo.www.cse4562.util;

import java.util.Comparator;

import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;

/**
 * @author Sneha Mehta
 *
 */
public class JoinComparator implements Comparator<Join> {

  @Override
  public int compare(Join o1, Join o2) {
    if(!(o1.getRightItem() instanceof Table) || !(o2.getRightItem() instanceof Table) ) {
      TableSchema t1=SchemaManager.getTableSchema(((Table) o1.getRightItem()).getName());
      TableSchema t2 =SchemaManager.getTableSchema(((Table) o2.getRightItem()).getName());
      return ((Integer)t1.getTableStats().getCardinality()).compareTo((Integer)t2.getTableStats().getCardinality());

    }
    return 0;
  }

}
