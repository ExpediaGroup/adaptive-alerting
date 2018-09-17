# -------------
DROP DATABASE IF EXISTS model_service;

create database model_service;

use model_service;


create table metric
(
  id        int unsigned primary key not null auto_increment,
  m_key     varchar(100) unique not null,
  hash      varchar(100) unique not null,
  tags      json
);

create table model
(
  id                  int unsigned primary key not null auto_increment,
  uuid                varchar(100) unique not null,
  hyperparams         json,
  training_location   varchar(300),
  last_build_ts       timestamp
);


create table metric_model_mapping
(
  id         int unsigned primary key not null  AUTO_INCREMENT,
  metric_id  int unsigned not null,
  model_id   int unsigned not null,
  constraint metric_id_fk foreign key (metric_id) references metric (id)
    on delete cascade
    on update cascade,
  constraint model_id_fk foreign key (model_id) references model (id)
    on delete cascade
    on update cascade
);