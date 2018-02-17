package edu.buffalo.www.cse4562.operator;

import java.util.HashMap;
import java.util.List;

import edu.buffalo.www.cse4562.TableData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Union;

public class SelectionOperator extends Operator{

	public static void evaluateSelect(Select statement, HashMap<String,TableData> tables){ 
		SelectBody selectBody = statement.getSelectBody();
		if(selectBody instanceof PlainSelect) {
			PlainSelect plainSelect = (PlainSelect)selectBody;
			List<SelectItem> selectItems = plainSelect.getSelectItems();
			FromItem fromItem = plainSelect.getFromItem();
			Expression where = plainSelect.getWhere();
		}
		else if(selectBody instanceof Union) {
			//add union operations here
		}
		
	}
}
