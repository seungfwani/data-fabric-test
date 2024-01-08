package pe.fwani.antlr;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;

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
    public void enterEveryRule(ParserRuleContext ctx) {
        log.info("rule entered: " + ctx.getText());
    }

}
