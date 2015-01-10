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
package org.estatio.dom.financial.bankaccount;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.annotation.TypicalLength;

import org.estatio.dom.UdoDomainRepositoryAndFactory;
import org.estatio.dom.JdoColumnLength;
import org.estatio.dom.RegexValidation;
import org.estatio.dom.financial.FinancialAccount;
import org.estatio.dom.financial.FinancialAccountType;
import org.estatio.dom.financial.FinancialAccounts;
import org.estatio.dom.financial.utils.IBANValidator;
import org.estatio.dom.party.Party;

@DomainService(menuOrder = "30", repositoryFor = FinancialAccount.class)
@Named("Accounts")
public class BankAccounts extends UdoDomainRepositoryAndFactory<BankAccount> {

    public BankAccounts() {
        super(BankAccounts.class, BankAccount.class);
    }

    @Override
    public String iconName() {
        return "FinancialAccount";
    }

    // //////////////////////////////////////

    @NotContributed
    @ActionSemantics(Of.NON_IDEMPOTENT)
    public BankAccount newBankAccount(
            final @Named("Owner") Party owner,
            final @Named("IBAN") @TypicalLength(JdoColumnLength.BankAccount.IBAN) String iban) {
        final BankAccount bankAccount = newTransientInstance(BankAccount.class);
        bankAccount.setReference(iban);
        bankAccount.setName(iban);
        bankAccount.setIban(iban);
        bankAccount.refresh();
        persistIfNotAlready(bankAccount);
        bankAccount.setOwner(owner);
        return bankAccount;
    }

    public String validateNewBankAccount(
            final Party owner,
            final String iban) {
        if (!IBANValidator.valid(iban)) {
            return "Not a valid IBAN number";
        }
        return null;
    }

    // //////////////////////////////////////

    @Programmatic
    public BankAccount newBankAccount(
            final @Named("Owner") Party owner,
            final @Named("Reference") @RegEx(validation = RegexValidation.REFERENCE, caseSensitive = true) String reference,
            final @Named("Name") String name) {
        final BankAccount bankAccount = newTransientInstance(BankAccount.class);
        bankAccount.setReference(reference);
        bankAccount.setName(name);
        persistIfNotAlready(bankAccount);
        bankAccount.setOwner(owner);
        return bankAccount;
    }

    // //////////////////////////////////////

    @Programmatic
    public List<BankAccount> findBankAccountsByOwner(final Party party) {
        return Lists.newArrayList(
                Iterables.filter(financialAccounts.findAccountsByTypeOwner(FinancialAccountType.BANK_ACCOUNT, party),
                        BankAccount.class));
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @Prototype
    @MemberOrder(sequence = "99")
    public List<BankAccount> allBankAccounts() {
        return allInstances();
    }

    // //////////////////////////////////////

    @Inject
    private FinancialAccounts financialAccounts;

}
