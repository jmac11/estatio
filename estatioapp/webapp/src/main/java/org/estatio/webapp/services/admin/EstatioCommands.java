/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.estatio.webapp.services.admin;

import java.util.List;
import java.util.UUID;
import org.isisaddons.module.command.dom.CommandJdo;
import org.isisaddons.module.command.dom.CommandServiceJdoRepository;
import org.joda.time.LocalDate;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.services.clock.ClockService;
import org.estatio.dom.UdoService;

@DomainService
@DomainServiceLayout(
        named = "Changes",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        menuOrder = "20.2"
)
public class EstatioCommands extends UdoService<EstatioCommands> {

    public EstatioCommands() {
        super(EstatioCommands.class);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @Bookmarkable
    @MemberOrder(sequence="1")
    public List<CommandJdo> commandsCurrentlyRunning() {
        return commandServiceRepository.findCurrent();
    }
    public boolean hideCommandsCurrentlyRunning() {
        return commandServiceRepository == null;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="2")
    public List<CommandJdo> commandsPreviouslyRan() {
        return commandServiceRepository.findCompleted();
    }
    public boolean hideCommandsPreviouslyRan() {
        return commandServiceRepository == null;
    }

    // //////////////////////////////////////

    @MemberOrder(sequence="10.3")
    @ActionSemantics(Of.SAFE)
    public CommandJdo lookupCommand(
            final @Named("Transaction Id") UUID transactionId) {
        return commandServiceRepository.findByTransactionId(transactionId);
    }
    public boolean hideLookupCommand() {
        return commandServiceRepository == null;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="10.4")
    public List<CommandJdo> findCommands(
            final @Optional @Named("From") LocalDate from,
            final @Optional @Named("To") LocalDate to) {
        return commandServiceRepository.findByFromAndTo(from, to);
    }
    public boolean hideFindCommands() {
        return commandServiceRepository == null;
    }
    public LocalDate default0FindCommands() {
        return clockService.now().minusDays(7);
    }
    public LocalDate default1FindCommands() {
        return clockService.now();
    }


    // //////////////////////////////////////

    @javax.inject.Inject
    private CommandServiceJdoRepository commandServiceRepository;

    @javax.inject.Inject
    private ClockService clockService;

}

