package ptonppqm;

import org.apache.neethi.Policy;
import org.apache.velocity.VelocityContext;
import org.herasaf.xacml.core.policy.impl.PolicySetType;
import org.openehealth.ipf.commons.ihe.fhir.chppqm.translation.FhirToXacmlTranslator;
import org.openehealth.ipf.commons.ihe.xacml20.ChPpqMessageCreator;
import org.openehealth.ipf.commons.ihe.xacml20.ChPpqPolicySetCreator;
import org.openehealth.ipf.commons.ihe.xacml20.model.PpqConstants;
import org.openehealth.ipf.commons.ihe.xacml20.stub.UnknownPolicySetIdFaultMessage;
import org.openehealth.ipf.commons.ihe.xacml20.stub.ehealthswiss.*;
import org.openehealth.ipf.commons.ihe.xacml20.stub.xacml20.saml.assertion.XACMLPolicyStatementType;
import org.openehealth.ipf.commons.ihe.xacml20.stub.xacml20.saml.protocol.XACMLPolicyQueryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.openehealth.ipf.platform.camel.ihe.xacml20.Xacml20CamelValidators.*;

/**
 * @author Dmytro Rud
 */
@Component
public class TestRouteBuilder extends PpqmRouteBuilder {

    @Autowired
    public TestRouteBuilder(
            PpqmProperties ppqmProperties,
            FhirToXacmlTranslator fhirToXacmlTranslator,
            ChPpqMessageCreator ppqMessageCreator)
    {
        super(ppqmProperties, fhirToXacmlTranslator, ppqMessageCreator);
    }

    @Override
    public void configure() throws Exception {

        from("ch-ppq1:ppq1Endpoint")
                .process(chPpq1RequestValidator())
                .process(exchange -> {
                    log.info("Received PPQ-1 request");
                    AssertionBasedRequestType ppq1Request = exchange.getMessage().getMandatoryBody(AssertionBasedRequestType.class);
                    if (ppq1Request instanceof DeletePolicyRequest) {
                        throw new UnknownPolicySetIdFaultMessage("tralala");
                    }
                    XACMLPolicyStatementType statement = (XACMLPolicyStatementType) ppq1Request.getAssertion().getStatementOrAuthnStatementOrAuthzDecisionStatement().get(0);
                    PolicySetType policySet = (PolicySetType) statement.getPolicyOrPolicySet().get(0);
                    EprPolicyRepositoryResponse ppq1Response = new EprPolicyRepositoryResponse();
                    ppq1Response.setStatus(policySet.getPolicySetId().toString().equals(TestConstants.FAILURE_POLICY_SET_ID)
                            ? PpqConstants.StatusCode.FAILURE
                            : PpqConstants.StatusCode.SUCCESS);
                    exchange.getMessage().setBody(ppq1Response);
                })
                .process(chPpq1ResponseValidator())
        ;

        from("ch-ppq2:ppq2Endpoint")
                .process(chPpq2RequestValidator())
                .process(exchange -> {
                    log.info("Received PPQ-2 request");
                    XACMLPolicyQueryType ppq2Request = exchange.getMessage().getMandatoryBody(XACMLPolicyQueryType.class);
                    int requestedCount = ppq2Request.getRequestOrPolicySetIdReferenceOrPolicyIdReference().size();

                    List<PolicySetType> policySets = new ArrayList<>(requestedCount);

                    policySets.add(ChPpqPolicySetCreator.createPolicySet("201", new VelocityContext(Map.of(
                            "id", TestConstants.KNOWN_POLICY_SET_ID,
                            "eprSpid", "123456789012345678",
                            "representativeId", "representative123"))));

                    for (int i = 1; i < requestedCount; ++i) {
                        policySets.add(ChPpqPolicySetCreator.createPolicySet("303", new VelocityContext(Map.of(
                                "id", UUID.randomUUID().toString(),
                                "eprSpid", "123456789012345678",
                                "representativeId", UUID.randomUUID().toString()))));
                    }

                    exchange.getMessage().setBody(ppqMessageCreator.createPositivePolicyQueryResponse(policySets));
                })
                .process(chPpq2ResponseValidator())
        ;

    }

}
