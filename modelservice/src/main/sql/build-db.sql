DROP DATABASE IF EXISTS model_service;

CREATE DATABASE model_service;

USE model_service;


CREATE TABLE metric
(
  metric_id   INT UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT,
  metric_uuid CHAR(32) UNIQUE          NOT NULL
);

CREATE TABLE models
(
  model_id      MEDIUMINT UNSIGNED PRIMARY KEY      NOT NULL AUTO_INCREMENT,
  model_uuid    CHAR(32) UNIQUE                     NOT NULL,
  hyperparams   JSON,
  thresholds    JSON,
  to_rebuild    BOOLEAN                                      DEFAULT FALSE,
  last_build_ts TIMESTAMP
);


CREATE TABLE metric_model_map
(
  metric_id INT UNSIGNED       NOT NULL,
  model_id  MEDIUMINT UNSIGNED NOT NULL
);

-- =====================================================================================================================
-- PROCEDURES
-- =====================================================================================================================

DROP PROCEDURE IF EXISTS add_new_model;

CREATE PROCEDURE add_new_model(
  metricUuid CHAR(32),
  hyperParams JSON,
  modelUuid CHAR(32)
)
  BEGIN

    DECLARE metricID INT;

    INSERT IGNORE INTO metric (metric_uuid) VALUES (metricUuid);
    SELECT metric_id INTO metricID FROM metric WHERE metric_uuid=metricUuid;

    INSERT INTO  models (model_uuid, hyperparams) VALUES (modelUuid, hyperParams);
    INSERT INTO metric_model_map (metric_id,model_id) VALUES (metricID, LAST_INSERT_ID());

  END;

DROP PROCEDURE IF EXISTS update_threshold;
CREATE PROCEDURE update_threshold(
  metricUuid CHAR(32),
  thresHold JSON,
  modelUuid CHAR(32)
)
  BEGIN
    DECLARE modelID MEDIUMINT UNSIGNED;
    SELECT models.model_id INTO modelID
    FROM metric_model_map
      JOIN models ON metric_model_map.model_id = models.model_id
    WHERE metric_model_map.metric_id = (SELECT metric_id
                                        FROM metric
                                        WHERE metric_uuid = metricUuid) AND
          model_uuid = modelUuid;

    UPDATE models SET thresholds=thresHold WHERE model_id=modelID;

  END;


