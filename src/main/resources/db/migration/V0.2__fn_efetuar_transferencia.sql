create or replace function localizar_conta(email varchar) returns jsonb as
$$
declare
    js_return jsonb;
begin
    -- transforma o registro inteiro em JSON
    select row_to_json(c)::jsonb
    into js_return
    from conta c
    where c.email = $1;
    if js_return is null then
        -- JSON de acordo com o contrato da API
        select jsonb_build_object('code', 'CONTA_DESCONHECIDA', 'nome', email)
        into js_return;
    end if;
    return js_return;
end;
$$ language plpgsql;

-- quando aparecer n * 0.01 é porque estamos convertendo o valor monetário absoluto (bigint) em sua forma decimal.
-- Exemplo:
-- valor absoluto (da tabela) -> valor decimal (exposto pela API)
--                          1 -> 0.01 (um centavo)
--                         10 -> 0.1 (dez centavos)
--                        100 -> 1.0 (um real)
create or replace function efetuar_transferencia(email_origem varchar,
                                                 email_destino varchar,
                                                 valor bigint) returns jsonb as
$$
declare
    js_conta_origem   jsonb;
    js_conta_destino  jsonb;
    rec_conta_origem  record;
    rec_conta_destino record;
begin
    -- busca a conta de origem (em formato JSON)
    select localizar_conta($1) into js_conta_origem;
    -- se existir o atributo 'code' na raíz do json é porque retornou CONTA_DESCONHECIDA (linha 13 ⬆️)
    if js_conta_origem ? 'code' then
        return js_conta_origem;
    end if;
    -- repete a busca p/ conta de destino
    select localizar_conta($2) into js_conta_destino;
    if js_conta_destino ? 'code' then
        return js_conta_destino;
    end if;
    -- tenta debitar da conta
    update conta c
    set saldo = (saldo - $3)
    where conta_id = js_conta_origem['conta_id']::int
      and limite + saldo >= $3
    returning * into rec_conta_origem;
    -- sem saldo p/ cobrir o valor da transferência
    if rec_conta_origem.conta_id is null then
        return jsonb_build_object(
                'code', 'SALDO_INSUFICIENTE',
                'saldoAtual', js_conta_origem['saldo']::int * 0.01,
                'limiteDisponivel', (js_conta_origem['limite']::int + js_conta_origem['saldo']::bigint) * 0.01);
    end if;
    -- credita na conta
    update conta c
    set saldo = (saldo + $3)
    where conta_id = (js_conta_destino['conta_id'])::int
    returning * into rec_conta_destino;
    -- insere operacao de credito e debito
    insert into operacao (conta_id, conta_terceiro_id, data, valor)
    values (rec_conta_origem.conta_id, rec_conta_destino.conta_id, now(), -$3),
           (rec_conta_destino.conta_id, rec_conta_origem.conta_id, now(), $3);
    -- usando limite
    if rec_conta_origem.saldo < 0 then
        return jsonb_build_object(
                'code', 'TRANSACAO_EFETUADA_UTILIZANDO_LIMITE',
                'instante', now()::timestamp(0),  -- remove os milisegundos
                'saldo', rec_conta_origem.saldo * 0.01,
                'limiteDisponivel', (rec_conta_origem.limite + rec_conta_origem.saldo) * 0.01);
    end if;
    -- sem usar limite
    return jsonb_build_object(
            'code', 'TRANSACAO_EFETUADA',
            'instante', now()::timestamp(0),
            'saldo', rec_conta_origem.saldo * 0.01,
            'limiteContratado', rec_conta_origem.limite * 0.01);
end;
$$ language plpgsql;

grant execute on function efetuar_transferencia to myschema_user;

create or replace function efetuar_agendamento(email_origem varchar,
                                               email_destino varchar,
                                               valor bigint,
                                               data timestamp) returns jsonb as
$$
declare
    js_conta_origem  jsonb;
    js_conta_destino jsonb;
begin
    -- busca as contas envolvidas
    select localizar_conta($1) into js_conta_origem;
    if js_conta_origem ? 'code' then
        return js_conta_origem;
    end if;
    select localizar_conta($2) into js_conta_destino;
    if js_conta_destino ? 'code' then
        return js_conta_destino;
    end if;
    -- insere as operações de crédito e débito
    insert into operacao (conta_id, conta_terceiro_id, data, valor)
    values (js_conta_origem['conta_id']::int, js_conta_destino['conta_id']::int, $4, -$3),
           (js_conta_destino['conta_id']::int, js_conta_origem['conta_id']::int, $4, $3);
    return jsonb_build_object(
            'code', 'OPERACAO_AGENDADA',
            'data', $4::date,
            'valor', $3 * 0.01);
end;
$$ language plpgsql;

grant execute on function efetuar_agendamento to myschema_user;