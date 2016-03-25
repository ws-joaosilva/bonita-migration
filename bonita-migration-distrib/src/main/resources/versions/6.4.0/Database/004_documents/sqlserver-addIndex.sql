CREATE NONCLUSTERED INDEX idx_document_content
ON document_content (tenantid,documentId)
INCLUDE (id)
@@
