create sequence hibernate_sequence start with 1 increment by 1;

create table external_id (
    party_id bigint not null,
    external_id varchar(255) not null,
    primary key (party_id, external_id));

create table external_policy (
    id bigint not null,
    config varchar(max) not null,
    external_id varchar(255) not null,
    name varchar(255) not null,
    type int not null,
    phase varchar(255) not null default 'PRE_PROCESS',
    version int,
    external_system_id bigint not null,
    primary key (id));

create table external_system (
    id bigint not null,
    abac_rest_base_url varchar(255),
    config_hash varchar(255) not null,
    external_id varchar(255) not null,
    name varchar(255) not null,
    validation_status varchar(255) not null,
    version int,
    primary key (id));

create table party (
    id bigint not null,
    version int,
    type_id bigint not null,
    primary key (id));

create table party_relation (
    id bigint not null,
    valid_until datetime2,
    version int,
    relation_type_id bigint not null,
    source_id bigint not null,
    target_id bigint not null,
    primary key (id));

create table party_type (
    id bigint not null,
    name varchar(255),
    version int,
    primary key (id));

create table plugin_state (
    id bigint not null,
    initialized bit not null,
    plugin_id varchar(255) not null,
    service_id varchar(255) not null,
    sync_time datetime2,
    primary key (id));

create table policy (
    id bigint not null,
    name varchar(255) not null,
    policy varchar(max) not null,
    version int,
    primary key (id));

create table relation_type (
    id bigint not null,
    name varchar(255) not null,
    version int,
    allowed_source_id bigint,
    allowed_target_id bigint,
    primary key (id));

create table shedlock (
    name varchar(255) not null,
    lock_until datetime2 not null,
    locked_at datetime2 not null,
    locked_by varchar(255) not null,
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

create index xp_party_source on party_relation (source_id);
create index xp_party_target on party_relation (target_id);
create index xp_party_type on party_relation (relation_type_id);
create index xp_party_multi on party_relation (source_id, relation_type_id, target_id);
alter table party_type add constraint UK_lg17a8bvvbyc2nk99gj959slj unique (name);
