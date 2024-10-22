create role myschema_owner with login password 'myschema_owner';
create role myschema_user with login password 'myschema_user';
create schema myschema authorization myschema_owner;