package pe.fwani.antlr;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
public class QueryParseListener extends pe.fwani.antlr.SqliteBaseListener {
    @Override
    public void enterParse(pe.fwani.antlr.SqliteParser.ParseContext ctx) {
        log.info(ctx.getText());
    }
}
