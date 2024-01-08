package pe.fwani;

import net.sf.jsqlparser.JSQLParserException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pe.fwani.antlr.*;
import pe.fwani.antlr.SqliteLexer;
import pe.fwani.antlr.SqliteParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1z
 */
@RestController
public class ParserController {

    public static Map<String, Object> toMap(ParseTree tree) {
        Map<String, Object> map = new LinkedHashMap<>();
        traverse(tree, map);
        return map;
    }

    public static void traverse(ParseTree tree, Map<String, Object> map) {

        if (tree instanceof TerminalNodeImpl) {
            Token token = ((TerminalNodeImpl) tree).getSymbol();
            map.put("type", token.getType());
            map.put("text", token.getText());
        } else {
            List<Map<String, Object>> children = new ArrayList<>();
            String name = tree.getClass().getSimpleName().replaceAll("Context$", "");
            map.put(Character.toLowerCase(name.charAt(0)) + name.substring(1), children);

            for (int i = 0; i < tree.getChildCount(); i++) {
                Map<String, Object> nested = new LinkedHashMap<>();
                children.add(nested);
                traverse(tree.getChild(i), nested);
            }
        }
    }

    @PostMapping(value = "/parse",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object parse(@RequestBody QueryDTO queryDTO) throws JSQLParserException {
        var lexer = new SqliteLexer(CharStreams.fromString(queryDTO.getQuery()));

        var tokens = new CommonTokenStream(lexer);
        var parser = new SqliteParser(tokens);
        var listener = new QueryParseListener();
        parser.removeErrorListeners();
        parser.addErrorListener(new QueryParseErrorListener());
        try {
            var parseTree = parser.parse();
            var walker = new ParseTreeWalker();
            walker.walk(listener, parseTree);
            return Map.of("data", toMap(parseTree));
        } catch (SqlParseException e) {
            return Map.of("message", e.getMessage());
        }

//        var stmt = CCJSqlParserUtil.parse(queryDTO.getQuery());
//        System.out.println(stmt.toString());
//        return Map.of("data", stmt.toString());
    }
}
