/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.migration

import groovy.xml.StreamingMarkupBuilder
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.database.schema.IndexDefinition
import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.dbunit.ext.oracle.OracleConnection

import java.sql.DriverManager
import java.sql.SQLException

/**
 * @author Baptiste Mesta
 */
class DBUnitHelper {

    private MigrationContext context

    DBUnitHelper(MigrationContext context) {
        context.loadProperties();
        context.openSqlConnection()
        this.context = context
    }

    static Map trueValueMap = [
            (MigrationStep.DBVendor.ORACLE)   : 1,
            (MigrationStep.DBVendor.POSTGRES) : true,
            (MigrationStep.DBVendor.MYSQL)    : true,
            (MigrationStep.DBVendor.SQLSERVER): true
    ]
    static Map falseValueMap = [
            (MigrationStep.DBVendor.ORACLE)   : 0,
            (MigrationStep.DBVendor.POSTGRES) : false,
            (MigrationStep.DBVendor.MYSQL)    : false,
            (MigrationStep.DBVendor.SQLSERVER): false
    ]

    def trueValue() {
        trueValueMap.get(context.dbVendor)
    }

    def falseValue() {
        falseValueMap.get(context.dbVendor)

    }

    def static dataSet(data) {
        new ReplacementDataSet(new FlatXmlDataSet(new StringReader(new StreamingMarkupBuilder().bind {
            dataset data
        }.toString())), ["[NULL]": null], null)
    }


    def executeScript(URL resource) {
        resource.text.split("@@|GO|;").each({ String stmt ->
            stmt = stmt.trim()
            if (!stmt.isEmpty()) {
            context.sql.execute(stmt)
            }
        })
    }

    def String[] createTables(String folder, String feature) {
        println "Create tables from sql file in $folder with suffix $feature"
        executeScript(DBUnitHelper.class.getClassLoader().getResource("sql/v${folder}/${context.dbVendor.name().toLowerCase()}-${feature}.sql"))
    }

    def String[] createTables(String folder) {
        println "Create tables from sql file in $folder"
        executeScript(DBUnitHelper.class.getClassLoader().getResource("sql/v${folder}/${context.dbVendor.name().toLowerCase()}.sql"))
    }

    def boolean hasTable(String tableName) {
        def query
        switch (context.dbVendor) {
            case MigrationStep.DBVendor.POSTGRES:
                query = """
                    SELECT *
                     FROM information_schema.tables
                     WHERE table_schema='public'
                       AND table_type='BASE TABLE'
                       AND UPPER(table_name) = UPPER($tableName)
                    """
                break

            case MigrationStep.DBVendor.ORACLE:
                query = """
                    SELECT *
                    FROM user_tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    """
                break

            case MigrationStep.DBVendor.MYSQL:
                query = """
                    SELECT *
                    FROM information_schema.tables
                    WHERE UPPER(table_name) = UPPER($tableName)
                    AND table_schema = DATABASE()
                    """
                break

            case MigrationStep.DBVendor.SQLSERVER:
                query = """
                    SELECT * FROM information_schema.tables
                    WHERE UPPER(TABLE_NAME) = UPPER($tableName)
                    """
                break
        }
        def firstRow = context.sql.firstRow(query)
        return firstRow != null
    }

    def boolean hasIndexOnTable(String tableName, String indexName) {
        context.databaseHelper.hasIndexOnTable(tableName, indexName)
    }

    def boolean hasColumnOnTable(String tableName, String columnName) {
        context.databaseHelper.hasColumnOnTable(tableName, columnName)
    }

    def boolean hasForeignKeyOnTable(String tableName, String foreignKey) {
        context.databaseHelper.hasForeignKeyOnTable(tableName, foreignKey)
    }

    def boolean hasPrimaryKeyOnTable(String tableName, String pkName) {
        context.databaseHelper.hasPrimaryKeyOnTable(tableName, pkName)
    }

    def boolean hasUniqueKeyOnTable(String tableName, String ukName) {
        context.databaseHelper.hasUniqueKeyOnTable(tableName, ukName)
    }

    def String getPrimaryKey(String tableName) {
        context.databaseHelper.getPrimaryKey(tableName)
    }

    def IndexDefinition getIndexDefinition(String tableName, String indexName) {
        context.databaseHelper.getIndexDefinition(tableName, indexName)
    }


    def hasSequenceForTenant(def tenantId, def sequenceId) {
        def value = context.databaseHelper.getSequenceValue(tenantId, sequenceId)
        value && value.nextid > 0
    }

    def getBlobContentAsString(Object blobValue) {
        context.databaseHelper.getBlobContentAsString(blobValue)
    }

    def JdbcDatabaseTester createTester() {
        new JdbcDatabaseTester(context.dbDriverClassName, context.dburl, context.dbUser, context.dbPassword) {
            public IDatabaseConnection getConnection() {
                if (context.dbVendor == MigrationStep.DBVendor.ORACLE) {
                    def conn = DriverManager.getConnection(context.dburl, context.dbUser, context.dbPassword);
                    return new OracleConnection(conn, context.dbUser);
                } else {
                    return super.getConnection();
                }
            }
        }
    }

    def dropTables(String[] tables) {
        tables.each {
            //add .toString to avoid the error bellow. Is there a better way to do that?
            //Failed to execute: DROP TABLE ? because: ERROR: syntax error at or near "$1"
            if (hasTable(it)) {
                def statement = "DROP TABLE $it".toString()
                println("DROP TABLE [$it]".toString())
                try {
                    context.sql.execute(statement)
                } catch (SQLException e) {
                    System.err.println(String.format("error while executing %s", statement))
                    throw e
                }
            } else {
                println("table [$it] does not exists")
            }
        }
    }

    def hasPropertyInConfigFile(String configFile, String expectedKey, String expectedValue) {
        def scannedFiles = 0
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${configFile}       
                ORDER BY content_type, tenant_id 
                """) {
            scannedFiles++
            def tenantId = it.tenant_id as long
            def contentType = it.content_type as String
            def properties = new Properties();
            String content = getBlobContentAsString(it.resource_content)
            context.logger.debug(String.format("check that property file | tenant id: %3d | type: %-25s | file name: %s | contains %s = %s",
                    tenantId, contentType, configFile, expectedKey, expectedValue))
            StringReader reader=new StringReader(content)
            properties.load(reader)
            assert (properties.containsKey(expectedKey))
            assert (properties.get(expectedKey) == expectedValue)

        }
        assert (scannedFiles > 0)
        true
    }

    def countConfigFileWithContent(String configFile, String expectedContent) {
        def scannedFiles = 0
        context.sql.eachRow("""
                SELECT tenant_id, content_type, resource_content
                FROM configuration
                WHERE resource_name=${configFile}       
                ORDER BY content_type, tenant_id 
                """) {
            scannedFiles++
            def tenantId = it.tenant_id as long
            def contentType = it.content_type as String
            String content = getBlobContentAsString(it.resource_content)
            context.logger.debug(String.format("check configuration file content | tenant id: %3d | type: %-25s " +
                    "| file name: %s ",
                    tenantId, contentType, configFile))
            assert (content == expectedContent)
        }
        scannedFiles
    }

}
