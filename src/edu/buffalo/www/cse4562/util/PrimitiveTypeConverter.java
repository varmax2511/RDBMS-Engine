package edu.buffalo.www.cse4562.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.PrimitiveType;
import net.sf.jsqlparser.statement.create.table.ColDataType;

public class PrimitiveTypeConverter {

  public static Map<String, PrimitiveType> dataType2Primitive = new HashMap<>();

  static {
    dataType2Primitive.put("int", PrimitiveType.LONG);
    dataType2Primitive.put("char", PrimitiveType.STRING);
    dataType2Primitive.put("varchar", PrimitiveType.STRING);
    dataType2Primitive.put("string", PrimitiveType.STRING);
    dataType2Primitive.put("decimal", PrimitiveType.DOUBLE);
    dataType2Primitive.put("date", PrimitiveType.DATE);
    //dataType2Primitive.put("boolean", PrimitiveType.BOOL);
  }

  public static PrimitiveValue getPrimitiveValueByColDataType(
      ColDataType colDataType, String value) {

    // unknown value
    if (!dataType2Primitive.containsKey(colDataType.getDataType())) {
      throw new IllegalArgumentException("Unknown data type, not supported: "+colDataType.getDataType());
    }

    final PrimitiveType primitiveType = dataType2Primitive
        .get(colDataType.getDataType());

    // long
    if (primitiveType.equals(PrimitiveType.LONG)) {
      return new LongValue(value);
    }

    // Date
    if (primitiveType.equals(PrimitiveType.DATE)) {
      return new DateValue(value);
    }

    return new StringValue(value);
  }

}
