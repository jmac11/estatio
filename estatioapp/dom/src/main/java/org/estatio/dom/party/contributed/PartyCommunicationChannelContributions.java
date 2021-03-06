package org.estatio.dom.party.contributed;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.applib.annotation.Where;

import org.estatio.dom.communicationchannel.CommunicationChannel;
import org.estatio.dom.communicationchannel.CommunicationChannelType;
import org.estatio.dom.communicationchannel.CommunicationChannels;
import org.estatio.dom.party.Party;

@DomainService
@Hidden
public class PartyCommunicationChannelContributions {

    @ActionSemantics(Of.SAFE)
    @NotContributed(As.ACTION)
    @Hidden(where = Where.OBJECT_FORMS)
    public String phoneNumbers(Party party) {
        return StringUtils.join(channelTitle(party, CommunicationChannelType.PHONE_NUMBER, 0), " | ");
    }

    @ActionSemantics(Of.SAFE)
    @NotContributed(As.ACTION)
    @Hidden(where = Where.OBJECT_FORMS)
    public String emailAddresses(Party party) {
        return StringUtils.join(channelTitle(party, CommunicationChannelType.EMAIL_ADDRESS, 0), " | ");
    }

    private List<String> channelTitle(Party party, final CommunicationChannelType type, final int index) {
        final SortedSet<CommunicationChannel> results = communicationChannels.findByOwnerAndType(party, type);
        ArrayList<String> titles = new ArrayList<String>();
        for (CommunicationChannel communicationChannel : results) {
            titles.add(container.titleOf(communicationChannel));
        }
        return titles;
    }

    @Inject
    private CommunicationChannels communicationChannels;

    @Inject
    private DomainObjectContainer container;

}
