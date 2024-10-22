package study.meetup.springuicada.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import study.meetup.springuicada.application.ExibirExtrato;
import study.meetup.springuicada.application.ExibirExtrato.Output.ContaDesconhecida;
import study.meetup.springuicada.application.ExibirExtrato.Output.Extrato;

@RestController
public class ExibirExtratoController {
    private final ExibirExtrato usecase;

    public ExibirExtratoController(ExibirExtrato useCase) {
        this.usecase = useCase;
    }

    @PostMapping("/extrato")
    ResponseEntity<ExibirExtrato.Output> execute(@RequestBody @Valid ExibirExtrato.Input input) {
        return switch (usecase.apply(input)) {
            case Extrato output -> ResponseEntity.status(200).body(output);
            case ContaDesconhecida output -> ResponseEntity.status(400).body(output);
        };
    }

}
