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
import org.estatio.dom.invoice.InvoiceStatus;

@DomainService
@DomainServiceLayout(
        named="Invoices",
        menuBar = DomainServiceLayout.MenuBar.PRIMARY,
        menuOrder = "50.3"
)
@Immutable
public class InvoiceSummariesForPropertyDueDateStatus
        extends UdoDomainRepositoryAndFactory<InvoiceSummaryForPropertyDueDateStatus> {

    public InvoiceSummariesForPropertyDueDateStatus() {
        super(InvoiceSummariesForPropertyDueDateStatus.class, InvoiceSummaryForPropertyDueDateStatus.class);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<InvoiceSummaryForPropertyDueDateStatus> allNewInvoices() {
        return findInvoicesByStatus(InvoiceStatus.NEW);
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "2")
    public List<InvoiceSummaryForPropertyDueDateStatus> allApprovedInvoices() {
        return findInvoicesByStatus(InvoiceStatus.APPROVED);
    }

    @ActionLayout(
            prototype = true
    )
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "99")
    public List<InvoiceSummaryForPropertyDueDateStatus> allInvoicesByPropertyDueDateStatus() {
        return allInstances();
    }
    
    // //////////////////////////////////////

    @Programmatic
    public List<InvoiceSummaryForPropertyDueDateStatus> findInvoicesByStatus(
            final @Optional InvoiceStatus status) {
        return allMatches("findByStatus",
                "status", status);
    }
}
