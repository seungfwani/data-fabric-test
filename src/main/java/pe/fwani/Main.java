package pe.fwani;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
public class Main {
//    public static void main(String[] args) throws JSQLParserException {
//        System.out.println("Hello world!");
//
//        String sqlStr = "select 1,2 from dual a where a=b";
//
//        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);
//
//        for (var selectItem : select.getSelectItems()) {
//            System.out.println(selectItem.getExpression());
//        }
//
//        Table table = (Table) select.getFromItem();
//        System.out.println(table.getName());
//
//        EqualsTo equalsTo = (EqualsTo) select.getWhere();
//        Column a = (Column) equalsTo.getLeftExpression();
//        Column b = (Column) equalsTo.getRightExpression();
//        System.out.println(a.getColumnName());
//        System.out.println(b.getColumnName());
//        System.out.println((select.getLimit()));
//    }
}