package ptonppqm;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.OperationOutcome;
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

import javax.xml.soap.SOAPException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openehealth.ipf.commons.ihe.fhir.chppqm.ChPpqmConsentCreator.create201Consent;
import static org.openehealth.ipf.commons.ihe.fhir.chppqm.ChPpqmConsentCreator.createUuid;

/**
 * @author Dmytro Rud
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(PpqmApp.class)
@ActiveProfiles("test")
@Slf4j
public class Ppq3Test {

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

    private Exchange send(Object request, String httpMethod) throws Exception {
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
        Exchange exchange = send(consent, "POST");
        MethodOutcome methodOutcome = exchange.getMessage().getMandatoryBody(MethodOutcome.class);
        assertTrue(methodOutcome.getCreated());
    }

    @Test
    public void testPpq3PutUnknown() throws Exception {
        Consent consent = create201Consent(createUuid(), TestConstants.EPR_SPID);
        Exchange exchange = send(consent, "PUT");
        MethodOutcome methodOutcome = exchange.getMessage().getMandatoryBody(MethodOutcome.class);
        assertTrue(methodOutcome.getCreated());
    }

    @Test
    public void testPpq3PutKnown() throws Exception {
        Consent consent = create201Consent(TestConstants.KNOWN_POLICY_SET_ID, TestConstants.EPR_SPID);
        Exchange exchange = send(consent, "PUT");
        MethodOutcome methodOutcome = exchange.getMessage().getMandatoryBody(MethodOutcome.class);
        assertTrue((methodOutcome.getCreated() == null) || !methodOutcome.getCreated());
    }

    @Test
    public void testPpq3Delete() throws Exception {
        String policySetId = createUuid();
        Exchange exchange = send(policySetId, "DELETE");
        MethodOutcome methodOutcome = exchange.getMessage().getMandatoryBody(MethodOutcome.class);
        OperationOutcome operationOutcome = (OperationOutcome) methodOutcome.getOperationOutcome();
        assertEquals(1, operationOutcome.getIssue().size());
        assertTrue(operationOutcome.getIssue().get(0).getDiagnostics().startsWith("<ns1:Fault"));
    }

}
