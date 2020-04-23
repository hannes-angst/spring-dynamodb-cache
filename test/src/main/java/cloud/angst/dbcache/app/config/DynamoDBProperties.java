package cloud.angst.dbcache.app.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@ConfigurationProperties("amazon.dynamodb")
public class DynamoDBProperties {
    private String endpoint;
}
