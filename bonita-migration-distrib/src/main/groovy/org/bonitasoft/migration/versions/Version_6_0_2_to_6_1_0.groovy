/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.migration.versions;

import java.sql.ResultSet

import org.bonitasoft.migration.MigrationUtil



/**
 *
 * Main script to execute the migration
 *
 * @author Baptiste Mesta
 *
 */
public class Version_6_0_2_to_6_1_0 {
    private def File bonitaHome
    private def dbVendor

    /**
     * migration with specific behaviors. Instead of executing the sql, the script will call the methods having the same name
     */
    private def specificMigrations =  [
        "platform",
        "profile",
        "document"
    ]

    public static void main(String[] args) {
        new Version_6_0_2_to_6_1_0().execute(args);
    }

    public execute(String[] args){
        def migrationUtil = new MigrationUtil()
        def Map props = migrationUtil.parseOrAskArgs(args);
        dbVendor = props.get(MigrationUtil.DB_VENDOR);
        bonitaHome = new File(props.get(MigrationUtil.BONITA_HOME));
        if(!bonitaHome.exists()){
            throw new IllegalStateException("bonita home does not exists");
        }
        def sql = migrationUtil.getSqlConnection(props);
        def resources = new File("versions/"+this.getClass().getSimpleName())
        migrateFeatures(resources, dbVendor, sql)
        sql.close()
    }

    public migrateFeatures(File resources, String dbVendor, groovy.sql.Sql sql) {
        def features = [];
        def files = resources.listFiles();
        Arrays.sort(files);
        files.each {
            if(it.isDirectory())
                features.add(it.getAbsoluteFile());
        }
        features.eachWithIndex { file,idx->
            def feature = file.getName().substring(4);
            println "Migrating <"+feature+"> "+(idx+1)+"/"+features.size();
            if(feature in specificMigrations){
                "$feature"(file, dbVendor, sql);
            }else{
                executeDefaultSqlFile(file, dbVendor, sql);
            }
        }
    }

    public executeDefaultSqlFile(File file, String dbVendor, groovy.sql.Sql sql){
        def sqlFile = new File(file,dbVendor+".sql")
        if(sqlFile.exists()){
            def content = sqlFile.text;
            println sql.executeUpdate(content) + " row(s) updated";
        }else{
            println "nothing to execute"
        }
    }

    public platform(File feature, String dbVendor, groovy.sql.Sql sql){
        def content = new File(feature,dbVendor+".sql").text;
        content = content.replaceAll(":version", "6.1.0");
        println sql.executeUpdate(content) + " row(s) updated";
    }

    public profile(File feature, String dbVendor, groovy.sql.Sql sql){
        def content = new File(feature,dbVendor+".sql").text;
        def currentTime = System.currentTimeMillis();
        //        println sql.executeUpdate(content,currentTime,currentTime) + " row(s) updated";
        def tenants = []

        sql.query(new File(feature,dbVendor+"-tenants.sql").text){ ResultSet rs ->
            while (rs.next()) tenants.add(rs.getLong(1));
        }
        println "executing update for each tenants: "+tenants
        tenants.each {
            println "for tenant id="+it;
            //there is profile and profile entries needed
            def adminId = null;
            def directoryId = null;
            sql.eachRow(new File(feature,dbVendor+"-get_admin_profile_id.sql").text.replaceAll(":tenantId", String.valueOf(it))) { row ->
                adminId = row[0]
            }
            sql.eachRow(new File(feature,dbVendor+"-get_dir_profile_entry_id.sql").text.replaceAll(":tenantId", String.valueOf(it))) { row ->
                directoryId = row[0]
            }
            if(adminId != null && directoryId != null){
                sql.execute(new File(feature,dbVendor+"-update.sql").text
                        .replaceAll(":tenantId", String.valueOf(it))
                        .replaceAll(":admin_profile_id", String.valueOf(adminId))
                        .replaceAll(":dir_profile_entry_id", String.valueOf(directoryId)));
            }
            println "done";
        }
    }

    public document(File feature, String dbVendor, groovy.sql.Sql sql){
        executeDefaultSqlFile(feature, dbVendor, sql);
        //get the path from bonita home (default = bonita.home/platform/work
        //use the default one for now
        //for each row in document_mapping get the corresponding file content
        def contentRoot = new File(new File(new File(bonitaHome,"server"),"platform"),"work")
        int id = 1;
        def contents = [];
        def migrateContentFor = { table, contentIndex ->
            sql.eachRow("SELECT * FROM "+table) { row ->
                if(row[6]){//document has content
                    def contentId = row[contentIndex];
                    def tenantId = row[0];
                    def File content = new File(contentRoot,contentId);
                    if(!content.exists()){
                        throw new IllegalStateException("content not found "+content.getAbsolutePath());
                    }
                    sql.executeInsert("INSERT INTO document_content (tenantid, id, documentId, content ) VALUES (?,?,?,?)",tenantId,id,contentId,content.getBytes())
                    id++;
                    contents.add(content)
                }
            }
        }
        //execute for live document mappings
        migrateContentFor("document_mapping",9);
        //execute for archived document mappings
        migrateContentFor("arch_document_mapping",10);
        //deleting files
        contents.each { it.delete(); }
        println "migrated "+contents.size()+" documents from file system to database"
    }

}