package ptonppqm;

import ca.uhn.fhir.rest.api.MethodOutcome;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.hl7.fhir.r4.model.Consent;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.fhir.Constants;
import org.openehealth.ipf.commons.ihe.xacml20.Xacml20Utils;
import org.openehealth.ipf.platform.camel.core.util.Exchanges;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.openehealth.ipf.commons.ihe.fhir.chppqm.ChPpqmConsentCreator.create201Consent;
import static org.openehealth.ipf.commons.ihe.fhir.chppqm.ChPpqmConsentCreator.createUuid;

/**
 * @author Dmytro Rud
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(PpqmApp.class)
@ActiveProfiles("test")
@Slf4j
public class PpqmAppTest {

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

    private Exchange sendPpq3Request(Object request, String httpMethod) throws Exception {
        Exchange exchange = new DefaultExchange(camelContext, ExchangePattern.InOut);
        exchange.getMessage().setBody(request);
        exchange.getMessage().setHeader(Constants.HTTP_METHOD, httpMethod);
        exchange = producerTemplate.send("ch-ppq3://localhost:" + serverPort + "/fhir", exchange);
        Exception exception = Exchanges.extractException(exchange);
        if (exception != null) {
            throw exception;
        }
        return exchange;
    }

    @Test
    public void testPpq3Post() throws Exception {
        Consent consent = create201Consent(createUuid(), TestConstants.EPR_SPID);
        Exchange exchange = sendPpq3Request(consent, "POST");
        MethodOutcome methodOutcome = exchange.getMessage().getMandatoryBody(MethodOutcome.class);
        assertTrue(methodOutcome.getCreated());
    }

    @Test
    public void testPpq3PutUnknown() throws Exception {
        Consent consent = create201Consent(createUuid(), TestConstants.EPR_SPID);
        Exchange exchange = sendPpq3Request(consent, "PUT");
        MethodOutcome methodOutcome = exchange.getMessage().getMandatoryBody(MethodOutcome.class);
        assertTrue(methodOutcome.getCreated());
    }

    @Test
    public void testPpq3PutKnown() throws Exception {
        Consent consent = create201Consent("urn:uuid:" + TestConstants.KNOWN_POLICY_SET_ID, TestConstants.EPR_SPID);
        Exchange exchange = sendPpq3Request(consent, "PUT");
        MethodOutcome methodOutcome = exchange.getMessage().getMandatoryBody(MethodOutcome.class);
        assertTrue((methodOutcome.getCreated() == null) || !methodOutcome.getCreated());
    }

}
