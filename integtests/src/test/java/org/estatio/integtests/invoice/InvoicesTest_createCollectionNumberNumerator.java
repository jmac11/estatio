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
package org.estatio.integtests.invoice;

import java.math.BigInteger;
import javax.inject.Inject;
import org.estatio.dom.invoice.Constants;
import org.estatio.dom.invoice.Invoices;
import org.estatio.dom.numerator.Numerator;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertiesAndUnitsForAll;
import org.estatio.fixture.invoice.InvoicesAndInvoiceItemsForAll;
import org.estatio.fixture.lease.LeasesEtcForAll;
import org.estatio.fixture.party.PersonsAndOrganisationsAndCommunicationChannelsForAll;
import org.estatio.integtests.EstatioIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.fixturescripts.CompositeFixtureScript;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class InvoicesTest_createCollectionNumberNumerator extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        scenarioExecution().install(new CompositeFixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                execute(new EstatioBaseLineFixture(), executionContext);
                execute("parties", new PersonsAndOrganisationsAndCommunicationChannelsForAll(), executionContext);
                execute("properties", new PropertiesAndUnitsForAll(), executionContext);
                execute("leases", new LeasesEtcForAll(), executionContext);
                execute("invoices", new InvoicesAndInvoiceItemsForAll(), executionContext);
            }
        });
    }

    @Inject
    private Invoices invoices;

    @Test
    public void createThenFind() throws Exception {
        // when
        Numerator numerator = invoices.createCollectionNumberNumerator("%09d", BigInteger.TEN);
        // then
        assertThat(numerator, is(notNullValue()));
        assertThat(numerator.getName(), is(Constants.COLLECTION_NUMBER_NUMERATOR_NAME));
        assertThat(numerator.getObjectType(), is(nullValue()));
        assertThat(numerator.getObjectIdentifier(), is(nullValue()));
        assertThat(numerator.getLastIncrement(), is(BigInteger.TEN));
    }

    @Test
    public void whenNone() throws Exception {
        Numerator numerator = invoices.findCollectionNumberNumerator();
        assertThat(numerator, is(nullValue()));
    }

}