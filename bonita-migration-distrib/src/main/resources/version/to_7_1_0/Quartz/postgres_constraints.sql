
ALTER TABLE QRTZ_TRIGGERS  ADD
  CONSTRAINT PK_QRTZ_TRIGGERS PRIMARY KEY
  (
    SCHED_NAME,
    TRIGGER_NAME,
    TRIGGER_GROUP
  );

ALTER TABLE QRTZ_CRON_TRIGGERS ADD
  CONSTRAINT FK_QRTZ_CRON_TRIGGERS FOREIGN KEY
  (
    SCHED_NAME,
    TRIGGER_NAME,
    TRIGGER_GROUP
  ) REFERENCES QRTZ_TRIGGERS (
    SCHED_NAME,
    TRIGGER_NAME,
    TRIGGER_GROUP
  ) ON DELETE CASCADE;


ALTER TABLE QRTZ_SIMPLE_TRIGGERS ADD
  CONSTRAINT FK_QRTZ_SIMPLE_TRIGGERS FOREIGN KEY
  (
    SCHED_NAME,
    TRIGGER_NAME,
    TRIGGER_GROUP
  ) REFERENCES QRTZ_TRIGGERS (
    SCHED_NAME,
    TRIGGER_NAME,
    TRIGGER_GROUP
  ) ON DELETE CASCADE;

ALTER TABLE QRTZ_SIMPROP_TRIGGERS ADD
  CONSTRAINT FK_QRTZ_SIMPROP_TRIGGERS FOREIGN KEY
  (
    SCHED_NAME,
    TRIGGER_NAME,
    TRIGGER_GROUP
  ) REFERENCES QRTZ_TRIGGERS (
    SCHED_NAME,
    TRIGGER_NAME,
    TRIGGER_GROUP
  ) ON DELETE CASCADE;


ALTER TABLE QRTZ_TRIGGERS ADD
  CONSTRAINT FK_QRTZ_TRIGGERS FOREIGN KEY
  (
    SCHED_NAME,
    JOB_NAME,
    JOB_GROUP
  ) REFERENCES QRTZ_JOB_DETAILS (
    SCHED_NAME,
    JOB_NAME,
    JOB_GROUP
  );
