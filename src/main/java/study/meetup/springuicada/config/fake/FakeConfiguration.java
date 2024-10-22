package study.meetup.springuicada.config.fake;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import study.meetup.springuicada.application.EfetuarTransferencia;
import study.meetup.springuicada.application.ExibirExtrato;
import study.meetup.springuicada.application.fake.ContaFake;
import study.meetup.springuicada.application.fake.EfetuarTransferenciaFakeAdapter;
import study.meetup.springuicada.application.fake.ExibirExtratoFakeAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FakeProperties.class)
public class FakeConfiguration {

    @Bean
    @Lazy
    Function<String, ContaFake> contaFakeProvider(FakeProperties fakeProperties) {
        Map<String, ContaFake> contasMap = new HashMap<>();
        return conta -> {
            if ("nao@existe.com".equalsIgnoreCase(conta)) return null;
            return contasMap.computeIfAbsent(conta.toLowerCase(),
                    email -> new ContaFake(email, fakeProperties.limitePadrao() * 100));
        };
    }

    @Bean
    @ConditionalOnMissingBean(EfetuarTransferencia.class)
    EfetuarTransferencia efetuarTransferenciaFakeAdapter(Function<String, ContaFake> contaFakeProvider) {
        return new EfetuarTransferenciaFakeAdapter(contaFakeProvider);
    }

    @Bean
    @ConditionalOnMissingBean(ExibirExtrato.class)
    ExibirExtrato exibirExtratoFakeAdapter(Function<String, ContaFake> contaFakeProvider) {
        return new ExibirExtratoFakeAdapter(contaFakeProvider);
    }

}
