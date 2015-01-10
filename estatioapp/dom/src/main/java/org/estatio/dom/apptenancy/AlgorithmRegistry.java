package org.estatio.dom.apptenancy;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.estatio.dom.EstatioDomainObject;
import org.estatio.dom.agreement.Agreement;
import org.estatio.dom.agreement.AgreementRoles;
import org.estatio.dom.asset.Property;

class AlgorithmRegistry {

    private Map<Class,List<Algorithm>> algorithmListByClass = Maps.newHashMap();
    /**
     * lazily cached
     */
    private Map<Class<? extends EstatioDomainObject>, List<Algorithm>> flattenedAlgorithmListByClass = Maps.newHashMap();

    void addAlgorithms() {
        add(new Algorithm.OnChanged<Property>(Property.class) {
            @Override
            public void executed(final EstatioDomainObject.ApplicationTenancyChangedEvent ev, final Property source) {
                update(source, source.getUnits());
            }
        });
        add(new Algorithm.OnChanged<Agreement>(Agreement.class) {

            @Override
            public void executed(final EstatioDomainObject.ApplicationTenancyChangedEvent ev, final Agreement source) {
                update(source, agreementRoles.findByAgreement(source));
            }

            @Inject
            private AgreementRoles agreementRoles;
        });
    }

    // //////////////////////////////////////

    private <S extends EstatioDomainObject<S>> void add(Algorithm<S, ?> algorithm) {
        final Class<S> sourceClass = algorithm.getSourceClass();
        List<Algorithm> algorithms = algorithmListByClass.get(sourceClass);
        if(algorithms == null) {
            algorithms = Lists.newArrayList();
            algorithmListByClass.put(sourceClass, algorithms);
        }
        algorithms.add(algorithm);
    }

    List<Algorithm> lookup(final EstatioDomainObject source) {

        final Class<? extends EstatioDomainObject> sourceClass = source.getClass();
        List<Algorithm> algorithmList = flattenedAlgorithmListByClass.get(sourceClass);

        if(algorithmList == null) {
            algorithmList = Lists.newArrayList();

            final List<Class<?>> types = superTypesOf(source);

            for (final Class<?> eachType : types) {
                final List<Algorithm> list = algorithmListByClass.get(eachType);
                if(list != null) {
                    algorithmList.addAll(list);
                }
            }

            flattenedAlgorithmListByClass.put(sourceClass, algorithmList);
        }

        return algorithmList;
    }

    private static List<Class<?>> superTypesOf(final EstatioDomainObject source) {
        List<Class<?>> types = Lists.newArrayList();
        Class<?> type = source.getClass();
        while(EstatioDomainObject.class.isAssignableFrom(type) && type != EstatioDomainObject.class) {
            types.add(type);
            type = type.getSuperclass();
        }
        return types;
    }

    // //////////////////////////////////////


}
