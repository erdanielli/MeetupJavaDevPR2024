package study.meetup.springuicada.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import study.meetup.springuicada.application.EfetuarTransferencia;
import study.meetup.springuicada.application.EfetuarTransferencia.Output;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.OperacaoAgendada;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoEfetuada;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoNaoAutorizada;

@RestController
public class EfetuarTransferenciaController {
    private final EfetuarTransferencia usecase;

    public EfetuarTransferenciaController(EfetuarTransferencia useCase) {
        this.usecase = useCase;
    }

    @PostMapping("/transferir")
    ResponseEntity<Output> execute(@RequestBody @Valid EfetuarTransferencia.Input input) {
        return switch (usecase.execute(input)) {
            case OperacaoAgendada output -> ResponseEntity.status(200).body(output);
            case TransacaoEfetuada output -> ResponseEntity.status(200).body(output);
            case TransacaoNaoAutorizada output -> ResponseEntity.status(400).body(output);
        };
    }

}
