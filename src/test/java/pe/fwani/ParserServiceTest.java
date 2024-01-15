package pe.fwani;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pe.fwani.antlr.*;
import pe.fwani.antlr.SqliteLexer;
import pe.fwani.antlr.SqliteParser;

import java.util.HashMap;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@SpringBootTest
class ParserServiceTest {

    @Autowired
    private ParserService service;

    @Test
    void parse() {
        var result = service.parse("select `a`, (b) from a");
        System.out.println(result);
    }

    @Test
    void test() {
        var query = "select `a`, (b) from a";
        var lexer = new SqliteLexer(CharStreams.fromString(query));

        var tokens = new CommonTokenStream(lexer);
        var parser = new SqliteParser(tokens);
        var listener = new QueryParseListener();
        parser.removeErrorListeners();
        parser.addErrorListener(new QueryParseErrorListener());
        try {
            var parseTree = parser.parse();
            var walker = new ParseTreeWalker();
            walker.walk(listener, parseTree);
            service.getColumns(parseTree, new HashMap<>(), null, null);

        } catch (SqlParseException e) {
            System.out.println(e.getMessage());
        }
    }
}