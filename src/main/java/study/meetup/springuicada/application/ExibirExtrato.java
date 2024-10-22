package study.meetup.springuicada.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import study.meetup.springuicada.application.util.UseCase;
import study.meetup.springuicada.application.util.UseCaseOutput;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/// Recupera o extrato completo da conta
public interface ExibirExtrato extends UseCase<ExibirExtrato.Input, ExibirExtrato.Output> {

    record Input(@NotNull(message = "CONTA_REQUERIDA") @Email(message = "CONTA_INVALIDA") String conta) {
    }

    sealed interface Output extends UseCaseOutput {

        record ContaDesconhecida(String nome) implements Output {
            @Override
            public String code() {
                return "CONTA_DESCONHECIDA";
            }
        }

        record Extrato(List<Operacao> extrato) implements Output {
            @Override
            public String code() {
                return "EXTRATO";
            }
        }

        sealed interface Operacao {
            @JsonProperty
            String tipo();

            double valor();

            sealed interface Efetuada extends Operacao {
                LocalDateTime instante();

                record Credito(double valor, LocalDateTime instante, String origem) implements Efetuada {
                    @Override
                    public String tipo() {
                        return "CREDITO";
                    }
                }

                record Debito(double valor, LocalDateTime instante, String destino) implements Efetuada {
                    @Override
                    public String tipo() {
                        return "DEBITO";
                    }
                }
            }

            sealed interface Agendada extends Operacao {
                LocalDate data();

                record CreditoAgendado(double valor, LocalDate data, String origem) implements Agendada {
                    @Override
                    public String tipo() {
                        return "CREDITO_AGENDADO";
                    }
                }

                record DebitoAgendado(double valor, LocalDate data, String destino) implements Agendada {
                    @Override
                    public String tipo() {
                        return "DEBITO_AGENDADO";
                    }
                }
            }
        }
    }
}
