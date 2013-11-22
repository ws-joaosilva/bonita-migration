import org.bonitasoft.migration.core.MigrationUtil;

def currentDir = ""

def File newServerBonitaHome = new File(feature, "bonita/server/");
def File oldServerBonitaHome = new File(bonitaHome, "/server/");

currentDir = "/platform/conf"
MigrationUtil.migrateDirectory(newServerBonitaHome.path + currentDir, oldServerBonitaHome.path + currentDir)

currentDir = "/platform/tenant-template"
MigrationUtil.migrateDirectory(newServerBonitaHome.path + currentDir, oldServerBonitaHome.path + currentDir)

println "Detecting tenants..."
def tenantsServerDir = new File(oldServerBonitaHome, "/tenants")
if (tenantsServerDir.exists()) {
    def tenants = Arrays.asList(tenantsServerDir.listFiles());
    if (tenants.empty){
        println "Not found any tenants."
    } else {
        println "Executing update for each tenant : " + tenants;
        tenantsServerDir.eachFile { tenant ->
            println "For tenant : " + tenant.name
            PrintStream stdout = MigrationUtil.setSystemOutWithTab(5);
            
            MigrationUtil.migrateDirectory(newServerBonitaHome.path + "/platform/tenant-template/conf", tenant.path + "/conf")
            System.setOut(stdout);
        }
    }
} else {
    println "Not found any tenants."
}