create or replace function exibir_extrato(conta varchar) returns jsonb as
$$
declare
    origem_js  jsonb;
    extrato_js jsonb;
begin
    select localizar_conta($1) into origem_js;
    if origem_js ? 'code' then
        return origem_js;
    end if;
    with q_operacoes as (select data::date > now()::date as agendamento,
                                c.email                  as outra_conta,
                                op.data,
                                abs(op.valor) * 0.01     as valor,
                                valor < 0                as debito
                         from operacao op
                                  inner join conta c on op.outra_conta = c.conta_id
                         where op.conta_id = (origem_js ->> 'conta_id')::int
                         order by 1 desc),
         q_metadados as (select case when agendamento then '_AGENDADO' else '' end    as sufixo_tipo,
                                case when debito then 'DEBITO' else 'CREDITO' end     as prefix_tipo,
                                case when agendamento then 'data' else 'instante' end as data_label,
                                case when debito then 'destino' else 'origem' end     as outra_conta_label,
                                outra_conta,
                                data,
                                valor
                         from q_operacoes),
         q_json as (select jsonb_build_object(
                                   'tipo', prefix_tipo || sufixo_tipo,
                                   'valor', valor,
                                   outra_conta_label, outra_conta,
                                   data_label, case data_label when 'data' then data::date else data::timestamp(0) end)
                               as js_op
                    from q_metadados)
    select jsonb_build_object('code', 'EXTRATO', 'extrato', jsonb_agg(js_op))
    into extrato_js
    from q_json;
    return extrato_js;
end;
$$
    language plpgsql;

grant execute on function exibir_extrato to myschema_user;