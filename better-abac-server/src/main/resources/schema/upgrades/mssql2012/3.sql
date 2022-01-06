alter table external_policy add phase varchar(255) not null default 'PRE_PROCESS';

update schema_version set version = 3;
