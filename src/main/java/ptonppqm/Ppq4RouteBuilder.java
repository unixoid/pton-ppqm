package ptonppqm;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.openehealth.ipf.commons.ihe.fhir.chppqm.ChPpqmUtils;
import org.openehealth.ipf.commons.ihe.fhir.chppqm.translation.FhirToXacmlTranslator;
import org.openehealth.ipf.commons.ihe.fhir.chppqm.translation.XacmlToFhirTranslator;
import org.openehealth.ipf.commons.ihe.xacml20.ChPpqMessageCreator;
import org.openehealth.ipf.commons.ihe.xacml20.stub.ehealthswiss.AssertionBasedRequestType;
import org.openehealth.ipf.commons.ihe.xacml20.stub.ehealthswiss.EprPolicyRepositoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dmytro Rud
 */
@Component
@Slf4j
public class Ppq4RouteBuilder extends PpqmFeedRouteBuilder {

    @Getter
    private final String uriSchema = "ch-ppq4";

    @Autowired
    public Ppq4RouteBuilder(
            PpqmProperties ppqmProperties,
            FhirToXacmlTranslator fhirToXacmlTranslator,
            ChPpqMessageCreator ppqMessageCreator)
    {
        super(ppqmProperties, fhirToXacmlTranslator, ppqMessageCreator);
    }

    @Override
    protected String extractHttpMethod(Exchange exchange) throws Exception {
        Bundle bundle = exchange.getMessage().getMandatoryBody(Bundle.class);
        return bundle.getEntry().get(0).getRequest().getMethod().toCode();
    }

    @Override
    protected List<String> extractPolicySetIds(Object ppqmRequest) {
        Bundle bundle = (Bundle) ppqmRequest;
        return bundle.getEntry().stream()
                .map(entry -> {
                    Consent consent = (Consent) entry.getResource();
                    return ChPpqmUtils.extractConsentId(consent, ChPpqmUtils.ConsentIdTypes.POLICY_SET_ID);
                })
                .collect(Collectors.toList());
    }

    @Override
    protected AssertionBasedRequestType createPpqRequest(Object ppqmRequest, String method) {
        return fhirToXacmlTranslator.translatePpq4To1Request((Bundle) ppqmRequest);
    }

    @Override
    protected Object createPpqmResponse(Object ppqmRequest, AssertionBasedRequestType xacmlRequest, EprPolicyRepositoryResponse xacmlResponse) {
        return XacmlToFhirTranslator.translatePpq1To4Response((Bundle) ppqmRequest, xacmlRequest, xacmlResponse);
    }

}