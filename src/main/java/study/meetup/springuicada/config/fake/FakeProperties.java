package study.meetup.springuicada.config.fake;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fake")
public record FakeProperties(Integer limitePadrao) {

    public FakeProperties {
        limitePadrao = limitePadrao == null ? 1000 : limitePadrao;
    }
}
