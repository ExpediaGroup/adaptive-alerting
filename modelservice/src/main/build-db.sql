DROP DATABASE IF EXISTS model_service;

CREATE DATABASE model_service;

USE model_service;


CREATE TABLE metric
(
  id          INT UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT,
  metric_uuid CHAR(32) UNIQUE          NOT NULL
);

CREATE TABLE models
(
  id              MEDIUMINT UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT,
  folder_location VARCHAR(100),
  hyperparams     JSON,
  thresholds      JSON
);


CREATE TABLE model_instance
(
  id              SMALLINT UNSIGNED PRIMARY KEY       NOT NULL AUTO_INCREMENT,
  model_id        MEDIUMINT UNSIGNED                  NOT NULL,
  build_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  path            VARCHAR(100),
  CONSTRAINT model_instance_metric_id_fk FOREIGN KEY (model_id) REFERENCES models (id)
);

CREATE TABLE metric_model_map
(
  metric_id INT UNSIGNED       NOT NULL,
  model_id  MEDIUMINT UNSIGNED NOT NULL,
  CONSTRAINT metric_model_map_metric_id_fk FOREIGN KEY (metric_id) REFERENCES metric (id),
  CONSTRAINT metric_model_map_models_id_fk FOREIGN KEY (model_id) REFERENCES models (id)
);

INSERT INTO model_service.metric (metric_uuid) VALUES ('9a5af14e6c5d75d8015e1d228da310de');
INSERT INTO model_service.metric (metric_uuid) VALUES ('8a5af14e6c5d75d8015e1d228da3104c');
SELECT * FROM metric;

INSERT INTO model_service.models (folder_location, hyperparams, thresholds) VALUES ('s3:///adsfasdasd', '{"beta": 3, "alpha": 2}', '{"low": 400, "high": 300}');
INSERT INTO model_service.models (folder_location, hyperparams, thresholds) VALUES ('s3:///adsfasdasd', '{"beta": 3, "alpha": 2}', '{"low": 400, "high": 300}');

INSERT INTO model_service.metric_model_map (metric_id, model_id) VALUES (1, 1);
INSERT INTO model_service.metric_model_map (metric_id, model_id) VALUES (1, 2);

INSERT INTO model_service.model_instance (model_id, build_timestamp, path) VALUES (1, '2018-07-26 14:29:29', '/first');
INSERT INTO model_service.model_instance (model_id, build_timestamp, path) VALUES (1, '2018-07-26 14:29:46', '/second');