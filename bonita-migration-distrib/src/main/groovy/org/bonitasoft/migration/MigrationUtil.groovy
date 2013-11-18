package org.bonitasoft.migration

import groovy.sql.Sql

import java.io.File
import java.sql.ResultSet


public class MigrationUtil {


    public static String DB_URL = "db.url"

    public static String DB_USER = "db.user"

    public static String DB_PASSWORD = "db.password"

    public static String DB_DRIVERCLASS = "db.driverclass"

    public static String DB_VENDOR = "db.vendor"

    public static String BONITA_HOME = "bonita.home"

    public static String REQUEST_SEPARATOR = "@@"


    public static Map parseOrAskArgs(String[] args){
        //will ask for missing parameter
        return listToMap(args)
    }

    public static Map listToMap(String[] list){
        def map = [:]
        def iterator = list.iterator()
        while (iterator.hasNext()) {
            map.put(iterator.next().substring(2),iterator.next())
        }
        return map
    }

    public static Sql getSqlConnection(Map props){
        def dburl = props.get(MigrationUtil.DB_URL)
        def user = props.get(MigrationUtil.DB_USER)
        def pass = props.get(MigrationUtil.DB_PASSWORD)
        def driver = props.get(MigrationUtil.DB_DRIVERCLASS)
        println "url=" + dburl
        println "user=" + user
        println "pass=" + pass
        println "driver=" + driver
        return Sql.newInstance(dburl, user, pass, driver)
    }

    public static executeDefaultSqlFile(File file, String dbVendor, groovy.sql.Sql sql){
        executeSqlFile(file, dbVendor, null, null, sql, true)
    }

    public static executeSqlFile(File file, String dbVendor, String suffix, Map<String, String> parameters, groovy.sql.Sql sql, boolean toUpdate){
        def sqlFile = getSqlFile(file, dbVendor, suffix)
        sql.withTransaction {
            if(sqlFile.exists()){
                def contents = getSqlContent(sqlFile.text, parameters)

                for (content in contents) {
                    if (!content.trim().empty) {
                        if (toUpdate) {
                            println sql.executeUpdate(content) + " row(s) updated"
                        } else {
                            sql.execute(content)
                        }
                    }
                }
            } else{
                println "nothing to execute"
            }
        }
    }

    public static String[] getSqlContent(String sqlFileContent, Map<String, String> parameters){
        def sqlFileContentWithParameters = replaceParameters(sqlFileContent, parameters).replaceAll("\r\n", "\n")
        return sqlFileContentWithParameters.split(REQUEST_SEPARATOR)
    }

    public static File getSqlFile(File folder, String dbVendor, String suffix){
        return new File(folder, dbVendor + (suffix == null || suffix.isEmpty() ? "" : "-" + suffix) + ".sql")
    }

    static String replaceParameters(String sqlFileContent, Map<String, String> parameters){
        String newSqlFileContent = sqlFileContent
        if (parameters != null) {
            for (parameter in parameters) {
                newSqlFileContent = newSqlFileContent.replaceAll(parameter.key, parameter.value)
            }
        }
        return newSqlFileContent
    }

    public static Object getId(File feature, String dbVendor, String fileExtension, Object it, groovy.sql.Sql sql){
        def id = null
        sql.eachRow(getSqlContent(feature, dbVendor, fileExtension)
                .replaceAll(":tenantId", String.valueOf(it))) { row ->
                    id = row[0]
                }
        return id
    }

    public static List<Object> getTenantsId(File feature, String dbVendor, groovy.sql.Sql sql){
        def tenants = []

        sql.query(getSqlContent(feature, dbVendor, "tenants")) { ResultSet rs ->
            while (rs.next()) tenants.add(rs.getLong(1))
        }
        return tenants
    }
}
