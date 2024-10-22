package study.meetup.springuicada.application.fake;

import study.meetup.springuicada.application.EfetuarTransferencia;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoEfetuada.ComSaldoNaoNegativo;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoEfetuada.UtilizandoLimite;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoNaoAutorizada.ContaDesconhecida;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoNaoAutorizada.SaldoInsuficiente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

public final class EfetuarTransferenciaFakeAdapter implements EfetuarTransferencia {
    private final Function<String, ContaFake> contaFakeProvider;

    public EfetuarTransferenciaFakeAdapter(Function<String, ContaFake> contaFakeProvider) {
        this.contaFakeProvider = contaFakeProvider;
    }

    @Override
    public Output execute(Input input) {
        // valida a existencia das contas
        ContaFake contaOrigem = contaFakeProvider.apply(input.origem());
        if (contaOrigem == null) return new ContaDesconhecida(input.origem());
        ContaFake contaDestino = contaFakeProvider.apply(input.destino());
        if (contaDestino == null) return new ContaDesconhecida(input.destino());

        // fluxo de agendamento se data for futura (sem validar saldo)
        if (input.data().isAfter(LocalDate.now())) {
            return agendamento(input, contaOrigem, contaDestino);
        }
        return imediato(input, contaOrigem, contaDestino, LocalDateTime.now());
    }

    private Output agendamento(Input input, ContaFake contaOrigem, ContaFake contaDestino) {
        LocalDate dataEfetiva = input.data();
        dataEfetiva = switch (dataEfetiva.getDayOfWeek()) {
            case SATURDAY -> dataEfetiva.plusDays(2L);
            case SUNDAY -> dataEfetiva.plusDays(1L);
            default -> dataEfetiva;
        };
        contaOrigem.agendarDebito(input.valor(), dataEfetiva, contaDestino);
        contaDestino.agendarCredito(input.valor(), dataEfetiva, contaOrigem);
        return new Output.OperacaoAgendada(dataEfetiva, input.valor());
    }

    private Output imediato(Input input, ContaFake contaOrigem, ContaFake contaDestino, LocalDateTime instante) {
        if (!contaOrigem.debitar(input.valor(), instante, contaDestino)) {
            return new SaldoInsuficiente(contaOrigem.saldoAtual(), contaOrigem.limiteDisponivel());
        }
        contaDestino.creditar(input.valor(), instante, contaOrigem);
        if (contaOrigem.utilizandoLimite()) {
            return new UtilizandoLimite(instante, contaOrigem.saldoAtual(), contaOrigem.limiteDisponivel());
        }
        return new ComSaldoNaoNegativo(instante, contaOrigem.saldoAtual(), contaOrigem.limiteContratado());
    }
}
