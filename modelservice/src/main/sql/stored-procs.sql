USE `aa_model_service`;

DROP PROCEDURE IF EXISTS insert_mapping;
DROP PROCEDURE IF EXISTS insert_model;
DROP PROCEDURE IF EXISTS insert_detector;

DROP PROCEDURE IF EXISTS insert_mapping_wildcard_metric_targets_to_model;
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
  IN params json,
  IN date_created timestamp
)
  BEGIN
    DECLARE detector_id INT(5) UNSIGNED;

    SELECT d.id INTO detector_id FROM detector d WHERE d.uuid = uuid;
    INSERT INTO model (detector_id, params , date_created) VALUES (detector_id, params, date_created);
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

CREATE PROCEDURE insert_mapping_wildcard_metric_targets_to_detector (
  IN detector_uuid CHAR(36),
  IN metric_ukey CHAR(100)
)
  BEGIN
    DECLARE metric_id INT(10) UNSIGNED;
    DECLARE detector_id INT(10) UNSIGNED;
    DECLARE done INT DEFAULT 0;
    DECLARE present INT DEFAULT 0;
    DECLARE cur1 cursor for SELECT m.id FROM metric m WHERE m.ukey LIKE metric_ukey;
    DECLARE continue handler for not found set done=1;

    open cur1;

    REPEAT
      FETCH cur1 into metric_id;
      if NOT done then
        SELECT id INTO detector_id FROM detector WHERE uuid = detector_uuid;
        IF NOT EXISTS (SELECT 1 FROM metric_detector_mapping m3 WHERE m3.metric_id = metric_id and m3.detector_id = detector_id)
        THEN
          INSERT INTO metric_detector_mapping (metric_id, detector_id) VALUES (metric_id, detector_id);
        END IF;
      END IF;
    UNTIL done END REPEAT;
    close cur1;
  END //

CREATE PROCEDURE populate_metric_tags()

begin

  DECLARE i, metric_tag_id INT DEFAULT 0;
  DECLARE tag, tag_key, tag_value varchar(4000);
  DECLARE metric_count int;
  DECLARE j int default 0;

  SET transaction isolation level read uncommitted;
  SET metric_count = 500;

  while j <= metric_count do
  select m.tags from metric_test m where id = j into tag;

  while (length(tag) > 0 AND i < json_length(tag)) do
    select json_unquote(json_extract(json_keys(tag),CONCAT('$[',i,']'))) into tag_key;
    select json_unquote(json_extract(json_extract(tag,'$.*'),CONCAT('$[',i,']'))) into tag_value;
    INSERT IGNORE INTO metric_tags (tag_id, tag_keys, tag_values) VALUES ((null),tag_key,tag_value);
    select tag_id from metric_tags where tag_keys = tag_key and tag_values = tag_value into metric_tag_id;
   INSERT INTO metric_mapper (map_id, id, tag_id) VALUES ((null), j, metric_tag_id);
    SELECT i + 1 INTO i;
  end while;
  set tag = null;
  SET i = 0;
  SELECT j + 1 INTO j;
  end while;

end //

DELIMITER ;
