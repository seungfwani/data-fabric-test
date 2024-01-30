package pe.fwani.antlr;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class MyDataFabricSqlVisitorTest {

    @Test
    void visitParse() {

        var lexer = new DataFabricSqlLexer(CharStreams.fromString("select" +
                " a,b,c as fas, 231, test(adsf) from ab, (select * from aaab, asb) gsdf" +
                " where adf >= 12324 and (asdb in ('asdf', 'asdf','gggs') or sdfg like 'asdb')"));

        var tokens = new CommonTokenStream(lexer);
        var parser = new DataFabricSqlParser(tokens);
//        var visitor = new MyDataFabricSqlVisitor();
        var visitor = new MyDataFabricSqlVisitorV2();
        try {
            var parseTree = parser.parse();
            var result = visitor.visit(parseTree);
            System.out.println(result);
            System.out.println("used model list: " + visitor.getModels());
            var serializer = new QueryTreeJsonSerializer("pe.fwani.antlr.DataFabricSqlParser");
            System.out.println(serializer.serialize(parseTree));
        } catch (SqlParseException e) {
            System.out.println(e.getMessage());
        }
    }
}