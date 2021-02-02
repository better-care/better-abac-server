create index xp_system_policy on external_policy (external_system_id);
create index p_party_type on party (type_id);
create index rt_allowed_source on relation_type (allowed_source_id);
create index rt_allowed_target on relation_type (allowed_target_id);