package org.estatio.dom.valuetypes;

import java.util.List;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;
import org.junit.Test;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

public class LevelTest {

    public static class PredicatesTest {

        static ApplicationTenancy applicationTenancyOf(final String path) {
            final ApplicationTenancy applicationTenancy = new ApplicationTenancy();
            applicationTenancy.setPath(path);
            return applicationTenancy;
        }

        public static class ParentsOf extends PredicatesTest {

            @Test
            public void whenHasParents() throws Exception {
                //given
                final List<ApplicationTenancy> candidates = Lists.newArrayList(
                        applicationTenancyOf("/"),
                        applicationTenancyOf("/a"),
                        applicationTenancyOf("/x"),
                        applicationTenancyOf("/a/b"),
                        applicationTenancyOf("/a/x"),
                        applicationTenancyOf("/a/b/c"),
                        applicationTenancyOf("/a/b/c/d"));

                final Predicate<ApplicationTenancy> parentsOf = Level.Predicates.parentsOf(Level.of("/a/b/c"));

                // when
                final Iterable<ApplicationTenancy> matching = Iterables.filter(candidates, parentsOf);

                // then
                final List<ApplicationTenancy> expected = Lists.newArrayList(
                        candidates.get(0),
                        candidates.get(1),
                        candidates.get(3));
                assertThat(matching, contains(expected.toArray()));
            }

            @Test
            public void whenNoParents() throws Exception {
                //given
                final List<ApplicationTenancy> candidates = Lists.newArrayList(
                        applicationTenancyOf("/"),
                        applicationTenancyOf("/a"),
                        applicationTenancyOf("/x"),
                        applicationTenancyOf("/a/b"),
                        applicationTenancyOf("/a/x"),
                        applicationTenancyOf("/a/b/c"),
                        applicationTenancyOf("/a/b/c/d"));
                final Predicate<ApplicationTenancy> parentsOf = Level.Predicates.parentsOf(Level.of("/"));

                // when
                final Iterable<ApplicationTenancy> matching = Iterables.filter(candidates, parentsOf);

                // then
                assertThat(matching, isEmpty());
            }

        }

        public static class ChildrenOf extends PredicatesTest {

            @Test
            public void whenHasChildren() throws Exception {
                //given
                final List<ApplicationTenancy> candidates = Lists.newArrayList(
                        applicationTenancyOf("/"),
                        applicationTenancyOf("/a"),
                        applicationTenancyOf("/b"),
                        applicationTenancyOf("/b/p"),
                        applicationTenancyOf("/a/b"),
                        applicationTenancyOf("/a/c"),
                        applicationTenancyOf("/a/w/x/y"));

                final Predicate<ApplicationTenancy> childrenOf = Level.Predicates.childrenOf(Level.of("/a"));

                // when
                final Iterable<ApplicationTenancy> matching = Iterables.filter(candidates, childrenOf);

                // then
                final List<ApplicationTenancy> expected = Lists.newArrayList(
                        candidates.get(4),
                        candidates.get(5),
                        candidates.get(6));
                assertThat(matching, contains(expected.toArray()));
            }

            @Test
            public void whenNoChildren() throws Exception {

                //given
                final List<ApplicationTenancy> candidates = Lists.newArrayList(
                        applicationTenancyOf("/"),
                        applicationTenancyOf("/a"));
                final Predicate<ApplicationTenancy> childrenOf = Level.Predicates.childrenOf(Level.of("/a"));

                // when
                final Iterable<ApplicationTenancy> matching = Iterables.filter(candidates, childrenOf);

                // then
                assertThat(matching, isEmpty());
            }

        }

        static Matcher<? super Iterable<ApplicationTenancy>> isEmpty() {
            return new TypeSafeMatcher<Iterable<ApplicationTenancy>>() {
                @Override
                protected boolean matchesSafely(final Iterable<ApplicationTenancy> item) {
                    return !item.iterator().hasNext();
                }

                @Override
                public void describeTo(final Description description) {
                    description.appendText("is empty");
                }
            };
        }

    }
}