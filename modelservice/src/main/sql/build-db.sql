# -------------
DROP DATABASE IF EXISTS aa_model_service;

create database aa_model_service;

use aa_model_service;

create table metric
(
  id        int unsigned primary key not null auto_increment,
  m_key     varchar(100) unique not null,
  hash      varchar(100) unique not null,
  description varchar(100) unique not null,
  tags      json
);

create table model_type
(
  id        smallint unsigned primary key not null auto_increment,
  `key`     varchar(100) unique not null
);

create table model
(
  id                  int unsigned primary key not null auto_increment,
  uuid                char(36) unique not null,
  type_id             smallint unsigned not null,
  hyperparams         json,
  training_location   varchar(300),
  weak_sigmas         decimal(3, 3),
  strong_sigmas       decimal(3, 3),
  last_build_ts       timestamp,
  other_stuff         json,
  constraint type_id_fk foreign key (type_id) references model_type (id)
);

create table metric_model_mapping
(
  id         int unsigned primary key not null auto_increment,
  metric_id  int unsigned not null,
  model_id   int unsigned not null,
  constraint metric_id_fk foreign key (metric_id) references metric (id),
  constraint model_id_fk foreign key (model_id) references model (id),
  unique index (metric_id, model_id)
);
