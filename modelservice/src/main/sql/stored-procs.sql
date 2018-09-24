USE `aa_model_service`;

DROP PROCEDURE IF EXISTS insert_mapping;
DROP PROCEDURE IF EXISTS insert_model;

DELIMITER //

CREATE PROCEDURE insert_model (
  IN uuid CHAR(36),
  IN type_ukey VARCHAR(100)
)
  BEGIN
    DECLARE type_id SMALLINT(5) UNSIGNED;

    SELECT t.id INTO type_id FROM model_type t WHERE t.ukey = type_ukey;
    INSERT INTO model (uuid, type_id) VALUES (uuid, type_id);
  END //

CREATE PROCEDURE insert_mapping (
  IN metric_hash CHAR(36),
  IN model_uuid CHAR(36)
)
  BEGIN
    DECLARE metric_id INT(10) UNSIGNED;
    DECLARE model_id INT(10) UNSIGNED;

    SELECT m.id INTO metric_id FROM metric m WHERE m.hash = metric_hash;
    SELECT m.id INTO model_id FROM model m WHERE m.uuid = model_uuid;
    INSERT INTO metric_model_mapping (metric_id, model_id) VALUES (metric_id, model_id);
  END //

DELIMITER ;
