package study.meetup.springuicada.application.fake;

import study.meetup.springuicada.domain.BRL;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ContaFake {
    private long saldoAtual;
    private final int limiteContratado;

    public ContaFake(long saldoInicial, int limiteContratado) {
        this.saldoAtual = saldoInicial;
        this.limiteContratado = limiteContratado;
    }

    void agendarDebito(double valor, LocalDate data, ContaFake contaDestino) {
        // TODO extrato
    }

    void agendarCredito(double valor, LocalDate data, ContaFake contaOrigem) {
        // TODO extrato
    }

    boolean debitar(double valor, LocalDateTime instante, ContaFake contaDestino) {
        long v = BRL.toLong(valor);
        if (v > limiteContratado + saldoAtual) return false;
        saldoAtual -= v;
        return true;
        // TODO extrato
    }

    void creditar(double valor, LocalDateTime instante, ContaFake contaOrigem) {
        saldoAtual += BRL.toLong(valor);
        // TODO extrato
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
}
