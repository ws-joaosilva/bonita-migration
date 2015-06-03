/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.migration.versions.to_7_0_0

import org.bonitasoft.migration.core.Reporter
import spock.lang.Specification
/**
 * @author Elias Ricken de Medeiros
 */
class ApplicationTokenCheckerTest extends Specification {

    def reporter = Mock(Reporter)
    def appMessageBuilder = Mock(ApplicationMessageBuilder)
    def appPageMessageBuilder = Mock(ApplicationPageMessageBuilder)

    private ApplicationTokenChecker checker = new ApplicationTokenChecker(reporter, appMessageBuilder, appPageMessageBuilder)

    def "process invalid tokens should add invalid tokens to reporter list"() {
        appMessageBuilder.buildMessage() >> {"invalid apps"}
        appPageMessageBuilder.buildMessage() >> {"invalid app pages"}

        when:
        checker.processInvalidTokens()

        then:
        1 * reporter.addWarning("invalid apps")
        1 * reporter.addWarning("invalid app pages")
    }

    def "does not add warning when message is empty"() {
        appMessageBuilder.buildMessage() >> {""}
        appPageMessageBuilder.buildMessage() >> {""}

        when:
        checker.processInvalidTokens()

        then:
        0 * reporter.addWarning(_ as String)

    }

    def "does not add warning when message is null"() {
        appMessageBuilder.buildMessage() >> {null}
        appPageMessageBuilder.buildMessage() >> {null}

        when:
        checker.processInvalidTokens()

        then:
        0 * reporter.addWarning(_ as String)

    }
}
