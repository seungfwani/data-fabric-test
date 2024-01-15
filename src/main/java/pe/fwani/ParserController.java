package pe.fwani;

import net.sf.jsqlparser.JSQLParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1z
 */
@RestController
public class ParserController {
    @Autowired
    private ParserService service;

    @PostMapping(value = "/parse",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object parse(@RequestBody QueryDTO queryDTO) throws JSQLParserException {
        return service.parse(queryDTO.getQuery());
//        var stmt = CCJSqlParserUtil.parse(queryDTO.getQuery());
//        System.out.println(stmt.toString());
//        return Map.of("data", stmt.toString());
    }
}
