package com.paymentchain.apigateway.setups;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.GlobalFilter;

@Configuration
@Slf4j
public class GlobalPostFilter {

    @Bean
    public GlobalFilter postFilter(){
        return (exchange,chain) ->{
            return chain.filter(exchange)
                    .then(Mono.fromRunnable(()->{
                        log.info("Global Post filter executed");
                    }));
        };
    }
}
