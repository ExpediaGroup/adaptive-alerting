USE `aa_model_service`;

DROP PROCEDURE IF EXISTS insert_mapping;
DROP PROCEDURE IF EXISTS insert_model;
DROP PROCEDURE IF EXISTS insert_detector;

DELIMITER //

CREATE PROCEDURE insert_detector (
  IN uuid CHAR(36),
  IN type_ukey VARCHAR(100)
)
  BEGIN
    DECLARE type_id INT(5) UNSIGNED;

    SELECT t.id INTO type_id FROM model_type t WHERE t.ukey = type_ukey;
    INSERT INTO detector (uuid, model_type_id) VALUES (uuid, type_id);
  END //

CREATE PROCEDURE insert_model (
  IN uuid CHAR(36),
  IN params json
)
  BEGIN
    DECLARE detector_id INT(5) UNSIGNED;

    SELECT d.id INTO detector_id FROM detector d WHERE d.uuid = uuid;
    INSERT INTO model (detector_id, params) VALUES (detector_id, params);
  END //

CREATE PROCEDURE insert_mapping (
  IN metric_hash CHAR(36),
  IN detector_uuid CHAR(36)
)
  BEGIN
    DECLARE metric_id INT(10) UNSIGNED;
    DECLARE detector_id INT(10) UNSIGNED;

    SELECT m.id INTO metric_id FROM metric m WHERE m.hash = metric_hash;
    SELECT m.id INTO detector_id FROM detector m WHERE m.uuid = detector_uuid;
    INSERT INTO metric_detector_mapping (metric_id, detector_id) VALUES (metric_id, detector_id);
  END //

DELIMITER ;
