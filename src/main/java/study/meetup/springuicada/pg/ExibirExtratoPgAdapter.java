package study.meetup.springuicada.pg;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.meetup.springuicada.application.ExibirExtrato;

@Primary
@Service
@Transactional
public class ExibirExtratoPgAdapter implements ExibirExtrato {
    private final JdbcClient db;
    private final RowMapper<Output> rowMapper;

    public ExibirExtratoPgAdapter(JdbcClient db, JsonRowMapperFactory jsonRowMapperFactory) {
        this.db = db;
        this.rowMapper = jsonRowMapperFactory.forType(Output.class);
    }

    @Override
    public Output execute(Input input) {
        return db.sql("select exibir_extrato(:conta)")
                .param("conta", input.conta())
                .query(rowMapper)
                .single();
    }
}
