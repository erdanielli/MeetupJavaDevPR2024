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
    with q_relevantes as (select data::date > now()::date as agendamento,
                                 abs(op.valor) * 0.01     as valor_decimal,
                                 c.email                  as outra_conta,
                                 op.data                  as data,
                                 valor < 0                as debito
                          from operacao op
                                   inner join conta c on op.conta_terceiro_id = c.conta_id
                          where op.conta_id = (origem_js['conta_id'])::int
                          order by 1 desc),
         q_metadados as (select case when debito then 'DEBITO' else 'CREDITO' end     as prefixo_tipo,
                                case when agendamento then '_AGENDADO' else '' end    as sufixo_tipo,
                                case when agendamento then 'data' else 'instante' end as data_label,
                                case
                                    when agendamento then data::date
                                    else data::timestamp(0) end                       as data_value,
                                case when debito then 'destino' else 'origem' end     as outra_conta_label,
                                outra_conta,
                                data,
                                valor_decimal
                         from q_relevantes),
         q_json as (select jsonb_build_object(
                                   'tipo', prefixo_tipo || sufixo_tipo,
                                   'valor', valor_decimal,
                                   outra_conta_label, outra_conta,
                                   data_label, data_value) as operacao_json
                    from q_metadados)
    select jsonb_build_object('code', 'EXTRATO', 'extrato', jsonb_agg(operacao_json))
    into extrato_js
    from q_json;
    return extrato_js;
end;
$$ language plpgsql;

grant execute on function exibir_extrato to myschema_user;