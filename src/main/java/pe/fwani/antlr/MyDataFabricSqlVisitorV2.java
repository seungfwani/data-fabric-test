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

import java.util.ArrayList;
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
public class MyDataFabricSqlVisitorV2 extends DataFabricSqlBaseVisitor<DataFabricSqlParser.ParseContext> {
    private List<String> models = new ArrayList<>();

    public MyDataFabricSqlVisitorV2(SparkSession sparkSession) {
        super();
    }

    public MyDataFabricSqlVisitorV2() {
//        this(SparkSession.builder().getOrCreate());
        this(null);
        log.info("aa");
    }


    @Override
    public DataFabricSqlParser.ParseContext visitSelect_stmt(DataFabricSqlParser.Select_stmtContext ctx) {
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
    public DataFabricSqlParser.ParseContext visitSelect_core(DataFabricSqlParser.Select_coreContext ctx) {
        // visit 순서 제어
        if (ctx.from_() != null){
            visitFrom_(ctx.from_());
        }
        if (ctx.where_() != null){
            visitWhere_(ctx.where_());
        }
        if (ctx.group_by_() != null){
            visitGroup_by_(ctx.group_by_());
        }
        if (ctx.select_() != null){
            visitSelect_(ctx.select_());
        }
        return null;
    }

    @Override
    public DataFabricSqlParser.ParseContext visitFrom_(DataFabricSqlParser.From_Context ctx) {
        if (ctx.table_or_subquery() != null) {
            return visitTable_or_subquery(ctx.table_or_subquery());
        } else { // join clause
            return visitJoin_clause(ctx.join_clause());
        }
        
    }

    @Override
    public DataFabricSqlParser.ParseContext visitJoin_clause(DataFabricSqlParser.Join_clauseContext ctx) {
        for (var tos : ctx.table_or_subquery()) {
            visitTable_or_subquery(tos);
        }
        return null;
    }

    @Override
    public DataFabricSqlParser.ParseContext visitTable_or_subquery(DataFabricSqlParser.Table_or_subqueryContext ctx) {
        if (ctx.model_name() != null) {  // 심플 모델명
            var modelName = ctx.model_name().getText();
            log.info("modelName : " + modelName);
            models.add(modelName);
            // TODO modelName 을 이용해 모델의 실제 데이터 소스 가져 오기
            return null;
        } else if (ctx.select_stmt() != null) {  // 서브 쿼리
            return visitSelect_stmt(ctx.select_stmt());
        } else { // ( join )
            return visitJoin_clause(ctx.join_clause());
        }
    }

    @Override
    public DataFabricSqlParser.ParseContext visitModel_name(DataFabricSqlParser.Model_nameContext ctx) {
        return super.visitModel_name(ctx);
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
