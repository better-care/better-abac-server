alter table external_policy add phase varchar2(255 char) not null default 'PRE_PROCESS';

update schema_version set version = 3;
