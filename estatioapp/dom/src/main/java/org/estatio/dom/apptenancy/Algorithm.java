package org.estatio.dom.apptenancy;

import javax.inject.Inject;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.estatio.dom.EstatioDomainObject;

public class Algorithm<S extends EstatioDomainObject<S>, E> {

    private final Class<S> sourceClass;

    public Algorithm(final Class<S> sourceClass) {
        this.sourceClass = sourceClass;
    }

    public Class<S> getSourceClass() {
        return sourceClass;
    }

    public void hide(final E ev, final S source) {
    }
    public void disable(final E ev, final S source) {
    }
    public void validate(final E ev, final S source) {
    }
    public void executing(final E ev, final S source) {
    }
    public void executed(final E ev, final S source) {
    }

    /**
     * Convenience for subclasses.
     */
    protected void update(final EstatioDomainObject<?> source, final Iterable<? extends EstatioDomainObject<?>> targetList) {
        for (EstatioDomainObject<?> target : targetList) {
            target.setApplicationTenancyPath(source.getApplicationTenancyPath());
        }
    }

    protected S wrap(final S domainObject) {
        return wrapperFactory.wrap(domainObject);
    }
    protected S wrapNoExecute(S domainObject) {
        return wrapperFactory.wrapNoExecute(domainObject);
    }
    protected S wrap(S domainObject, final WrapperFactory.ExecutionMode mode) {
        return wrapperFactory.wrap(domainObject, mode);
    }

    @Inject
    private WrapperFactory wrapperFactory;

    public static class OnChanged<S extends EstatioDomainObject<S>> extends Algorithm<S, EstatioDomainObject.ApplicationTenancyChangedEvent> {
        public OnChanged(final Class<S> sourceClass) {
            super(sourceClass);
        }
    }

    public static class OnMovedDown<S extends EstatioDomainObject<S>> extends Algorithm<S, EstatioDomainObject.ApplicationTenancyMovedDownEvent> {
        public OnMovedDown(final Class<S> sourceClass) {
            super(sourceClass);
        }
    }

    public static class OnMovedUp<S extends EstatioDomainObject<S>> extends Algorithm<S, EstatioDomainObject.ApplicationTenancyMovedUpEvent> {
        public OnMovedUp(final Class<S> sourceClass) {
            super(sourceClass);
        }
    }
}
