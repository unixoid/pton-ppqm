package ptonppqm;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.openehealth.ipf.commons.ihe.xacml20.Xacml20Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;

/**
 * @author Dmytro Rud
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(PpqmApp.class)
@ActiveProfiles("test")
@Slf4j
abstract public class PpqmTestBase {

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    protected ProducerTemplate producerTemplate;

    @Value("${server.port}")
    protected Integer serverPort;

    @BeforeAll
    public static void beforeAll() {
        Xacml20Utils.initializeHerasaf();
        Locale.setDefault(Locale.ENGLISH);
    }

}
