/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.integtests.assets;

import java.util.Set;
import javax.inject.Inject;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancies;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.estatio.dom.asset.Properties;
import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.Unit;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForOxf;
import org.estatio.fixture.security.tenancy.ApplicationTenancyForHelloWorld;
import org.estatio.fixture.security.tenancy.ApplicationTenancyRoot;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PropertyTest extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        runScript(new FixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());

                executionContext.executeChild(this, new PropertyForOxf());
            }
        });
    }

    @Inject
    Properties properties;


    public static class GetUnits extends PropertyTest {

        @Test
        public void whenReturnsInstance_thenCanTraverseUnits() throws Exception {
            // given
            Property property = properties.findPropertyByReference(PropertyForOxf.PROPERTY_REFERENCE);

            // when
            Set<Unit> units = property.getUnits();

            // then
            assertThat(units.size(), is(25));
        }
    }


    public static class MoveDownApplicationTenancy extends PropertyTest {

        private ApplicationTenancy rootApplicationTenancy;
        private ApplicationTenancy helloWorldApplicationTenancy;

        @Inject
        ApplicationTenancies applicationTenancies;

        @Before
        public void setup() {
            rootApplicationTenancy = applicationTenancies.findTenancyByPath(ApplicationTenancyRoot.PATH);
            helloWorldApplicationTenancy = applicationTenancies.findTenancyByPath(ApplicationTenancyForHelloWorld.PATH);
        }

        @Test
        public void updatesUnits() throws Exception {

            // given
            Property property = properties.findPropertyByReference(PropertyForOxf.PROPERTY_REFERENCE);

            assertPropertyAndUnits_hasApplicationTenancy(property, rootApplicationTenancy);

            // when
            wrap(property).moveDownApplicationTenancy(helloWorldApplicationTenancy);

            // then
            assertPropertyAndUnits_hasApplicationTenancy(property, helloWorldApplicationTenancy);
        }

        private void assertPropertyAndUnits_hasApplicationTenancy(final Property property, final ApplicationTenancy tenancy) {
            assertThat(property.getApplicationTenancy(), is(tenancy));
            for (Unit unit : property.getUnits()) {
                assertThat(unit.getApplicationTenancy(), is(tenancy));
            }
        }
    }


}