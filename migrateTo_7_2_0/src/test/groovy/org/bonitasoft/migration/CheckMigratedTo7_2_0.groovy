/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.migration

import org.bonitasoft.engine.LocalServerTestsInitializer
import org.bonitasoft.engine.api.PlatformAPIAccessor
import org.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.bpm.contract.Type
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.MultiInstanceLoopCharacteristics
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition
import org.bonitasoft.engine.test.PlatformTestUtil
import org.bonitasoft.migration.filler.FillerUtils
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

/**
 * @author Laurent Leseigneur
 */
class CheckMigratedTo7_2_0 {

    @BeforeClass
    public static void beforeClass() {
        FillerUtils.initializeEngineSystemProperties()
        startNode()
    }

    private static void startNode() {
        LocalServerTestsInitializer.instance.prepareEnvironment()
        def platformTestUtil = new PlatformTestUtil()
        def platform = platformTestUtil.loginOnPlatform()
        def platformApi = platformTestUtil.getPlatformAPI(platform)
        platformApi.startNode()
        platformTestUtil.logoutOnPlatform(platform)
    }

    @Test
    def void checkProcessStillWorks() {
        def session = TenantAPIAccessor.getLoginAPI().login("install", "install")
        def processAPI = TenantAPIAccessor.getProcessAPI(session)
        def id = processAPI.getProcessDefinitionId("MyProcess to be migrated", "1.0-SNAPSHOT")


        def definition = processAPI.getDesignProcessDefinition(id)

        assert definition.getName() == "MyProcess to be migrated"
        assert definition.getVersion() == "1.0-SNAPSHOT"
        assert definition.description == "2-lines\ndescription"
        assert definition.displayDescription == "2-lines\ndisplay description"


        def dataDefinition = definition.getFlowElementContainer().getDataDefinitions().get(0)
        assert dataDefinition.name == "myData"
        assert dataDefinition.description == "my data description"

        def step1 = definition.getFlowElementContainer().getActivity("step1")
        def step2 = definition.getFlowElementContainer().getActivity("step2")

        assert step1 != null
        assert step1.getOutgoingTransitions().size() == 1
        assert step1.getOutgoingTransitions().get(0).getTarget() == step2.getId()
        assert step1.description == "autoTaskDesc"

        assert step2 != null
        assert step2.getIncomingTransitions().size() == 1
        assert step2.getIncomingTransitions().get(0).getSource() == step1.getId()
        assert ((UserTaskDefinition) step2).getContract().getInputs().get(0).description == "Serves description non-reg purposes"

        // Check that we do not add a sub-element 'description' if there was no attribute 'description':
        def autoTask3 = definition.getFlowElementContainer().getActivity("taskWithNoDescription")
        assert autoTask3.description == null
        assert autoTask3.getConnectors().size() == 1
        assert autoTask3.getConnectors().get(0).getName() == "theConnector"
        assert autoTask3.getConnectors().get(0).getInputs().size() == 1
        assert autoTask3.getConnectors().get(0).getInputs().get("input1").getName() == "input1Value"
        assert autoTask3.getConnectors().get(0).getOutputs().size() == 1
        assert autoTask3.getOperations().size() == 1
        assert autoTask3.getOperations().get(0).getLeftOperand().name == "myData"

        assert definition.getFlowElementContainer().getTransitions().size() == 1
        assert definition.getFlowElementContainer().getTransitions().iterator().next().getSource() == step1.getId()
        assert definition.getFlowElementContainer().getTransitions().iterator().next().getTarget() == step2.getId()

        assert definition.getActorInitiator().getName() == "myActorInitiator"
        assert definition.getActorsList().size() == 2
        assert definition.getActorsList().get(0).getName() == "myActor"
        assert definition.getActorsList().get(1).getName() == "myActorInitiator"

        assert definition.getFlowElementContainer().getDataDefinition("xmlData") != null
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").class.name.contains("XMLDataDefinition")
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").getDefaultValueExpression().getContent() == "'<tag>'+isOk+'</tag>'"
        assert definition.getFlowElementContainer().getDataDefinition("xmlData").getDefaultValueExpression().getDependencies().size() == 1


        def step3 = definition.getFlowElementContainer().getActivity("step3")
        assert step3.loopCharacteristics instanceof MultiInstanceLoopCharacteristics
        assert !(step3.loopCharacteristics as MultiInstanceLoopCharacteristics).sequential
        assert (step3.loopCharacteristics as MultiInstanceLoopCharacteristics).loopCardinality.content == "12"

        assert definition.contract.inputs.size() == 2
        assert definition.contract.inputs[0].multiple
        assert definition.contract.inputs[0].type == Type.BOOLEAN
        assert definition.contract.inputs[0].name == "isOk"
        assert definition.contract.inputs[0].description == "the is ok contract input"
        assert definition.contract.inputs[1].hasChildren()
        assert definition.contract.inputs[1].inputs[0].name == "name"
        assert definition.contract.inputs[1].inputs[1].name == "value"
        assert definition.contract.inputs[1].inputs[1].type == Type.INTEGER



        def processInstance = processAPI.startProcessWithInputs(id, [isOk: [true, false, true], request: [name: "myRequest", value: 123]])

        def instances
        def timeout = System.currentTimeMillis() + 15000
        while ((instances = processAPI.getOpenActivityInstances(processInstance.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT)).size() < 1 && System.currentTimeMillis() < timeout ){
            Thread.sleep(200)
            println "wait 200"
        }
        assert instances.size() == 1
        assert instances.get(0).getName() == "step2"


        TenantAPIAccessor.getLoginAPI().logout(session)
    }

    @AfterClass
    public static void afterClass() {
        new PlatformTestUtil().stopPlatformAndTenant(PlatformAPIAccessor.getPlatformAPI(new PlatformTestUtil().loginOnPlatform()), true)
    }

}