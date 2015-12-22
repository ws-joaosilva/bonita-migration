package org.bonitasoft.migration.version.to7_2_0

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Elias Ricken de Medeiros
 */
class MigrateTo7_2_0Test extends Specification {

    @Unroll
    def "should contains #step"(def index, def step) {
        setup:
        def migration = new MigrateTo7_2_0();

        expect:
        migration.getMigrationSteps().get(index).getClass() == step

        where:
        index || step
        0     || MigrateProcessDefXml
        1     || ParametersInDatabase
        2     || CheckBDMQueries
        3     || IncreasePageNameField

    }

}
