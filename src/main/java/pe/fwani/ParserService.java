package pe.fwani;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ParserService {
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

    public List<String> getTable(ParseTree tree) {
        if (tree instanceof SqliteV2Parser.Table_or_subqueryContext) {
            if (((SqliteV2Parser.Table_or_subqueryContext) tree).table_name() != null) {
                return List.of(tree.getText());
            } else {
                List<String> output = new ArrayList<>();
                for (int i = 0; i < tree.getChildCount(); i++) {
                    output.addAll(getTable(tree.getChild(i)));
                }
                return output;
            }
        } else {
            List<String> output = new ArrayList<>();
            for (int i = 0; i < tree.getChildCount(); i++) {
                output.addAll(getTable(tree.getChild(i)));
            }
            return output;
        }
    }

    public String generate(String queryMap) {
        var om = new ObjectMapper();

        return "";
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
            return Map.of("data", Map.of(
                    "tree", toMap(parseTree, parser.getVocabulary()),
                    "tables", getTable(parseTree)
            ));

        } catch (SqlParseException e) {
            return Map.of("message", e.getMessage());
        }
    }
}
