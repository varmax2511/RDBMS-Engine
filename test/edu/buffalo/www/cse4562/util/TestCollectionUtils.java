package edu.buffalo.www.cse4562.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import net.sf.jsqlparser.expression.LongValue;

public class TestCollectionUtils {

  @Test
  public void testAreTuplesEmptyNegative() {
    final Collection<Tuple> tuples = new ArrayList<>();
    final ColumnCell colCell = new ColumnCell(new LongValue(1));
    final List<ColumnCell> cells = new ArrayList<>();
    cells.add(colCell);
    tuples.add(new Tuple(cells));
    tuples.add(new Tuple(new ArrayList<>()));
    tuples.add(new Tuple(new ArrayList<>()));
    tuples.add(new Tuple(new ArrayList<>()));

    assertEquals(false, CollectionUtils.areTuplesEmpty(tuples));

  }

  @Test
  public void testAreTuplesEmpty() {
    final Collection<Tuple> tuples = new ArrayList<>();
    tuples.add(new Tuple(new ArrayList<>()));
    tuples.add(new Tuple(new ArrayList<>()));
    tuples.add(new Tuple(new ArrayList<>()));

    assertEquals(true, CollectionUtils.areTuplesEmpty(tuples));

  }
}
