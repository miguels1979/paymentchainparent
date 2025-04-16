package com.paymentchain.customer.configuration;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    /**
     *
     * Esto es útil si quieres que cada clase cree su propio WebClient, pero partiendo de una configuración común.
     */

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder(){
        HttpClient client = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .responseTimeout(Duration.ofSeconds(1))
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(5000,TimeUnit.MILLISECONDS));
                });

        String osName =  System.getProperty("os.name").toLowerCase();
        if(osName.contains("linux")){
            client = client.option(EpollChannelOption.TCP_KEEPIDLE,300);
            client = client.option(EpollChannelOption.TCP_KEEPINTVL,60);
        }

        return WebClient.builder()
                //.baseUrl("http://localhost:8083/")//define la url base
                .clientConnector(new ReactorClientHttpConnector(client))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                //.defaultUriVariables(Collections.singletonMap("url", "http://localhost:8083/product"));
    }
}
