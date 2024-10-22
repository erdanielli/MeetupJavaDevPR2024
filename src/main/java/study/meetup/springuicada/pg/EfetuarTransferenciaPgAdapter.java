package study.meetup.springuicada.pg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.meetup.springuicada.application.EfetuarTransferencia;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.OperacaoAgendada;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoEfetuada.ComSaldoNaoNegativo;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoEfetuada.UtilizandoLimite;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoNaoAutorizada.ContaDesconhecida;
import study.meetup.springuicada.application.EfetuarTransferencia.Output.TransacaoNaoAutorizada.SaldoInsuficiente;
import study.meetup.springuicada.domain.BRL;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Primary
@Service
@Transactional
public class EfetuarTransferenciaPgAdapter implements EfetuarTransferencia {
    private final JdbcClient db;
    private final RowMapper<Output> rowMapper;

    public EfetuarTransferenciaPgAdapter(JdbcClient db, ObjectMapper objectMapper) {
        this.db = db;
        this.rowMapper = new OutputRowMapper(objectMapper);
    }

    @Override
    public Output execute(Input input) {
        // fluxo de agendamento se data for futura (sem validar saldo)
        if (input.data().isAfter(LocalDate.now())) {
            return agendamento(input);
        }
        return imediato(input);
    }

    private Output agendamento(Input input) {
        LocalDate dataEfetiva = input.data();
        dataEfetiva = switch (dataEfetiva.getDayOfWeek()) {
            case SATURDAY -> dataEfetiva.plusDays(2L);
            case SUNDAY -> dataEfetiva.plusDays(1L);
            default -> dataEfetiva;
        };
        return db.sql("select efetuar_agendamento(:origem, :destino, :valor, :data)")
                .param("origem", input.origem())
                .param("destino", input.destino())
                .param("valor", BRL.toLong(input.valor()))
                .param("data", dataEfetiva)
                .query(rowMapper)
                .single();
    }

    private Output imediato(Input input) {
        return db.sql("select efetuar_transferencia(:origem, :destino, :valor)")
                .param("origem", input.origem())
                .param("destino", input.destino())
                .param("valor", BRL.toLong(input.valor()))
                .query(rowMapper)
                .single();
    }

    private static class OutputRowMapper implements RowMapper<Output> {
        final ObjectMapper objectMapper;

        OutputRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Output mapRow(ResultSet rs, int rowNum) throws SQLException {
            try (var jsonb = rs.getBinaryStream(1)) {
                if (jsonb == null) return null;
                return map(objectMapper.readTree(jsonb));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private Output map(JsonNode json) throws JsonProcessingException {
            return switch (json.path("code").textValue()) {
                case "CONTA_DESCONHECIDA" -> objectMapper.treeToValue(json, ContaDesconhecida.class);
                case "SALDO_INSUFICIENTE" -> objectMapper.treeToValue(json, SaldoInsuficiente.class);
                case "OPERACAO_AGENDADA" -> objectMapper.treeToValue(json, OperacaoAgendada.class);
                case "TRANSACAO_EFETUADA" -> objectMapper.treeToValue(json, ComSaldoNaoNegativo.class);
                case "TRANSACAO_EFETUADA_UTILIZANDO_LIMITE" -> objectMapper.treeToValue(json, UtilizandoLimite.class);
                default -> throw new NoSuchElementException(json.toString());
            };
        }
    }


}
