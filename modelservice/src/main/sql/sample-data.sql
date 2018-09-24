USE `aa_model_service`;

INSERT INTO `metric` (`ukey`, `hash`, `tags`) VALUES
  ('karmalab.stats.gauges.AirBoss.chelappabo003_karmalab_net.java.lang.Threading.ThreadCount', '1.71828d68a2938ff1ef96c340f12e2dd6', '{"unit": "unknown", "mtype": "gauge", "org_id": "1", "interval": "30"}')
;

INSERT INTO `model_type` (`ukey`) VALUES
  ('aquila-detector'),
  ('constant-detector'),
  ('cusum-detector'),
  ('ewma-detector'),
  ('individuals-detector'),
  ('pewma-detector'),
  ('rcf-detector')
;

CALL insert_model('3ec81aa2-2cdc-415e-b4f3-c1beb223ae60', 'cusum-detector');

CALL insert_mapping('1.71828d68a2938ff1ef96c340f12e2dd6', '3ec81aa2-2cdc-415e-b4f3-c1beb223ae60');
