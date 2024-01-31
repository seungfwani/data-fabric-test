package pe.fwani.antlr;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@Getter
public class MyDataFabricSqlVisitor extends DataFabricSqlBaseVisitor<Void> {
    private final List<String> models = new ArrayList<>();
    private JSONObject jsonTree;
    private final QueryTreeJsonSerializer serializer = new QueryTreeJsonSerializer(DataFabricSqlParser.class);

    @Override
    public Void visitErrorNode(ErrorNode node) {
        throw new SqlParseException("ERROR: error node : " + node);
    }

    @Override
    public Void visitParse(DataFabricSqlParser.ParseContext ctx) {
        jsonTree = serializer.serialize(ctx);
        return super.visitParse(ctx);
    }

    @Override
    public Void visitSelect_core(DataFabricSqlParser.Select_coreContext ctx) {
        // visit 순서 제어
        if (ctx.from_() != null) {
            visitFrom_(ctx.from_());
        }
        if (ctx.where_() != null) {
            visitWhere_(ctx.where_());
        }
        if (ctx.group_by_() != null) {
            visitGroup_by_(ctx.group_by_());
        }
        if (ctx.select_() != null) {
            visitSelect_(ctx.select_());
        }
        return null;
    }

    @Override
    public Void visitFrom_(DataFabricSqlParser.From_Context ctx) {
        if (ctx.table_or_subquery() != null) {
            return visitTable_or_subquery(ctx.table_or_subquery());
        } else { // join clause
            return visitJoin_clause(ctx.join_clause());
        }

    }

    @Override
    public Void visitJoin_clause(DataFabricSqlParser.Join_clauseContext ctx) {
        for (var tos : ctx.table_or_subquery()) {
            visitTable_or_subquery(tos);
        }
        return null;
    }

    @Override
    public Void visitTable_or_subquery(DataFabricSqlParser.Table_or_subqueryContext ctx) {
        if (ctx.model_name() != null) {  // 심플 모델명
            String owner;
            if (ctx.owner_name() == null) {
                owner = "public";
            } else {
                owner = ctx.owner_name().getText();
            }
            var modelName = ctx.model_name().getText();
            log.info("owner : " + owner + "modelName : " + modelName);
            // TODO modelName 을 이용해 모델의 실제 데이터 소스 가져 오기
            models.add(owner + "." + modelName);
            return null;
        } else if (ctx.select_stmt() != null) {  // 서브 쿼리
            return visitSelect_stmt(ctx.select_stmt());
        } else { // ( join )
            return visitJoin_clause(ctx.join_clause());
        }
    }
}
