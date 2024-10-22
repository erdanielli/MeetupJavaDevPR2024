package study.meetup.springuicada.application.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface UseCaseOutput {

    @JsonProperty
    String code();

}
