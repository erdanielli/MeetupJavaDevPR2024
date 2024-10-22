package study.meetup.springuicada.pg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.meetup.springuicada.application.ExibirExtrato;
import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao.Agendada.CreditoAgendado;
import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao.Agendada.DebitoAgendado;
import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao.Efetuada.Credito;
import study.meetup.springuicada.application.ExibirExtrato.Output.Operacao.Efetuada.Debito;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Service
@Transactional
public class ExibirExtratoPgAdapter implements ExibirExtrato {
    private final JdbcClient db;
    private final RowMapper<Output> rowMapper;

    public ExibirExtratoPgAdapter(JdbcClient db, ObjectMapper objectMapper) {
        this.db = db;
        this.rowMapper = new OutputRowMapper(objectMapper);
    }

    @Override
    public Output execute(Input input) {
        return db.sql("select exibir_extrato(:conta)")
                .param("conta", input.conta())
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
                case "CONTA_DESCONHECIDA" -> objectMapper.treeToValue(json, Output.ContaDesconhecida.class);
                case "EXTRATO" -> mapExtrato(json);
                default -> throw new NoSuchElementException(json.toString());
            };
        }

        private Output mapExtrato(JsonNode json) throws JsonProcessingException {
            var array = json.withArray("/extrato");
            if (array.isEmpty()) return new Output.Extrato(List.of());
            List<Output.Operacao> operacoes = new ArrayList<>();
            for (JsonNode opJson : array) {
                var opObj = switch (opJson.path("tipo").textValue()) {
                    case "CREDITO" -> objectMapper.treeToValue(opJson, Credito.class);
                    case "DEBITO" -> objectMapper.treeToValue(opJson, Debito.class);
                    case "CREDITO_AGENDADO" -> objectMapper.treeToValue(opJson, CreditoAgendado.class);
                    case "DEBITO_AGENDADO" -> objectMapper.treeToValue(opJson, DebitoAgendado.class);
                    default -> throw new NoSuchElementException(json.toString());
                };
                operacoes.add(opObj);
            }
            return new Output.Extrato(operacoes);
        }
    }
}
