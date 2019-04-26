spring:
  main:
    web-environment: true
    show-banner: true
  datasource:
    driverClassName: "com.mysql.jdbc.Driver"
    url: "${db_endpoint}"
    username: $${DB_USERNAME}
    password: $${DB_PASSWORD}
    maximum-pool-size: 8
    minimum-idle: 0
    # TODO Add other data source settings here
  jpa:
    openInView: true
    database: MYSQL
    show-sql: false
    generate-ddl: false
    hibernate:
      naming_strategy: org.hibernate.cfg.ImprovedNamingStrategy
  data:
    rest:
      base-path: /api     
endpoints:
  health:
    sensitive: false
management:
  context-path: "/admin"
  metrics:
    export:
      jmx:
        domain: spring
    enable:
      jvm: false
      tomcat: false
      system: false
      process: false
security:
  signingKey: $${SIGNING_KEY}

datasource-es:
  create.index.if.not.found: true
  index.name: ${detector_mapper_index_name}
  doctype: ${detector_mapper_doctype}
  urls: ${detector_mapper_es_urls}
  config: ${detector_mapper_es_config_vars_json}
