package pe.fwani;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pe.fwani.antlr.*;
import pe.fwani.antlr.SqliteV2Lexer;
import pe.fwani.antlr.SqliteV2Parser;

import java.util.HashMap;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@SpringBootTest
class ParserService2Test {

    @Autowired
    private ParserService2 service;

    @Test
    void parse() {
        var result = service.parse("select `a`, (b) from a");
        System.out.println(result);
    }

    @Test
    void test() {
        var query = "select `a`, (b) from a";
        var lexer = new SqliteV2Lexer(CharStreams.fromString(query));

        var tokens = new CommonTokenStream(lexer);
        var parser = new SqliteV2Parser(tokens);
        var listener = new QueryParseListener();
        parser.removeErrorListeners();
        parser.addErrorListener(new QueryParseErrorListener());
        try {
            var parseTree = parser.parse();
            var walker = new ParseTreeWalker();
            walker.walk(listener, parseTree);
            service.traverse(parseTree);

        } catch (SqlParseException e) {
            System.out.println(e.getMessage());
        }
    }
}