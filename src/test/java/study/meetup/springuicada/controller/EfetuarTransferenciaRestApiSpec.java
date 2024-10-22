package study.meetup.springuicada.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/// - cada adapter deve criar uma subclasse e configurar um profile p/ criar o bean
/// - documentar as condições iniciais necessárias:
///   - email da contas, saldo inicial e limites
/// - cabe à subclasse garantir essas condições.
/// - Os testes da Spec não podem ser alterados na subclasse.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class EfetuarTransferenciaRestApiSpec {

    @Test
    @Order(1)
    void deveDebitarUtilizandoLimite() {
        // transferir de A p/ B um valor X dentro do limite
    }

    @Test
    @Order(2)
    void deveDebitarSemPrecisarDoLimite() {
        // transferir de B p/ A um valor menor que X
    }

    // .. passar por todos os fluxos
}