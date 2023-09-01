package ptonppqm;

import org.openehealth.ipf.commons.ihe.xacml20.Xacml20Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

/**
 * @author Dmytro Rud
 */
@SpringBootApplication
@Import({PpqmConfiguration.class})
@EnableConfigurationProperties(PpqmProperties.class)
public class PpqmApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        Xacml20Utils.initializeHerasaf();
        SpringApplication.run(PpqmApp.class, args);
    }

}
