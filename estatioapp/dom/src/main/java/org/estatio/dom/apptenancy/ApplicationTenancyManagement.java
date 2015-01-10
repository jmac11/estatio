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
package org.estatio.dom.apptenancy;

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.estatio.dom.EstatioDomainObject;
import org.estatio.dom.UdoDomainService;


/**
 * Centralizes rules governing the moving of {@link org.isisaddons.module.security.dom.tenancy.ApplicationTenancy}.
 */
@DomainService
public class ApplicationTenancyManagement extends UdoDomainService<ApplicationTenancyManagement> {

    private AlgorithmRegistry algorithmRegistry;

    public ApplicationTenancyManagement() {
        super(ApplicationTenancyManagement.class);
    }

    // //////////////////////////////////////

    @Programmatic
    @PostConstruct
    public void init(Map<String, String> properties) {
        super.init(properties);

        algorithmRegistry = new AlgorithmRegistry();
        algorithmRegistry.addAlgorithms();
    }


    // //////////////////////////////////////

    @com.google.common.eventbus.Subscribe
    @Programmatic
    public void on(final EstatioDomainObject.ApplicationTenancyMovedDownEvent ev) {
        final EstatioDomainObject source = ev.getSource();

        List<Algorithm> algorithms = algorithmRegistry.lookup(source);
        for (Algorithm algorithm : algorithms) {
            getContainer().injectServicesInto(algorithm);

            switch (ev.getPhase()) {
                case HIDE:
                    algorithm.hide(ev, source);
                    break;
                case DISABLE:
                    algorithm.disable(ev, source);
                    break;
                case VALIDATE:
                    algorithm.validate(ev, source);
                    break;
                case EXECUTING:
                    algorithm.executing(ev, source);
                    break;
                case EXECUTED:
                    algorithm.executed(ev, source);
                    break;
            }
        }
    }


}
