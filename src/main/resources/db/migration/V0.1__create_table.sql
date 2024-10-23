create table conta
(
    conta_id serial primary key,
    email    varchar(250) unique not null,
    limite   int                 not null,
    saldo    bigint              not null default 0
);
grant select, insert, update on table conta to myschema_user;

create table operacao
(
    operacao_id       bigserial primary key,
    conta_id          int                     not null references conta,
    conta_terceiro_id int                     not null references conta,
    data              timestamp default now() not null,
    valor             bigint                  not null
);
create index operacao_conta_id on operacao (conta_id);

grant select, insert, update on table operacao to myschema_user;
