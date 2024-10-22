package study.meetup.springuicada.application.fake;

import study.meetup.springuicada.application.ExibirExtrato;
import study.meetup.springuicada.application.ExibirExtrato.Output.ContaDesconhecida;

import java.util.function.Function;

public final class ExibirExtratoFakeAdapter implements ExibirExtrato {
    private final Function<String, ContaFake> contaFakeProvider;

    public ExibirExtratoFakeAdapter(Function<String, ContaFake> contaFakeProvider) {
        this.contaFakeProvider = contaFakeProvider;
    }

    @Override
    public Output execute(Input input) {
        // valida a existencia das contas
        ContaFake conta = contaFakeProvider.apply(input.conta());
        if (conta == null) return new ContaDesconhecida(input.conta());
        return new Output.Extrato(conta.extrato());
    }
}
