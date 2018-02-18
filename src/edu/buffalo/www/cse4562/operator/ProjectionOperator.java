package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * This {@link Operator} does the projection of selected columns.
 *
 * @author varunjai
 *
 */
public class ProjectionOperator implements Operator {

  private final Collection<SelectItem> selectItems;
  /**
   * flag to indicate that columns have been requested, hence no projection
   */
  private boolean allColFlag = false;

  public ProjectionOperator(Collection<SelectItem> selectItems) {
    Validate.notEmpty(selectItems);
    this.selectItems = selectItems;

    // check for '*'
    if (selectItems.stream().anyMatch(item -> item instanceof AllColumns)) {
      allColFlag = true;
    }
  }

  public Collection<SelectItem> getSelectItems() {
    return selectItems;
  }

  @Override
  public Collection<Tuple> process(Collection<Tuple> tuples) {

    final List<Tuple> projectOutput = new ArrayList<>();
    // empty check
    if (CollectionUtils.areTuplesEmpty(tuples)) {
      return projectOutput;
    }

    // if user requested all columns for all tables
    if (allColFlag) {
      return tuples;
    }

    for (final Tuple tuple : tuples) {

      if (tuple.isEmpty()) {
        continue;
      }

      final List<ColumnCell> columnCells = new ArrayList<>();
      for (final ColumnCell columnCell : tuple.getColumnCells()) {
        for (final SelectItem selectItem : selectItems) {
          if (SchemaManager.getColumnIdByTableId(columnCell.getTableId(),
              selectItem.toString()) == columnCell.getColumnId()) {
            columnCells.add(columnCell);
          } // if
        } // for
      } // for

      projectOutput.add(new Tuple(columnCells));
    } // for

    return projectOutput;
  }

  public boolean isAllColFlag() {
    return allColFlag;
  }

  public void setAllColFlag(boolean allColFlag) {
    this.allColFlag = allColFlag;
  }

}
