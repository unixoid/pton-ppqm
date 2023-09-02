package ptonppqm;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.support.DefaultExchange;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;
import org.openehealth.ipf.commons.ihe.fhir.chppqm.ChPpqmUtils;
import org.openehealth.ipf.platform.camel.core.util.Exchanges;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openehealth.ipf.commons.ihe.fhir.chppqm.ChPpqmConsentCreator.*;

/**
 * @author Dmytro Rud
 */
public class Ppq4Test extends PpqmTestBase {

    private Exchange send(Bundle request) throws Exception {
        Exchange exchange = new DefaultExchange(camelContext, ExchangePattern.InOut);
        exchange.getMessage().setBody(request);
        exchange = producerTemplate.send("ch-ppq4://localhost:" + serverPort + "/fhir", exchange);
        Exception exception = Exchanges.extractException(exchange);
        if (exception != null) {
            throw exception;
        }
        return exchange;
    }

    @Test
    public void testPost() throws Exception {
        List<Consent> consents = List.of(
                create201Consent(createUuid(), TestConstants.EPR_SPID),
                create303Consent(createUuid(), TestConstants.EPR_SPID, "rep123", null, null));
        Bundle requestBundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        for (Consent consent : consents) {
            requestBundle
                    .addEntry(new Bundle.BundleEntryComponent()
                            .setRequest(new Bundle.BundleEntryRequestComponent()
                                    .setUrl("Consent")
                                    .setMethod(Bundle.HTTPVerb.POST))
                            .setResource(consent));
        }
        requestBundle.getMeta().addProfile(ChPpqmUtils.Profiles.REQUEST_BUNDLE);

        Exchange exchange = send(requestBundle);
        Bundle responseBundle = exchange.getMessage().getMandatoryBody(Bundle.class);

        assertEquals(Bundle.BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
        assertEquals(requestBundle.getEntry().size(), responseBundle.getEntry().size());
        for (Bundle.BundleEntryComponent entry : responseBundle.getEntry()) {
            assertTrue(entry.getResponse().getStatus().startsWith("201"));
        }
    }

    @Test
    public void testPutAllUnknown() throws Exception {
        List<Consent> consents = List.of(
                create201Consent(createUuid(), TestConstants.EPR_SPID),
                create303Consent(createUuid(), TestConstants.EPR_SPID, "rep123", null, null));
        Bundle requestBundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        for (Consent consent : consents) {
            requestBundle
                    .addEntry(new Bundle.BundleEntryComponent()
                            .setRequest(new Bundle.BundleEntryRequestComponent()
                                    .setUrl("Consent?identifier=" + ChPpqmUtils.extractConsentId(consent, ChPpqmUtils.ConsentIdTypes.POLICY_SET_ID))
                                    .setMethod(Bundle.HTTPVerb.PUT))
                            .setResource(consent));
        }
        requestBundle.getMeta().addProfile(ChPpqmUtils.Profiles.REQUEST_BUNDLE);

        Exchange exchange = send(requestBundle);
        Bundle responseBundle = exchange.getMessage().getMandatoryBody(Bundle.class);

        assertEquals(Bundle.BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
        assertEquals(requestBundle.getEntry().size(), responseBundle.getEntry().size());
        for (Bundle.BundleEntryComponent entry : responseBundle.getEntry()) {
            assertTrue(entry.getResponse().getStatus().startsWith("201"));
        }
    }

    @Test
    public void testPutAllKnown() throws Exception {
        List<Consent> consents = List.of(
                create201Consent(TestConstants.KNOWN_POLICY_SET_ID, TestConstants.EPR_SPID));
        Bundle requestBundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        for (Consent consent : consents) {
            requestBundle
                    .addEntry(new Bundle.BundleEntryComponent()
                            .setRequest(new Bundle.BundleEntryRequestComponent()
                                    .setUrl("Consent?identifier=" + ChPpqmUtils.extractConsentId(consent, ChPpqmUtils.ConsentIdTypes.POLICY_SET_ID))
                                    .setMethod(Bundle.HTTPVerb.PUT))
                            .setResource(consent));
        }
        requestBundle.getMeta().addProfile(ChPpqmUtils.Profiles.REQUEST_BUNDLE);

        Exchange exchange = send(requestBundle);
        Bundle responseBundle = exchange.getMessage().getMandatoryBody(Bundle.class);

        assertEquals(Bundle.BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
        assertEquals(requestBundle.getEntry().size(), responseBundle.getEntry().size());
        for (Bundle.BundleEntryComponent entry : responseBundle.getEntry()) {
            assertTrue(entry.getResponse().getStatus().startsWith("200"));
        }
    }

    @Test
    public void testPutMixedKnownAndUnknown() throws Exception {
        List<Consent> consents = List.of(
                create201Consent(TestConstants.KNOWN_POLICY_SET_ID, TestConstants.EPR_SPID),
                create303Consent(createUuid(), TestConstants.EPR_SPID, "rep123", null, null));
        Bundle requestBundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        for (Consent consent : consents) {
            requestBundle
                    .addEntry(new Bundle.BundleEntryComponent()
                            .setRequest(new Bundle.BundleEntryRequestComponent()
                                    .setUrl("Consent?identifier=" + ChPpqmUtils.extractConsentId(consent, ChPpqmUtils.ConsentIdTypes.POLICY_SET_ID))
                                    .setMethod(Bundle.HTTPVerb.PUT))
                            .setResource(consent));
        }
        requestBundle.getMeta().addProfile(ChPpqmUtils.Profiles.REQUEST_BUNDLE);

        boolean failed = false;
        try {
            Exchange exchange = send(requestBundle);
        } catch (Exception e) {
            assertTrue(e instanceof InvalidRequestException);
            assertTrue(e.getMessage().contains("Cannot create PPQ-1 request, because out of 2 policy sets being fed with HTTP method PUT, 1 are already present in the Policy Repository, and 1 are not"));
            failed = true;
        }
        assertTrue(failed);
    }

    @Test
    public void testDelete() throws Exception {
        Bundle requestBundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        for (int i = 0; i < 3; ++i) {
            requestBundle
                    .addEntry(new Bundle.BundleEntryComponent()
                            .setRequest(new Bundle.BundleEntryRequestComponent()
                                    .setUrl("Consent?identifier=" + createUuid())
                                    .setMethod(Bundle.HTTPVerb.DELETE)));
        }
        requestBundle.getMeta().addProfile(ChPpqmUtils.Profiles.REQUEST_BUNDLE);

        boolean failed = false;
        try {
            Exchange exchange = send(requestBundle);
        } catch (BaseServerResponseException e) {
            OperationOutcome operationOutcome = (OperationOutcome) e.getOperationOutcome();
            assertEquals(1, operationOutcome.getIssue().size());
            assertTrue(operationOutcome.getIssue().get(0).getDiagnostics().startsWith("<ns1:Fault"));
            failed = true;
        }
        assertTrue(failed);
    }


}
