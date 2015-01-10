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

import java.util.List;
import javax.jdo.JDOHelper;
import javax.jdo.annotations.InheritanceStrategy;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancies;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionInteraction;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;
import org.estatio.dom.valuetypes.Level;

/**
 * A domain object that is mutable and can be changed by multiple users over
 * time, and should therefore have optimistic locking controls in place.
 * 
 * <p>
 * Subclasses must be annotated with:
 * 
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
 * Note however that if a subclass that has a supertype which is annotated with
 * {@link javax.jdo.annotations.Version} (eg <tt>CommunicationChannel</tt>) then
 * the subtype must not also have a <tt>Version</tt> annotation (otherwise JDO
 * will end up putting a <tt>version</tt> column in both tables, and they are
 * not kept in sync).
 */
@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class EstatioDomainObject<T extends EstatioDomainObject<T>>
        extends UdoDomainObject<T> {

    public static final String ROOT_APPLICATION_TENANCY_PATH = "/";

    public EstatioDomainObject(
            final String keyProperties) {
        super(keyProperties);
    }

    /**
     * Callback when object is created (using {@link org.apache.isis.applib.DomainObjectContainer#newTransientInstance(Class)}).
     */
    public void created() {
        setApplicationTenancyPath(ROOT_APPLICATION_TENANCY_PATH);
    }

    @Hidden
    public String getId() {
        Object objectId = JDOHelper.getObjectId(this);
        if (objectId == null) {
            return "";
        }
        String objectIdStr = objectId.toString();
        final String id = objectIdStr.split("\\[OID\\]")[0];
        return id;
    }

    // //////////////////////////////////////

    /**
     * this default value will be overridden (in {@link #setApplicationTenancyPath(String)}) for
     */
    //
    private String applicationTenancyPath = ROOT_APPLICATION_TENANCY_PATH;

    @javax.jdo.annotations.Column(
            length = ApplicationTenancy.MAX_LENGTH_PATH,
            allowsNull = "false",
            name = "atPath"
    )
    @Hidden
    public String getApplicationTenancyPath() {
        return applicationTenancyPath;
    }

    public void setApplicationTenancyPath(final String applicationTenancyPath) {
        this.applicationTenancyPath = applicationTenancyPath;
    }

    @PropertyLayout(
            named = "Application Level",
            describedAs = "Determines those users for whom this object is available to view and/or modify."
    )
    public ApplicationTenancy getApplicationTenancy() {
        return applicationTenancies.findTenancyByPath(getApplicationTenancyPath());
    }

    // //////////////////////////////////////

    public static class ApplicationTenancyChangedEvent extends ActionInteractionEvent<EstatioDomainObject> {
        public ApplicationTenancyChangedEvent(final EstatioDomainObject source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    public static class ApplicationTenancyMovedDownEvent extends ApplicationTenancyChangedEvent {
        public ApplicationTenancyMovedDownEvent(final EstatioDomainObject source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    public static class ApplicationTenancyMovedUpEvent extends ApplicationTenancyChangedEvent {
        public ApplicationTenancyMovedUpEvent(final EstatioDomainObject source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    // //////////////////////////////////////

    @ActionSemantics(ActionSemantics.Of.IDEMPOTENT)
    @ActionInteraction(value = ApplicationTenancyMovedDownEvent.class)
    @ActionLayout(
            named = "Move down",
            cssClassFa = "fa-angle-double-down"
    )
    @MemberOrder(name = "applicationTenancy", sequence = "1")
    public T moveDownApplicationTenancy(
            @ParameterLayout(named = "Down to")
            final ApplicationTenancy newApplicationTenancy) {
        setApplicationTenancyPath(newApplicationTenancy.getPath());
        return (T)this;
    }

    public List<ApplicationTenancy> choices0MoveDownApplicationTenancy() {
        final List<ApplicationTenancy> all = applicationTenancies.allTenancies();
        final ApplicationTenancy current = getApplicationTenancy();
        final Level currentLevel = Level.of(current.getPath());
        return Lists.newArrayList(Iterables.filter(all, Level.Predicates.childrenOf(currentLevel)));
    }

    public String disableMoveDownApplicationTenancy(final ApplicationTenancy applicationTenancy) {
        return choices0MoveDownApplicationTenancy().isEmpty()? "No lower levels": null;
    }

    // //////////////////////////////////////

    @ActionSemantics(ActionSemantics.Of.IDEMPOTENT)
    @ActionInteraction(value = ApplicationTenancyMovedUpEvent.class)
    @ActionLayout(
            named = "Move up",
            cssClassFa = "fa-angle-double-up"
    )
    @MemberOrder(name = "applicationTenancy", sequence = "2")
    public T moveUpApplicationTenancy(
            @ParameterLayout(named = "Up to")
            final ApplicationTenancy newApplicationTenancy) {
        setApplicationTenancyPath(newApplicationTenancy.getPath());
        return (T)this;
    }

    public List<ApplicationTenancy> choices0MoveUpApplicationTenancy() {
        final List<ApplicationTenancy> all = applicationTenancies.allTenancies();
        final ApplicationTenancy current = getApplicationTenancy();
        final Level currentLevel = Level.of(current.getPath());
        return Lists.newArrayList(Iterables.filter(all, Level.Predicates.parentsOf(currentLevel)));
    }

    public String disableMoveUpApplicationTenancy(final ApplicationTenancy applicationTenancy) {
        return choices0MoveUpApplicationTenancy().isEmpty()? "No higher levels": null;
    }


    // //////////////////////////////////////

    @Hidden
    public Long getVersionSequence() {
        final Long version = (Long) JDOHelper.getVersion(this);
        return version;
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    ApplicationTenancies applicationTenancies;


}
