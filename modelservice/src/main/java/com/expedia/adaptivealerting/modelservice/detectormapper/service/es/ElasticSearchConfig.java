package com.expedia.adaptivealerting.modelservice.detectormapper.service.es;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.google.common.base.Supplier;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Configuration
@Data
public class ElasticSearchConfig {

    @Value("${es.index.name}")
    private String indexName;
    @Value("${create.index.if.not.found:false}")
    private boolean createIndexIfNotFound;
    @Value("${es.doctype}")
    private String docType;
    @Value("${es.urls}")
    private String urls;
    @Value("${es.connection.timeout}")
    private int connectionTimeout;
    @Value("${es.max.connection.idletime}")
    private int maxConnectionIdleTime;
    @Value("${es.max.total.connection}")
    private int maxTotalConnection;
    @Value("${es.read.timeout}")
    private int readTimeout;
    @Value("${es.request.compression:false}")
    private boolean requestCompression;
    @Value("${es.username:@null}")
    private String username;
    @Value("${es.password:@null}")
    private String password;

    @Getter
    @Value("${es.enabled:false}")
    private boolean enabled;

    private RestHighLevelClient client;


    @PostConstruct
    public void init() {
        RestClientBuilder builder  = RestClient
                .builder(HttpHost.create(urls))
                .setRequestConfigCallback( req -> {
                    req.setConnectionRequestTimeout(connectionTimeout);
                    req.setConnectTimeout(connectionTimeout);
                    req.setSocketTimeout(connectionTimeout);
                    return req;
                }).setMaxRetryTimeoutMillis(connectionTimeout)
                .setHttpClientConfigCallback(req -> {
                            req.setMaxConnTotal(maxTotalConnection);
                            req.setMaxConnPerRoute(500);
                            return req;
                });
        addAWSRequestSignerInterceptor(builder);
        client = new RestHighLevelClient(builder);
    }
    
    private void addAWSRequestSignerInterceptor(RestClientBuilder clientBuilder) {
        AWSSigningRequestInterceptor signingInterceptor = getAWSRequestSignerInterceptor();
        clientBuilder.setHttpClientConfigCallback(
                clientConf -> clientConf.addInterceptorLast(signingInterceptor));
    }
    
    private AWSSigningRequestInterceptor getAWSRequestSignerInterceptor() {
        final Supplier<LocalDateTime> clock = () -> LocalDateTime.now(ZoneOffset.UTC);
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        val awsSigner = new AWSSigner(credentialsProvider, "us-west-2", "es", clock);
        return new AWSSigningRequestInterceptor(awsSigner);
    }

    @PreDestroy
    public void destroy() throws IOException {
        client.close();
    }

    @Bean
    public RestHighLevelClient getClient() {
        return client;
    }
}
