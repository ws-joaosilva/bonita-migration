DROP INDEX idx_datamapp_container ON data_mapping
@@
DROP TABLE data_mapping
@@
DROP TABLE arch_data_mapping
@@
DELETE FROM "SEQUENCE" WHERE "ID" = 10021
@@
DELETE FROM "SEQUENCE" WHERE "ID" = 20051
@@