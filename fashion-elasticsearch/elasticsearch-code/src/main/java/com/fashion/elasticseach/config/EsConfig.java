package com.fashion.elasticseach.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Lazy
@Configuration
public class EsConfig {

    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.cluster-name}")
    private String esClusterName;

    @Bean
    public TransportClient esClient() throws UnknownHostException {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        Settings settings = Settings.builder().put("cluster.name", esClusterName).build();
        TransportClient esClient = new PreBuiltTransportClient(settings);
        if (esHost.contains(",")) {
            String[] host = esHost.split(",");
            for (String string : host) {
                InetSocketTransportAddress address = new InetSocketTransportAddress(InetAddress.getByName(string),
                        9300);
                esClient.addTransportAddress(address);//可以add多个节点
            }
        } else {
            InetSocketTransportAddress address = new InetSocketTransportAddress(InetAddress.getByName(esHost), 9300);
            esClient.addTransportAddress(address);//可以add多个节点
        }
        return esClient;
    }
}
