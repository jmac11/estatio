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
package org.estatio.dom.agreement;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Programmatic;
import org.estatio.dom.UdoDomainService;

@DomainService(menuOrder = "25", repositoryFor = AgreementType.class)
@Hidden
public class AgreementTypes extends UdoDomainService<AgreementType> {

    public AgreementTypes() {
        super(AgreementTypes.class, AgreementType.class);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    public AgreementType find(final String title) {
        return firstMatch("findByTitle", "title", title);
    }

    @Programmatic
    public AgreementType findOrCreate(final String title) {
        AgreementType agreementType = find(title);
        if (agreementType == null) {
            agreementType = getContainer().newTransientInstance(AgreementType.class);
            agreementType.setTitle(title);
            getContainer().persist(agreementType);
        }
        return agreementType;
    }

}
