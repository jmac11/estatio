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
package org.estatio.dom.index;

import java.util.List;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;

import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.RegexValidation;

@DomainService(repositoryFor = Index.class)
@DomainServiceLayout(
        named="Indices",
        menuBar = DomainServiceLayout.MenuBar.PRIMARY,
        menuOrder = "60.2"
)
public class Indices extends EstatioDomainService<Index> {

    public Indices() {
        super(Indices.class, Index.class);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.NON_IDEMPOTENT)
    @MemberOrder(sequence = "1")
    public Index newIndex(
            final @Named("Reference") @RegEx(validation = RegexValidation.REFERENCE, caseSensitive = true) String reference, 
            final @Named("Name") String name) {
        final Index index = newTransientInstance();
        index.setReference(reference);
        index.setName(name);
        persist(index);
        return index;
    }

    // //////////////////////////////////////
    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "2")
    public List<Index> allIndices() {
        return allInstances();
    }
    // //////////////////////////////////////

    @Programmatic
    public Index findIndex(final @Named("Reference") String reference) {
        return firstMatch("findByReference", "reference", reference);
    }

    @Programmatic
    public Index findOrCreateIndex(final String reference, final String name) {
        Index index = findIndex(reference);
        if (index == null) {
            index  = newIndex(reference, name);
        }
        return index;
    }

}
