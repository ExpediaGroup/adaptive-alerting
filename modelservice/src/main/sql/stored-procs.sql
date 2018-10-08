USE `aa_model_service`;

DROP PROCEDURE IF EXISTS insert_mapping;
DROP PROCEDURE IF EXISTS insert_model;
DROP PROCEDURE IF EXISTS insert_mapping_multiple_metrics_to_model;
DROP PROCEDURE IF EXISTS map_wildcard_metrics_to_model;


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

DELIMITER //

CREATE PROCEDURE insert_mapping_multiple_metrics_to_model (
  IN model_uuid CHAR(36),
  IN metric_ukey CHAR(100)
 )
  BEGIN
    DECLARE metric_id INT(10) UNSIGNED;
    DECLARE model_id INT(10) UNSIGNED;
    DECLARE done INT DEFAULT 0;
    DECLARE cur1 cursor for SELECT m.id FROM metric m WHERE m.ukey LIKE metric_ukey;
    DECLARE continue handler for not found set done=1;

    open cur1;

    REPEAT
    	FETCH cur1 into metric_id;
    	if NOT done then
    	SELECT id INTO model_id FROM model WHERE uuid = model_uuid;
    	 IF NOT EXISTS (SELECT 1 FROM metric_model_mapping m3 WHERE m3.metric_id = metric_id and m3.model_id = model_id)
    	 THEN
    	INSERT INTO metric_model_mapping (metric_id, model_id) VALUES (metric_id, model_id);
    	  END IF;
      END IF;
    UNTIL done END REPEAT;
    close cur1;
END //

CREATE PROCEDURE map_wildcard_metrics_to_model(
)
 BEGIN 
 -- add the mappings here during the migration of wildcard metrics, this here is the fist sample
 CALL insert_mapping_multiple_metrics_to_model('abfe507f-baff-4ebe-9c86-c002f396c8f6', "%ewetest.us-west-2.stats.gauges.affinity-prime-service%java.lang.OperatingSystem.ProcessCpuLoad%");
 END //
 
 DELIMITER ;


