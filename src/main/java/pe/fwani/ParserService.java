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
import pe.fwani.antlr.SqliteLexer;
import pe.fwani.antlr.SqliteParser;

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

    public List<String> getUsedTables(ParseTree tree) {
        List<String> tables = new ArrayList<>();
        traverse(tree, tables);
        return tables;
    }

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

    public void traverse(ParseTree tree, List<String> tables) {
        if (tree instanceof TerminalNodeImpl) {
            log.info("Leaf node: " + tree.getText());
        } else {
            String name = tree.getClass().getSimpleName().replaceAll("Context$", "");
            if (name.equalsIgnoreCase("result_column")) {

            } else if (name.equalsIgnoreCase("table_name")) {
                tables.add(tree.getChild(0).getChild(0).getText());
            } else {
                for (int i = 0; i < tree.getChildCount(); i++) {
                    traverse(tree.getChild(i), tables);
                }
            }
        }
    }

    static class AnyName {
        public AnyName(SqliteParser.Any_nameContext tree) {
            tree.getText();
        }
    }

    public static enum DataType {
        COLUMN,
        TABLE,
        OTHER
    }

    public Set<String> getColumns(ParseTree tree, int fromIndex) {
        Set<String> result = new HashSet<>();
        if (tree == null) {
            return result;
        }
        if (tree instanceof SqliteParser.Column_nameContext) {
            result.add(tree.getText());
        } else {
            for (int i = 0; i < Math.min(fromIndex, tree.getChildCount()); i++) {
                result.addAll(getColumns(tree.getChild(i), fromIndex));
            }
        }
        return result;
    }

    public void getColumns(ParseTree tree,
                           Map<String, Set<String>> tableAndColumns,
                           String tableName,
                           Set<String> candidateColumns) {
        // recursive out 조건
        // column_name
        // table_name
        //
        if (candidateColumns != null) {
            if (tableName != null) {
                tableAndColumns.get(tableName).addAll(candidateColumns);
            }
        }
        if (tree instanceof SqliteParser.Column_nameContext) {
            var data = tree.getText();
            if (tableName != null) {
                tableAndColumns.get(tableName).add(data);
            } else {
                if (tableAndColumns.containsKey(NON_RECOGNIZED_COLUMNS_KEY)) {
                    tableAndColumns.get(NON_RECOGNIZED_COLUMNS_KEY).add(data);
                } else {
                    tableAndColumns.put(NON_RECOGNIZED_COLUMNS_KEY, new HashSet<>(List.of(data)));
                }
            }
        } else if (tree instanceof SqliteParser.Select_coreContext) {
            int indexOfFrom = -1;
            for (int i = 0; i < tree.getChildCount(); i++) {
                var child = tree.getChild(i);
                if (child instanceof TerminalNodeImpl && child.getText().equalsIgnoreCase("from")) {
                    indexOfFrom = i;
                }
            }
            var detectedTableOrSubQuery = (SqliteParser.Table_or_subqueryContext) tree.getChild(indexOfFrom + 1);
            if (detectedTableOrSubQuery.table_name() != null) {  // 바로 table 나옴
                var detectedTableName = detectedTableOrSubQuery.getText();
                var columns = getColumns(tree, indexOfFrom);
                if (candidateColumns != null) {
                    columns.addAll(candidateColumns);
                }
                tableAndColumns.put(detectedTableName, columns);
            } else {  // subquery => subquery 밖에서 사용된(main query) select 절은 후보로 넣는다.
                var columns = getColumns(tree, indexOfFrom);
                if (candidateColumns != null) {
                    columns.addAll(candidateColumns);
                }
                getColumns(tree.getChild(indexOfFrom + 1), tableAndColumns, null, columns);
            }

            // where 절 이후 쿼리들 에서도 있을 수 있기 때문에 확인
            if (tree.getChildCount() > indexOfFrom + 2) {
                for (int i = indexOfFrom + 2; i < tree.getChildCount(); i++) {
                    getColumns(tree.getChild(i), tableAndColumns, tableName, candidateColumns);
                }
            }
        } else {
            log.info("나머지 체크 필요: " + tree.getText());
            for (int i = 0; i < tree.getChildCount(); i++) {
                getColumns(tree.getChild(i), tableAndColumns, tableName, candidateColumns);
            }
        }
    }

    public Map<String, Object> parse(String query) {
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
            Map<String, Set<String>> tableAndColumns = new HashMap<>();
            getColumns(parseTree, tableAndColumns, null, null);

            return Map.of("data", Map.of(
                    "tree", toMap(parseTree, parser.getVocabulary()),
                    "tables", tableAndColumns.keySet().stream().filter(x -> !x.equals(NON_RECOGNIZED_COLUMNS_KEY)),
                    "columns", tableAndColumns
            ));

        } catch (SqlParseException e) {
            return Map.of("message", e.getMessage());
        }
    }
}
