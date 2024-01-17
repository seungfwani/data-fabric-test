package pe.fwani;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@SpringBootTest
class ParserServiceTest {

    @Autowired
    private ParserService service;

    @Test
    void parse() {
        var result = service.parse("select `a`, (b) from a");
        System.out.println(result);
        Assertions.assertEquals(List.of("a"), ((Map<?, ?>) result.get("data")).get("tables"));
    }

    @Test
    void parse_2() {
        var result = service.parse("select 1;;;; select `as` from cvb;" +
                " SELECT a,max(f) as ddf FROM (select c, d, a, b from (select * from test.Orders))");
        System.out.println(result);
        Assertions.assertEquals(List.of("cvb", "test.Orders"), ((Map<?, ?>) result.get("data")).get("tables"));
    }
}