# -------------
DROP DATABASE IF EXISTS model_service;

create database model_service;

use model_service;


create table metric
(
  id         int unsigned primary key not null auto_increment,
  metric_key char(32) unique          not null
);

create table model
(
  id                mediumint unsigned primary key not null auto_increment,
  model_uuid        char(32) unique                not null,
  hyperparams       json,
  thresholds        json,
  to_rebuild        boolean                                 default false,
  training_location varchar(100),
  last_build_ts     timestamp
);


create table metric_model
(
  metric_id int unsigned       not null,
  model_id  mediumint unsigned not null,
  unique index (metric_id, model_id),

  constraint metric_id_fk foreign key (metric_id) references metric (id)
    on delete cascade
    on update cascade,
  constraint model_id_fk foreign key (model_id) references model (id)
    on delete cascade
    on update cascade
);

-- =====================================================================================================================
-- procedures
-- =====================================================================================================================


delimiter $$
create procedure add_new_model(
  _metric_key  char(32),
  _hyperparams json,
  _model_uuid  char(32))
  begin
    declare metricid int;
    insert ignore into metric (metric_key) values (_metric_key);
    select id
    into metricid
    from metric
    where metric_key = _metric_key;
    insert into model (model_uuid, hyperparams) values (_model_uuid, _hyperparams);
    insert into metric_model (metric_id, model_id) values (metricid, last_insert_id());
  end $$

create procedure update_threshold(
  _metric_key char(32),
  _threshold  json,
  _modeluuid  char(32)
)
  begin
    declare modelid mediumint unsigned;
    select model.id
    into modelid
    from metric_model
      join model on metric_model.model_id = model.id
    where metric_model.metric_id = (select id
                                    from metric
                                    where metric_key = _metric_key) and
          model_uuid = _modeluuid;

    update model
    set thresholds = _threshold
    where model.id = modelid;

  end $$
delimiter ;