create sequence hibernate_sequence start with 1 increment by 1;

create table external_id (
    party_id number(19,0) not null,
    external_id varchar2(255 char) not null,
    primary key (party_id, external_id));

create table external_policy (
    id number(19,0) not null,
    config clob not null,
    external_id varchar2(255 char) not null,
    name varchar2(255 char) not null,
    type number(10, 0) not null,
    phase varchar2(255 char) not null default 'PRE_PROCESS',
    version number(10, 0),
    external_system_id number(19,0) not null,
    primary key (id));

create table external_system (
    id number(19,0) not null,
    abac_rest_base_url varchar2(255 char),
    config_hash varchar2(255 char) not null,
    external_id varchar2(255 char) not null,
    name varchar2(255 char) not null,
    validation_status varchar2(255 char) not null,
    version number(10, 0),
    primary key (id));

create table party (
    id number(19,0) not null,
    version number(10, 0),
    type_id number(19,0) not null,
    primary key (id));

create table party_relation (
    id number(19,0) not null,
    valid_until timestamp,
    version number(10, 0),
    relation_type_id number(19,0) not null,
    source_id number(19,0) not null,
    target_id number(19,0) not null,
    primary key (id));

create table party_type (
    id number(19,0) not null,
    name varchar2(255 char),
    version number(10, 0),
    primary key (id));

create table plugin_state (
    id number(19,0) not null,
    initialized char(1) not null,
    plugin_id varchar2(255 char) not null,
    service_id varchar2(255 char) not null,
    sync_time timestamp, primary key (id));

create table policy (
    id number(19,0) not null,
    name varchar2(255 char) not null,
    policy clob not null,
    version number(10, 0),
    primary key (id));

create table relation_type (
    id number(19,0) not null,
    name varchar2(255 char) not null,
    version number(10, 0),
    allowed_source_id number(19,0),
    allowed_target_id number(19,0),
    primary key (id));

create table shedlock (
    name varchar2(255 char) not null,
    lock_until timestamp not null,
    locked_at timestamp not null,
    locked_by varchar2(255 char) not null,
    primary key (name));

alter table external_policy add constraint UKm4bp3bhm6u4d3t57ll7dy59tu unique (external_system_id, external_id);

alter table external_system add constraint UK6iq0kpajaqy8ok1th5l53udw5 unique (external_id);

alter table policy add constraint UK_8400610a8nl6feew9oty0mgyf unique (name);

alter table relation_type add constraint UK_dqprukb42qt2xmwu1vgg1oqsv unique (name);

alter table external_id add constraint xf_party foreign key (party_id) references party;

alter table external_policy add constraint FK19u7rvjn1aqk9nytfnfvvwqhc foreign key (external_system_id) references external_system;

alter table party add constraint xf_party_type foreign key (type_id) references party_type;

alter table party_relation add constraint xf_relation_type foreign key (relation_type_id) references relation_type;

alter table party_relation add constraint xf_relation_source foreign key (source_id) references party;

alter table party_relation add constraint xf_relation_target foreign key (target_id) references party;

alter table relation_type add constraint xf_pt_allowed_source foreign key (allowed_source_id) references party_type;

alter table relation_type add constraint xf_pt_allowed_target foreign key (allowed_target_id) references party_type;

alter table party_type add constraint UK_lg17a8bvvbyc2nk99gj959slj unique (name);

create index xp_party_source on party_relation (source_id);

create index xp_party_target on party_relation (target_id);

create index xp_party_type on party_relation (relation_type_id);

create index xp_party_multi on party_relation (source_id, relation_type_id, target_id);
