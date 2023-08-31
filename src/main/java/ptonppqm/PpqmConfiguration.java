package ptonppqm;

import org.openehealth.ipf.commons.ihe.fhir.chppqm.translation.FhirToXacmlTranslator;
import org.openehealth.ipf.commons.ihe.xacml20.ChPpqMessageCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dmytro Rud
 */
@Configuration
public class PpqmConfiguration {

    @Bean
    public ChPpqMessageCreator ppqMessageCreator(PpqmProperties properties) {
        String homeCommunityId = properties.getHomeCommunityId();
        if (!homeCommunityId.startsWith("urn:oid:")) {
            homeCommunityId = "urn:oid:" + homeCommunityId;
        }
        return new ChPpqMessageCreator(homeCommunityId);
    }

    @Bean
    public FhirToXacmlTranslator fhirToXacmlTranslator(ChPpqMessageCreator ppqMessageCreator) {
        return new FhirToXacmlTranslator(ppqMessageCreator);
    }

}
