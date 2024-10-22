create or replace function localizar_conta(email varchar) returns jsonb as
$$
declare
    output jsonb;
begin
    select row_to_json(c)::jsonb
    into output
    from conta c
    where c.email = $1;
    if output is null then
        select jsonb_build_object(
                       'code', 'CONTA_DESCONHECIDA',
                       'nome', email)
        into output;
    end if;
    return output;
end;
$$
    language plpgsql;

create or replace function efetuar_transferencia(email_origem varchar,
                                                 email_destino varchar,
                                                 valor bigint) returns jsonb as
$$
declare
    origem_js   jsonb;
    destino_js  jsonb;
    origem_rec  record;
    destino_rec record;
begin
    -- valida a existencias das contas envolvidas
    select localizar_conta($1) into origem_js;
    if origem_js ? 'code' then
        return origem_js;
    end if;
    select localizar_conta($2) into destino_js;
    if destino_js ? 'code' then
        return destino_js;
    end if;
    -- tenta debitar da conta
    update conta c
    set saldo = (saldo - $3)
    where conta_id = (origem_js ->> 'conta_id')::int
      and limite + saldo >= $3
    returning * into origem_rec;
    -- sem saldo
    if origem_rec.conta_id is null then
        return jsonb_build_object(
                'code', 'SALDO_INSUFICIENTE',
                'saldoAtual', (origem_js ->> 'saldo')::int * 0.01,
                'limiteDisponivel', ((origem_js ->> 'limite')::int + (origem_js ->> 'saldo')::int) * 0.01);
    end if;
    -- credita na conta
    update conta c
    set saldo = (saldo + $3)
    where conta_id = (destino_js ->> 'conta_id')::int
    returning * into destino_rec;
    -- insere operacao de credito e debito
    insert into operacao (conta_id, outra_conta, data, valor)
    values (origem_rec.conta_id, destino_rec.conta_id, now(), -$3),
           (destino_rec.conta_id, origem_rec.conta_id, now(), $3);
    -- usando limite
    if origem_rec.saldo < 0 then
        return jsonb_build_object(
                'code', 'TRANSACAO_EFETUADA_UTILIZANDO_LIMITE',
                'instante', now()::timestamp(0),
                'saldo', origem_rec.saldo * 0.01,
                'limiteDisponivel', (origem_rec.limite + origem_rec.saldo) * 0.01);
    end if;
    -- sem usar limite
    return jsonb_build_object(
            'code', 'TRANSACAO_EFETUADA',
            'instante', now()::timestamp(0),
            'saldo', origem_rec.saldo * 0.01,
            'limiteContratado', origem_rec.limite * 0.01);
end;
$$
    language plpgsql;

create or replace function efetuar_agendamento(email_origem varchar,
                                               email_destino varchar,
                                               valor bigint,
                                               data timestamp) returns jsonb as
$$
declare
    origem_js  jsonb;
    destino_js jsonb;
begin
    -- valida a existencias das contas envolvidas
    select localizar_conta($1) into origem_js;
    if origem_js ? 'code' then
        return origem_js;
    end if;
    select localizar_conta($2) into destino_js;
    if destino_js ? 'code' then
        return destino_js;
    end if;
    -- insere operacao de credito e debito
    insert into operacao (conta_id, outra_conta, data, valor)
    values ((origem_js ->> 'conta_id')::int, (destino_js ->> 'conta_id')::int, $4, -$3),
           ((destino_js ->> 'conta_id')::int, (origem_js ->> 'conta_id')::int, $4, $3);
    return jsonb_build_object(
            'code', 'OPERACAO_AGENDADA',
            'data', $4::date,
            'valor', $3 * 0.01);
end;
$$
    language plpgsql;