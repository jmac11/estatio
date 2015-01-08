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
package org.estatio.dom;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.InheritanceStrategy;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;
import org.isisaddons.module.security.dom.tenancy.WithApplicationTenancy;
import org.apache.isis.applib.annotation.Hidden;

/**
 * A domain object that is mutable and can be changed by multiple users over time,
 * and should therefore have optimistic locking controls in place.
 * 
 * <p>
 * Subclasses must be annotated with:
 * <pre>
 * @javax.jdo.annotations.DatastoreIdentity(
 *     strategy = IdGeneratorStrategy.NATIVE,
 *     column = "id")
 * @javax.jdo.annotations.Version(
 *     strategy=VersionStrategy.VERSION_NUMBER, 
 *     column="version")
 * public class MyDomainObject extends EstationMutableObject {
 *   ...
 * }
 * </pre>
 * 
 * <p>
 * Note however that if a subclass that has a supertype which is annotated 
 * with {@link javax.jdo.annotations.Version} (eg <tt>CommunicationChannel</tt>)
 * then the subtype must not also have a <tt>Version</tt> annotation (otherwise JDO
 * will end up putting a <tt>version</tt> column in both tables, and they are not 
 * kept in sync).
 */
@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class EstatioDomainObject<T extends UdoDomainObject<T>>
        extends UdoDomainObject<T> implements WithApplicationTenancy {


    public EstatioDomainObject(
            final String keyProperties) {
        super(keyProperties);
    }

    @Hidden
    public String getId() {
        Object objectId = JDOHelper.getObjectId(this);
        if(objectId == null) {
            return "";
        }
        String objectIdStr = objectId.toString();
        final String id = objectIdStr.split("\\[OID\\]")[0];
        return id;
    }


    // //////////////////////////////////////

    private ApplicationTenancy applicationTenancy;

    @javax.jdo.annotations.Column(allowsNull="false")
    public ApplicationTenancy getApplicationTenancy() {
        return applicationTenancy;
    }

    public void setApplicationTenancy(final ApplicationTenancy applicationTenancy) {
        this.applicationTenancy = applicationTenancy;
    }

    // //////////////////////////////////////

    @Hidden
    public Long getVersionSequence() {
        final Long version = (Long) JDOHelper.getVersion(this);
        return version;
    }


}
