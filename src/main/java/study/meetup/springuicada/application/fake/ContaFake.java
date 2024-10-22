package study.meetup.springuicada.application.fake;

import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao;
import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao.Agendada.CreditoAgendado;
import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao.Agendada.DebitoAgendado;
import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao.Efetuada.Credito;
import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao.Efetuada.Debito;
import study.meetup.springuicada.application.util.BRL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ContaFake {
    private final String email;
    private final int limiteContratado;
    private final List<Operacao> extrato;
    private long saldoAtual;

    public ContaFake(String email, int limiteContratado) {
        this.email = email;
        this.limiteContratado = limiteContratado;
        this.extrato = new ArrayList<>();
    }

    void agendarDebito(double valor, LocalDate data, ContaFake contaDestino) {
        extrato.add(new DebitoAgendado(valor, data, contaDestino.email));
    }

    void agendarCredito(double valor, LocalDate data, ContaFake contaOrigem) {
        extrato.add(new CreditoAgendado(valor, data, contaOrigem.email));
    }

    boolean debitar(double valor, LocalDateTime instante, ContaFake contaDestino) {
        long v = BRL.toLong(valor);
        if (v > limiteContratado + saldoAtual) return false;
        saldoAtual -= v;
        extrato.add(new Debito(valor, instante, contaDestino.email));
        return true;
    }

    void creditar(double valor, LocalDateTime instante, ContaFake contaOrigem) {
        saldoAtual += BRL.toLong(valor);
        extrato.add(new Credito(valor, instante, contaOrigem.email));
    }

    double saldoAtual() {
        return BRL.toDecimal(saldoAtual);
    }

    double limiteDisponivel() {
        return BRL.toDecimal(limiteContratado + saldoAtual);
    }

    boolean utilizandoLimite() {
        return saldoAtual < 0L;
    }

    double limiteContratado() {
        return BRL.toDecimal(limiteContratado);
    }

    public List<Operacao> extrato() {
        return extrato.stream().sorted(this::pelaData).toList();
    }

    private int pelaData(Operacao op1, Operacao op2) {
        Function<Operacao, LocalDateTime> getDateTime = operacao -> switch (operacao) {
            case Operacao.Agendada ag -> ag.data().atStartOfDay();
            case Operacao.Efetuada ef -> ef.instante();
        };
        return getDateTime.apply(op1).compareTo(getDateTime.apply(op2));
    }
}
