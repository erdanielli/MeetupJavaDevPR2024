package study.meetup.springuicada.pg;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.meetup.springuicada.application.EfetuarTransferencia;
import study.meetup.springuicada.domain.BRL;

import java.time.LocalDate;

@Primary
@Service
@Transactional
public class EfetuarTransferenciaPgAdapter implements EfetuarTransferencia {
    private final JdbcClient db;
    private final RowMapper<Output> rowMapper;

    public EfetuarTransferenciaPgAdapter(JdbcClient db, JsonRowMapperFactory jsonRowMapperFactory) {
        this.db = db;
        this.rowMapper = jsonRowMapperFactory.forType(Output.class);
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
}
