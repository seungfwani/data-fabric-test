package pe.fwani;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.springframework.stereotype.Service;
import pe.fwani.antlr.*;
import pe.fwani.antlr.SqliteParser;
import pe.fwani.antlr.SqliteV2Lexer;
import pe.fwani.antlr.SqliteV2Parser;

import java.util.*;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Service
public class ParserService2 {
    private final String NON_RECOGNIZED_COLUMNS_KEY = "_other";

    public Map<String, Object> toMap(ParseTree tree, Vocabulary vocabulary) {
        Map<String, Object> map = new LinkedHashMap<>();
        traverse(tree, map, vocabulary);
        return map;
    }

    public void traverse(ParseTree tree, Map<String, Object> map, Vocabulary vocabulary) {

        if (tree instanceof TerminalNodeImpl) {
            Token token = ((TerminalNodeImpl) tree).getSymbol();
            map.put("type", vocabulary.getSymbolicName(token.getType()));
            map.put("text", token.getText());
        } else {
            List<Map<String, Object>> children = new ArrayList<>();
            String name = tree.getClass().getSimpleName().replaceAll("Context$", "");
            map.put(Character.toLowerCase(name.charAt(0)) + name.substring(1), children);

            for (int i = 0; i < tree.getChildCount(); i++) {
                Map<String, Object> nested = new LinkedHashMap<>();
                children.add(nested);
                traverse(tree.getChild(i), nested, vocabulary);
            }
        }
    }

    public List<String> getColumns(ParseTree tree) {
        return List.of();
    }

    public String getTable(ParseTree tree) {
        if (tree instanceof SqliteParser.Any_nameContext) {

        }
        for (int i = 1; i < tree.getChildCount(); i++) {
            var child = tree.getChild(i);
            if (child instanceof SqliteParser.Table_or_subqueryContext) {

            }
        }
        return "";
    }

    public Map<String, List<String>> traverse(ParseTree tree) {
        Map<String, List<String>> output = new HashMap<>();
        if (tree instanceof SqliteParser.Select_coreContext) {
            var select_ = tree.getChild(0);
            var columns = getColumns(tree);

            var from_ = tree.getChild(1);
            if (from_ == null) {
                output.put(NON_RECOGNIZED_COLUMNS_KEY, columns);
            } else {
                var tableName = getTable(from_);
            }

            for (int i = 0; i < tree.getChildCount(); i++) {
                var child = tree.getChild(i);

            }
        } else {
            var result = traverse(tree);
            for (var key : result.keySet()) {
                if (output.containsKey(key)) {
                    output.get(key).addAll(result.get(key));
                } else {
                    output.put(key, result.get(key));
                }
            }
        }
        return output;
    }

    public Map<String, Object> parse(String query) {
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
            Map<String, Set<String>> tableAndColumns = new HashMap<>();

            return Map.of("data", Map.of(
                    "tree", toMap(parseTree, parser.getVocabulary()),
//                    "tables", tableAndColumns.keySet().stream().filter(x -> !x.equals(NON_RECOGNIZED_COLUMNS_KEY)),
                    "columns", tableAndColumns
            ));

        } catch (SqlParseException e) {
            return Map.of("message", e.getMessage());
        }
    }
}
