package study.meetup.springuicada.application;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.*;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoEfetuada.ComSaldoNaoNegativo;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoEfetuada.UtilizandoLimite;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoNaoAutorizada.ContaDesconhecida;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoNaoAutorizada.SaldoInsuficiente;
import study.meetup.springuicada.application.util.UseCase;
import study.meetup.springuicada.application.util.UseCaseOutput;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

/// ## Efetua transferência entre duas contas
/// - Java 23 introduziu suporte ao Markdown para JavaDoc.
/// - Utilize esse recurso para documentar o caso de uso
///   - Fluxo principal e alternativos
public interface EfetuarTransferencia extends UseCase<EfetuarTransferencia.Input, EfetuarTransferencia.Output> {

    record Input(@NotNull(message = "ORIGEM_REQUERIDA") @Email(message = "ORIGEM_INVALIDA") String origem,
                 @NotNull(message = "DESTINO_REQUERIDO") @Email(message = "DESTINO_INVALIDO") String destino,
                 @NotNull(message = "DATA_REQUERIDA") @FutureOrPresent(message = "DATA_INVALIDA") LocalDate data,
                 @NotNull(message = "VALOR_REQUERIDO")
                 @Positive(message = "VALOR_INVALIDO")
                 @Digits(integer = 9, fraction = 2, message = "VALOR_INVALIDO") Double valor) {
    }

    @JsonTypeInfo(use = NAME, include = EXISTING_PROPERTY, property = "code", visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ContaDesconhecida.class, name = "CONTA_DESCONHECIDA"),
            @JsonSubTypes.Type(value = SaldoInsuficiente.class, name = "SALDO_INSUFICIENTE"),
            @JsonSubTypes.Type(value = Output.OperacaoAgendada.class, name = "OPERACAO_AGENDADA"),
            @JsonSubTypes.Type(value = ComSaldoNaoNegativo.class, name = "TRANSACAO_EFETUADA"),
            @JsonSubTypes.Type(value = UtilizandoLimite.class, name = "TRANSACAO_EFETUADA_UTILIZANDO_LIMITE")
    })
    sealed interface Output extends UseCaseOutput {

        sealed interface TransacaoNaoAutorizada extends Output {

            record ContaDesconhecida(String nome) implements TransacaoNaoAutorizada {
                @Override
                public String code() {
                    return "CONTA_DESCONHECIDA";
                }
            }

            record SaldoInsuficiente(double saldoAtual,
                                     double limiteDisponivel) implements TransacaoNaoAutorizada {
                @Override
                public String code() {
                    return "SALDO_INSUFICIENTE";
                }
            }
        }

        record OperacaoAgendada(LocalDate data, double valor) implements Output {
            @Override
            public String code() {
                return "OPERACAO_AGENDADA";
            }
        }

        sealed interface TransacaoEfetuada extends Output {

            LocalDateTime instante();

            double saldo();

            record ComSaldoNaoNegativo(LocalDateTime instante,
                                       double saldo,
                                       double limiteContratado) implements TransacaoEfetuada {
                @Override
                public String code() {
                    return "TRANSACAO_EFETUADA";
                }
            }

            record UtilizandoLimite(LocalDateTime instante,
                                    double saldo,
                                    double limiteDisponivel) implements TransacaoEfetuada {

                @Override
                public String code() {
                    return "TRANSACAO_EFETUADA_UTILIZANDO_LIMITE";
                }
            }
        }
    }
}
