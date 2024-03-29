package pe.fwani;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import pe.fwani.antlr.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
class QueryTreeJsonSerializerTest {
    private String sql = "select * from a";

    @Test
    void serialize() {
        var lexer = new SqliteV2Lexer(CharStreams.fromString(sql));

        var tokens = new CommonTokenStream(lexer);
        var parser = new SqliteV2Parser(tokens);
        try {
            var parseTree = parser.parse();
            var result = new QueryTreeJsonSerializer().serialize(parseTree);
            System.out.println(parseTree.toStringTree());
            System.out.println(parseTree.getText());
            System.out.println(result);
        } catch (SqlParseException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void deserialize() {
        var input = List.of(
                Map.of(
                        "sql_stmt_list", List.of(
                                Map.of(
                                        "sql_stmt", List.of(
                                                Map.of(
                                                        "select_stmt", List.of(
                                                                Map.of(
                                                                        "select_core", List.of(
                                                                                Map.of(
                                                                                        "select_", List.of(
                                                                                                Map.of("text", "select", "type", 75),
                                                                                                Map.of("result_column", List.of(Map.of("text", "*", "type", 101)))

                                                                                        ),
                                                                                        "from_", List.of(
                                                                                                Map.of("text", "from", "type", 30),
                                                                                                Map.of("table_or_subquery", List.of(
                                                                                                        Map.of("table_name", List.of(
                                                                                                                Map.of("any_name", List.of(Map.of("text", "a_table", "type", 87)))
                                                                                                        ))
                                                                                                ))

                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                Map.of("text", "<EOF>", "type", -1)
        );
        var inputJson = new JSONObject();
        inputJson.put("parse", input);
        var serializer = new QueryTreeJsonSerializer();
        var result = serializer.deserialize(inputJson);
        System.out.println(result.getText());
        System.out.println(result.toStringTree());
        System.out.println(serializer.serialize(result));
    }

    @Test
    void convertTreeToString() {
//        var lexer = new SqliteV2Lexer(CharStreams.fromString(sql));
        var lexer = new SqliteV2Lexer(CharStreams.fromString("select" +
                " a,b,c as fas, 231, test(adsf) from ab, (select * from aaab) gsdf" +
                " where adf >= 12324 and (asdb in ('asdf', 'asdf','gggs') or sdfg like 'asdb')"));

        var tokens = new CommonTokenStream(lexer);
        var parser = new SqliteV2Parser(tokens);
        try {
            var parseTree = parser.parse();
            var result = new QueryTreeJsonSerializer().convertTreeToString(parseTree);
            System.out.println(parseTree.toStringTree());
            System.out.println(parseTree.getText());
            System.out.println(result);
        } catch (SqlParseException e) {
            System.out.println(e.getMessage());
        }
    }
}