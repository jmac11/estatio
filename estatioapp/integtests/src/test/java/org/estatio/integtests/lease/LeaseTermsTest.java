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
package org.estatio.integtests.lease;

import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.estatio.dom.lease.LeaseTerm;
import org.estatio.dom.lease.LeaseTermForIndexable;
import org.estatio.dom.lease.LeaseTerms;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.lease.LeaseItemAndTermsForOxfTopModel001;
import org.estatio.integtests.EstatioIntegrationTest;
import org.estatio.integtests.VT;

import static org.hamcrest.CoreMatchers.is;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LeaseTermsTest extends EstatioIntegrationTest {

    public static class AllLeaseTerms extends LeaseTermsTest {

        @Before
        public void setupData() {
            runScript(new FixtureScript() {
                @Override
                protected void execute(ExecutionContext executionContext) {
                    executionContext.executeChild(this, new EstatioBaseLineFixture());

                    executionContext.executeChild(this, new LeaseItemAndTermsForOxfTopModel001());
                }
            });
        }

        @Inject
        private LeaseTerms leaseTerms;

        @Test
        public void whenExists() throws Exception {

            // when
            List<LeaseTerm> allLeaseTerms = leaseTerms.allLeaseTerms();

            // then
            Assert.assertThat(allLeaseTerms.isEmpty(), is(false));
            LeaseTerm term = allLeaseTerms.get(0);

            // and then
            Assert.assertNotNull(term.getFrequency());
            Assert.assertNotNull(term.getFrequency().nextDate(VT.ld(2012, 1, 1)));

            final LeaseTermForIndexable indexableRent = assertType(term, LeaseTermForIndexable.class);
            BigDecimal baseValue = indexableRent.getBaseValue();
            Assert.assertEquals(VT.bd("20000.00"), baseValue);
        }

    }
}