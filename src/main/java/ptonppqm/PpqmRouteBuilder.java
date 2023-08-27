package ptonppqm;

import org.apache.camel.builder.RouteBuilder;
import org.apache.cxf.binding.soap.SoapFault;
import org.openehealth.ipf.commons.ihe.fhir.chppqm.translation.FhirToXacmlTranslator;
import org.openehealth.ipf.commons.ihe.fhir.chppqm.translation.XacmlToFhirTranslator;
import org.openehealth.ipf.commons.ihe.xacml20.ChPpqMessageCreator;

/**
 * @author Dmytro Rud
 */
abstract public class PpqmRouteBuilder extends RouteBuilder {

    protected final PpqmProperties ppqmProperties;
    protected final FhirToXacmlTranslator fhirToXacmlTranslator;
    protected final ChPpqMessageCreator ppqMessageCreator;

    protected PpqmRouteBuilder(
            PpqmProperties ppqmProperties,
            FhirToXacmlTranslator fhirToXacmlTranslator,
            ChPpqMessageCreator ppqMessageCreator)
    {
        this.ppqmProperties = ppqmProperties;
        this.fhirToXacmlTranslator = fhirToXacmlTranslator;
        this.ppqMessageCreator = ppqMessageCreator;
    }

    protected void configureExceptionHandling() {
        onException(SoapFault.class)
                .handled(true)
                .maximumRedeliveries(0)
                .process(exchange -> {
                    log.info("Received SOAP Fault");
                    SoapFault soapFault = exchange.getException(SoapFault.class);
                    XacmlToFhirTranslator.translateSoapFault(soapFault);
                })
        ;

        onException(Exception.class)
                .handled(true)
                .maximumRedeliveries(0)
        ;
    }

}
