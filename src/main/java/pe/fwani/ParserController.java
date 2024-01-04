package pe.fwani;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1z
 */
@Controller
public class ParserController {
    @PostMapping(value = "/parse",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object parse(@RequestBody QueryDTO queryDTO) throws JSQLParserException {

        var stmt = CCJSqlParserUtil.parse(queryDTO.getQuery());
        System.out.println(stmt.toString());
        return Map.of("data", stmt.toString());
    }
}
