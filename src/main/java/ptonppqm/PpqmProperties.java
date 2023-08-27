package ptonppqm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Dmytro Rud
 */
@ConfigurationProperties("pton-ppqm")
@Data
public class PpqmProperties {

    private String homeCommunityId;

    private String ppq1Endpoint;
    private String ppq2Endpoint;

}
