package cloud.angst.dbcache.app.config;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.amazonaws.regions.Regions.DEFAULT_REGION;

@Slf4j
@Configuration
public class DynamoDbConfig {
    @Bean
    public AmazonDynamoDBClient amazonDynamoDbClient(@Value("${amazon.dynamodb.endpoint:}") String endpoint) {
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
        if (endpoint != null && !endpoint.isBlank()) {
            logger.warn("dynamodb endpoint set via property amazon.dynamodb.endpoint={}", endpoint);
            builder.setEndpointConfiguration(new EndpointConfiguration(endpoint, DEFAULT_REGION.getName()));
            BasicAWSCredentials emptyCredentials = new BasicAWSCredentials("", "");
            builder.setCredentials(
                    new AWSCredentialsProviderChain(
                            InstanceProfileCredentialsProvider.getInstance(),
                            new AWSStaticCredentialsProvider(emptyCredentials)));
        }
        return (AmazonDynamoDBClient) builder.build();
    }
}
