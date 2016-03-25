import org.bonitasoft.migration.core.MigrationUtil
import org.bonitasoft.migration.versions.v6_3_9_to_6_4_0.ChangeDocumentsStructure

MigrationUtil.executeSqlFile(feature, dbVendor, "addIndex", parameters, sql, false)

new ChangeDocumentsStructure(sql, dbVendor).migrate();