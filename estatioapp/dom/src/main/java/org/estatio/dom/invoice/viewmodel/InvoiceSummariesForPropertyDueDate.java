/*
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
package org.estatio.dom.invoice.viewmodel;

import java.util.List;
import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.estatio.dom.UdoDomainRepositoryAndFactory;

@DomainService
@DomainServiceLayout(
        named="Invoices",
        menuBar = DomainServiceLayout.MenuBar.PRIMARY,
        menuOrder = "50.2"
)
@Immutable
public class InvoiceSummariesForPropertyDueDate extends UdoDomainRepositoryAndFactory<InvoiceSummaryForPropertyDueDate> {

    public InvoiceSummariesForPropertyDueDate() {
        super(InvoiceSummariesForPropertyDueDate.class, InvoiceSummaryForPropertyDueDate.class);
    }

    // //////////////////////////////////////

    @ActionLayout(
            prototype = true
    )
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "90")
    public List<InvoiceSummaryForPropertyDueDate> allInvoicesByPropertyDueDate() {
        return allInstances();
    }

}
