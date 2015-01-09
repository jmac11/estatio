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
package org.estatio.dom.lease;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.LocalDate;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionInteraction;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Paged;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;

import org.estatio.dom.UdoDomainObject;
import org.estatio.dom.JdoColumnLength;
import org.estatio.dom.WithIntervalMutable;
import org.estatio.dom.WithSequence;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.Charges;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.lease.invoicing.InvoiceCalculationService.CalculationResult;
import org.estatio.dom.tax.Tax;
import org.estatio.dom.valuetypes.LocalDateInterval;

/**
 * An item component of an {@link #getLease() owning} {@link Lease}. Each is of
 * a {@link #getType() particular} {@link LeaseItemType}; Estatio currently
 * defines three such: {@link LeaseItemType#RENT (indexable) rent},
 * {@link LeaseItemType#TURNOVER_RENT turnover rent} and
 * {@link LeaseItemType#SERVICE_CHARGE service charge}
 * 
 * <p>
 * Each item gives rise to a succession of {@link LeaseTerm}s, typically
 * generated on a quarterly basis. The lease terms (by implementing
 * <tt>InvoiceSource</tt>) act as the source of <tt>InvoiceItem</tt>s.
 */
@javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.DATASTORE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Indices({
        @javax.jdo.annotations.Index(
                name = "LeaseItem_lease_type_sequence_IDX",
                members = { "lease", "type", "sequence" }),
        @javax.jdo.annotations.Index(
                name = "LeaseItem_lease_type_startDate_IDX",
                members = { "lease", "type", "startDate" })
})
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByLeaseAndTypeAndStartDate",
                language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.lease.LeaseItem "
                        + "WHERE lease == :lease "
                        + "   && type == :type "
                        + "   && startDate == :startDate"),
        @javax.jdo.annotations.Query(
                name = "findByLeaseAndTypeAndStartDateAndSequence",
                language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.lease.LeaseItem "
                        + "WHERE lease == :lease "
                        + "&& type == :type "
                        + "&& startDate == :startDate "
                        + "&& sequence == :sequence"),
        @javax.jdo.annotations.Query(
                name = "findByLeaseAndType",
                language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.dom.lease.LeaseItem "
                        + "WHERE lease == :lease "
                        + "&& type == :type "
                        + "ORDER BY sequence ")
})
@Unique(name = "LeaseItem_lease_type_startDate_sequence_IDX", members = { "lease", "type", "startDate", "sequence" })
@Bookmarkable(BookmarkPolicy.AS_CHILD)
@Immutable
public class LeaseItem
        extends UdoDomainObject<LeaseItem>
        implements WithIntervalMutable<LeaseItem>, WithSequence {

    private static final int PAGE_SIZE = 15;

    public static class Functions {
        public static Function<LeaseItem, LeaseItemStatus> GET_STATUS = new Function<LeaseItem, LeaseItemStatus>() {
            public LeaseItemStatus apply(final LeaseItem li) {
                return li.getStatus();
            }
        };
    }

    public LeaseItem() {
        super("lease, type, sequence");
    }

    // //////////////////////////////////////

    private LeaseItemStatus status;

    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.STATUS_ENUM)
    public LeaseItemStatus getStatus() {
        return status;
    }

    public void setStatus(final LeaseItemStatus status) {
        this.status = status;
    }

    // //////////////////////////////////////

    @ActionInteraction(LeaseItem.SuspendEvent.class)
    public LeaseItem suspend(final @Named("Reason") String reason) {
        setStatus(LeaseItemStatus.SUSPENDED);
        return this;
    }

    public boolean hideSuspend() {
        return getStatus().equals(LeaseItemStatus.SUSPENDED);
    }

    @ActionInteraction(LeaseItem.ResumeEvent.class)
    public LeaseItem resume(final @Named("Reason") String reason) {
        return this;
    }

    public boolean hideResume() {
        return !getStatus().equals(LeaseItemStatus.SUSPENDED);
    }

    @Programmatic
    public void doResume() {
        this.setStatus(LeaseItemStatus.UNKOWN);
    }

    // //////////////////////////////////////

    public Object remove(@Named("Are you sure?") Boolean confirm) {
        Lease tmpLease = getLease();
        if (confirm && doRemove()) {
            return tmpLease;
        }
        return this;
    }

    @Programmatic
    public boolean doRemove() {
        boolean canDelete = true;
        if (!getTerms().isEmpty()) {
            canDelete = getTerms().first().doRemove();
        }
        if (canDelete) {
            getContainer().remove(this);
            getContainer().flush();
        }
        return canDelete;
    }

    // //////////////////////////////////////

    private Lease lease;

    @javax.jdo.annotations.Column(name = "leaseId", allowsNull = "false")
    @Hidden(where = Where.PARENTED_TABLES)
    @Title(sequence = "1", append = ":")
    public Lease getLease() {
        return lease;
    }

    public void setLease(final Lease lease) {
        this.lease = lease;
    }

    // //////////////////////////////////////

    private Tax tax;

    @Hidden(where = Where.ALL_TABLES)
    @DescribedAs("When left empty the tax of the charge will be used")
    @javax.jdo.annotations.Column(name = "taxId", allowsNull = "true")
    public Tax getTax() {
        return tax;
    }

    public void setTax(final Tax tax) {
        this.tax = tax;
    }

    @Programmatic
    public Tax getEffectiveTax() {
        return getTax() == null && getCharge() != null ? getCharge().getTax() : getTax();
    }

    // //////////////////////////////////////

    private BigInteger sequence;

    @javax.jdo.annotations.Column(allowsNull = "false")
    @Hidden
    @Override
    public BigInteger getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(final BigInteger sequence) {
        this.sequence = sequence;
    }

    @Programmatic
    public LeaseTerm findTermWithSequence(final BigInteger sequence) {
        return leaseTerms.findByLeaseItemAndSequence(this, sequence);
    }

    // //////////////////////////////////////

    private LeaseItemType type;

    @javax.jdo.annotations.Persistent(defaultFetchGroup = "true")
    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.TYPE_ENUM)
    @Title(sequence = "2")
    public LeaseItemType getType() {
        return type;
    }

    public void setType(final LeaseItemType type) {
        this.type = type;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent
    private LocalDate startDate;

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    @javax.jdo.annotations.Persistent
    private LocalDate endDate;

    @Optional
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    // //////////////////////////////////////

    private WithIntervalMutable.Helper<LeaseItem> changeDates = new WithIntervalMutable.Helper<LeaseItem>(this);

    WithIntervalMutable.Helper<LeaseItem> getChangeDates() {
        return changeDates;
    }

    @ActionSemantics(Of.IDEMPOTENT)
    @Override
    public LeaseItem changeDates(
            final @Named("Start Date") @Optional LocalDate startDate,
            final @Named("End Date") @Optional LocalDate endDate) {
        return getChangeDates().changeDates(startDate, endDate);
    }

    public String disableChangeDates(
            final LocalDate startDate,
            final LocalDate endDate) {
        return null;
    }

    @Override
    public LocalDate default0ChangeDates() {
        return getChangeDates().default0ChangeDates();
    }

    @Override
    public LocalDate default1ChangeDates() {
        return getChangeDates().default1ChangeDates();
    }

    @Override
    public String validateChangeDates(
            final LocalDate startDate,
            final LocalDate endDate) {
        return getChangeDates().validateChangeDates(startDate, endDate);
    }

    public LeaseItem copy(
            final @Named("Start date") LocalDate startDate,
            final InvoicingFrequency invoicingFrequency,
            final PaymentMethod paymentMethod,
            final Charge charge
            ) {
        LeaseItem newItem = getLease().newItem(this.getType(), charge, invoicingFrequency, paymentMethod, startDate);
        this.copyTerms(startDate, newItem);
        this.changeDates(getStartDate(), newItem.getInterval().endDateFromStartDate());
        return newItem;
    }

    public LeaseItem terminate(
            final @Named("End date") LocalDate endDate) {
        this.changeDates(getStartDate(), endDate);
        return this;
    }

    public LocalDate default0Terminate() {
        return getLease().getInterval().endDateExcluding();
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public LocalDateInterval getInterval() {
        return LocalDateInterval.including(getStartDate(), getEndDate());
    }

    @Programmatic
    @Override
    public LocalDateInterval getEffectiveInterval() {
        return getInterval().overlap(getLease().getEffectiveInterval());
    }

    // //////////////////////////////////////

    public boolean isCurrent() {
        return isActiveOn(getClockService().now());
    }

    private boolean isActiveOn(final LocalDate localDate) {
        return getEffectiveInterval().contains(localDate);
    }

    // //////////////////////////////////////

    private InvoicingFrequency invoicingFrequency;

    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.INVOICING_FREQUENCY_ENUM)
    @Hidden(where = Where.PARENTED_TABLES)
    public InvoicingFrequency getInvoicingFrequency() {
        return invoicingFrequency;
    }

    public void setInvoicingFrequency(final InvoicingFrequency invoicingFrequency) {
        this.invoicingFrequency = invoicingFrequency;
    }

    // //////////////////////////////////////

    private PaymentMethod paymentMethod;

    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.PAYMENT_METHOD_ENUM)
    @Hidden(where = Where.PARENTED_TABLES)
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // //////////////////////////////////////

    private Charge charge;

    @javax.jdo.annotations.Column(name = "chargeId", allowsNull = "false")
    public Charge getCharge() {
        return charge;
    }

    public void setCharge(final Charge charge) {
        this.charge = charge;
    }

    public List<Charge> choicesCharge() {
        return charges.allCharges();
    }

    public LeaseItem changeCharge(final Charge charge) {
        setCharge(charge);
        return this;
    }

    public Charge default0ChangeCharge() {
        return getCharge();
    }

    // //////////////////////////////////////

    public LeaseItem changePaymentMethod(
            final PaymentMethod paymentMethod,
            final @Named("Reason") String reason) {
        setPaymentMethod(paymentMethod);
        return this;
    }

    public PaymentMethod default0ChangePaymentMethod(
            final PaymentMethod paymentMethod,
            final String reason
            ) {
        return getPaymentMethod();
    }

    // //////////////////////////////////////

    public LeaseItem overrideTax(
            final Tax tax,
            final @Named("Reason") String reason) {
        setTax(tax);
        return this;
    }

    public Tax default0OverrideTax(
            final Tax tax,
            final @Named("Reason") String reason) {
        return getTax();
    }

    public boolean hideOverrideTax() {
        return getTax() != null;
    }

    // //////////////////////////////////////

    public LeaseItem cancelOverrideTax(
            final @Named("Reason") String reason) {
        setTax(null);
        return this;
    }

    public boolean hideCancelOverrideTax() {
        return getTax() == null;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent
    private LocalDate nextDueDate;

    @Optional
    @Hidden(where = Where.PARENTED_TABLES)
    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(final LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent
    private LocalDate epochDate;

    @Optional
    @Hidden
    public LocalDate getEpochDate() {
        return epochDate;
    }

    public void setEpochDate(final LocalDate epochDate) {
        this.epochDate = epochDate;
    }

    // //////////////////////////////////////

    @Optional
    public BigDecimal getValue() {
        return valueForDate(getClockService().now());
    }

    @Programmatic
    public BigDecimal valueForDate(final LocalDate date) {
        final LeaseTerm currentTerm = currentTerm(date);
        return currentTerm != null ? currentTerm.valueForDate(date) : null;
    }

    @Programmatic
    public LeaseTerm currentTerm(final LocalDate date) {
        for (LeaseTerm term : getTerms()) {
            if (term.isActiveOn(date)) {
                return term;
            }
        }
        return null;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "leaseItem")
    private SortedSet<LeaseTerm> terms = new TreeSet<LeaseTerm>();

    @Render(Type.EAGERLY)
    @Paged(PAGE_SIZE)
    public SortedSet<LeaseTerm> getTerms() {
        return terms;
    }

    public void setTerms(final SortedSet<LeaseTerm> terms) {
        this.terms = terms;
    }

    @Programmatic
    public LeaseTerm findTerm(final LocalDate startDate) {
        for (LeaseTerm term : getTerms()) {
            if (startDate.equals(term.getStartDate())) {
                return term;
            }
        }
        return null;
    }

    // //////////////////////////////////////

    public LeaseTerm newTerm(
            final @Named("Start date") LocalDate startDate,
            final @Named("End date") @Optional LocalDate endDate) {
        LeaseTerm term;
        if (getType().autoCreateTerms() && !getTerms().isEmpty()) {
            LeaseTerm lastTerm = getTerms().last();
            term = lastTerm.createNext(startDate, endDate);
            lastTerm.align();
        }
        else {

            term = leaseTerms.newLeaseTerm(this, null, startDate, endDate);
        }
        term.initialize();
        term.align();
        return term;
    }

    public LocalDate default0NewTerm(
            final LocalDate startDate,
            final LocalDate endDate) {
        if (getTerms().size() == 0) {
            return getStartDate();
        }
        LeaseTerm last = getTerms().last();
        return last.default0CreateNext(null, null);
    }

    public LocalDate default1NewTerm(
            final LocalDate startDate,
            final LocalDate endDate) {
        if (getTerms().size() == 0) {
            return null;
        }
        return getTerms().last().default1CreateNext(null, null);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.IDEMPOTENT)
    public LeaseItem verify() {
        verifyUntil(ObjectUtils.min(getEffectiveInterval().endDateExcluding(), getClockService().now()));
        return this;
    }

    @ActionSemantics(Of.IDEMPOTENT)
    public LeaseItem verifyUntil(final LocalDate date) {
        if (!getTerms().isEmpty()) {
            getTerms().first().verifyUntil(date);
        }
        return this;
    }

    // //////////////////////////////////////

    @Programmatic
    public void copyTerms(final LocalDate startDate, final LeaseItem newItem) {
        LeaseTerm lastTerm = null;
        for (LeaseTerm term : getTerms()) {
            if (term.getInterval().contains(startDate)) {
                LeaseTerm newTerm;
                if (lastTerm == null) {
                    newTerm = newItem.newTerm(term.getStartDate(), null);
                } else {
                    newTerm = lastTerm.createNext(term.getStartDate(), term.getEndDate());
                }
                term.copyValuesTo(newTerm);
                lastTerm = newTerm;
            }
        }
    }

    // //////////////////////////////////////

    @Programmatic
    public List<CalculationResult> calculationResults(
            final InvoicingFrequency invoicingFrequency,
            final LocalDate startDueDate,
            final LocalDate nextDueDate
            ) {
        List<CalculationResult> results = new ArrayList<CalculationResult>();
        for (LeaseTerm term : getTerms()) {
            results.addAll(term.calculationResults(invoicingFrequency, startDueDate, nextDueDate));
        }
        return results;
    }

    // //////////////////////////////////////

    public static class Predicates {
        private Predicates() {
        }

        public static Predicate<LeaseItem> ofType(final LeaseItemType t) {
            return new Predicate<LeaseItem>() {
                @Override
                public boolean apply(LeaseItem input) {
                    return input.getType() == t;
                }
            };
        }
    }

    // //////////////////////////////////////

    public static class SuspendEvent extends ActionInteractionEvent<LeaseItem> {
        private static final long serialVersionUID = 1L;

        public SuspendEvent(
                final LeaseItem source,
                final Identifier identifier,
                final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    // //////////////////////////////////////

    public static class ResumeEvent extends ActionInteractionEvent<LeaseItem> {
        private static final long serialVersionUID = 1L;

        public ResumeEvent(
                final LeaseItem source,
                final Identifier identifier,
                final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    // //////////////////////////////////////

    @Inject
    private Charges charges;

    @Inject
    LeaseTerms leaseTerms;

}
