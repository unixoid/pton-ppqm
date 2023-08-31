package ptonppqm;

import org.openehealth.ipf.commons.audit.AuditContext;
import org.openehealth.ipf.commons.audit.codes.EventOutcomeIndicator;
import org.openehealth.ipf.commons.audit.event.ApplicationActivityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.WebApplicationContext;

import static org.openehealth.ipf.commons.audit.utils.AuditUtils.*;

/**
 * @author Dmytro Rud
 */
@SpringBootApplication
@Import({PpqmConfiguration.class})
@EnableConfigurationProperties(PpqmProperties.class)
public class PpqmApp extends SpringBootServletInitializer {

    private static final String APP_NAME = "Pton PPQm App";

    private final AuditContext auditContext;

    @Autowired
    public PpqmApp(AuditContext auditContext) {
        this.auditContext = auditContext;
    }

    @Override
    protected WebApplicationContext run(SpringApplication application) {
        try {
            auditContext.audit(new ApplicationActivityBuilder.ApplicationStart(EventOutcomeIndicator.Success)
                    .addApplicationStarterParticipant(getUserName())
                    .setApplicationParticipant(APP_NAME, getProcessId(), null, getLocalIPAddress())
                    .setAuditSource(auditContext)
                    .getMessage());

            return super.run(application);
        } finally {
            auditContext.audit(new ApplicationActivityBuilder.ApplicationStop(EventOutcomeIndicator.Success)
                    .addApplicationStarterParticipant(getUserName())
                    .setApplicationParticipant(APP_NAME, getProcessId(), null, getLocalIPAddress())
                    .setAuditSource(auditContext)
                    .getMessage());
        }
    }

    public static void main(String[] args) {
        //ConfigurationService.register(XMLObjectProviderRegistry.class, new XMLObjectProviderRegistry());
        SpringApplication.run(PpqmApp.class, args);
    }

}
