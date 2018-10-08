DROP EVENT IF EXISTS map_metric_model_event;

DELIMITER //

CREATE EVENT map_metric_model_event
-- will run on schedule to see there's been any new metrics corresponding to wilcard that needs to be mapped
    ON SCHEDULE EVERY 1 DAY
    DO
      CALL map_wildcard_metrics_to_model();
      //
DELIMITER ;
