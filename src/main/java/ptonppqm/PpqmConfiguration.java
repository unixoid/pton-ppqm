package ptonppqm;

import org.openehealth.ipf.commons.ihe.fhir.chppqm.translation.FhirToXacmlTranslator;
import org.openehealth.ipf.commons.ihe.xacml20.ChPpqMessageCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dmytro Rud
 */
@Configuration
public class PpqmConfiguration {

    @Value("homeCommunityId")
    private String homeCommunityId;

    @Bean
    public ChPpqMessageCreator ppqMessageCreator() {
        return new ChPpqMessageCreator(homeCommunityId);
    }

    @Bean
    public FhirToXacmlTranslator fhirToXacmlTranslator(ChPpqMessageCreator ppqMessageCreator) {
        return new FhirToXacmlTranslator(ppqMessageCreator);
    }

}
