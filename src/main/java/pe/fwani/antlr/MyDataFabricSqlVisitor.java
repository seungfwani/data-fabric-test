package pe.fwani.antlr;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.json.JSONObject;

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
@Getter
public class MyDataFabricSqlVisitor extends DataFabricSqlBaseVisitor<Dataset<Row>> {
    private SparkSession sparkSession;
    private Dataset<?> dataset;

    private JSONObject sqlTree;
    private Integer sqlStmtCount = 0;
    private Vocabulary vocabulary = SqliteV2Lexer.VOCABULARY;
    private JSONObject now;
    private JSONObject selectCore;

    public MyDataFabricSqlVisitor(SparkSession sparkSession) {
        super();
        this.sparkSession = sparkSession;
    }

    public MyDataFabricSqlVisitor() {
//        this(SparkSession.builder().getOrCreate());
        this(null);
        log.info("aa");
    }

    @Override
    public Dataset<Row> visitParse(DataFabricSqlParser.ParseContext ctx) {
        log.info("Visit main rule(parse) : " + ctx.getText());
        visitSql_stmt(ctx.sql_stmt());
        return null;
    }

    @Override
    public Dataset<Row> visitSql_stmt(DataFabricSqlParser.Sql_stmtContext ctx) {
        log.info("Visit sql_stmt : " + ctx.getText());
        visitSelect_stmt(ctx.select_stmt());
        return null;
    }

    @Override
    public Dataset<Row> visitSelect_stmt(DataFabricSqlParser.Select_stmtContext ctx) {
        var selectCoreList = ctx.select_core();
        if (selectCoreList.size() > 1) { // compound operator 사용
            for (int i = 0; i < selectCoreList.size(); i++) {
                var dataset = visitSelect_core(selectCoreList.get(i));
                // TODO Compound Operator 적용
            }
        } else { // main select 절 하나
            var dataset = visitSelect_core(selectCoreList.get(0));
            // TODO dataset 기록 방법
        }
        return null;
    }

    @Override
    public Dataset<Row> visitSelect_core(DataFabricSqlParser.Select_coreContext ctx) {
        // visit 순서 제어
        visitFrom_(ctx.from_());
        visitWhere_(ctx.where_());
        visitGroup_by_(ctx.group_by_());
        visitSelect_(ctx.select_());
        return null;
    }

    @Override
    public Dataset<Row> visitFrom_(DataFabricSqlParser.From_Context ctx) {
        if (ctx.table_or_subquery() != null) {
            var candidate = ctx.table_or_subquery();
            if (candidate.model_name() != null) {  // 심플 모델명
                var modelName = candidate.model_name().getText();
                log.info("modelName : " + modelName);
                // TODO modelName 을 이용해 모델의 실제 데이터 소스 가져 오기
                return sparkSession.emptyDataFrame();
            } else if (candidate.select_stmt() != null) {  // 서브 쿼리
                return visitSelect_stmt(candidate.select_stmt());
            } else { // ( join )
                return visitJoin_clause(candidate.join_clause());
            }
        } else { // join clause
            return null;
        }
    }

    public void enterParse(DataFabricSqlParser.ParseContext ctx) {
        log.info("Enter main rule(parse) : " + ctx.getText());
    }

    public void exitParse(DataFabricSqlParser.ParseContext ctx) {
        log.info("Exit  main rule(parse) : " + ctx.getText());
    }


    public List<Map<String, Object>> parse(String query) {
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
//            return Map.of("data", Map.of(
//                    "tree", toMap(parseTree, parser.getVocabulary()),
//                    "tables", getTable(parseTree)
//            ));

        } catch (SqlParseException e) {
//            return Map.of("message", e.getMessage());
        }
        return List.of();
    }

    public String stringify(List<Map<String, Object>> tree) {
        return "";
    }
}
