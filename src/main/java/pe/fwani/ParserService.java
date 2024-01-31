package pe.fwani;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import pe.fwani.antlr.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<Map<String, Object>> toMap(ParseTree tree, Vocabulary vocabulary) {
        List<Map<String, Object>> output = new ArrayList<>();
        for (int i = 0; i < tree.getChildCount(); i++) {
            if (tree instanceof SqliteV2Parser.Sql_stmtContext) {
                Map<String, Object> map = new LinkedHashMap<>();
                traverse(tree, map, vocabulary);
                output.add(map);
            } else {
                output.addAll(toMap(tree.getChild(i), vocabulary));
            }
        }
        return output;
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
            ((SqliteV2Parser.Table_or_subqueryContext) tree).table_or_subquery(0);
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
        var jsonObject = new JSONObject(queryMap);
        var serializer = new QueryTreeJsonSerializer();
        var tree = serializer.deserialize(jsonObject);
        return serializer.convertTreeToString(tree);
    }

    public Map<String, Object> parse(String query) {
        var lexer = new DataFabricSqlLexer(CharStreams.fromString(query));

        var tokens = new CommonTokenStream(lexer);
        var parser = new DataFabricSqlParser(tokens);
        var visitor = new MyDataFabricSqlVisitor();
        try {
            var parseTree = parser.parse();
            visitor.visit(parseTree);
            return Map.of("data", Map.of(
                    "tree", visitor.getJsonTree().toMap(),
                    "tables", visitor.getModels()
            ));

        } catch (SqlParseException e) {
            return Map.of("message", e.getMessage());
        }
    }
}
