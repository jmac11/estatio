/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
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
package org.estatio.dom.valuetypes;

import java.util.List;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

public final class Level implements Comparable<Level> {

    public static Level of(final String path) {
        return path != null ? new Level(path): null;
    }

    public Level(String path) {
        this.path = path;
    }

    //region > path

    private final String path;
    public String getPath() {
        return path;
    }

    //endregion

    //region > parentOf, childOf

    public boolean parentOf(final Level other) {
        return other != null && contains(other, this);
    }

    public boolean childOf(final Level other) {
        return other != null && contains(this, other);
    }

    private static boolean contains(Level parent, Level child) {
        return parent.path.startsWith(child.path) && parent.path.length() > child.path.length();
    }
    //endregion

    //region > parent
    public Level parent() {
        if(path.equals("/")) {
            return null;
        }
        final List<String> parts = getParts();
        final List<String> strings = parts.subList(0, parts.size() - 1);
        final String join = Joiner.on("/").join(strings);
        return new Level("/" + join);
    }

    /**
     * Returns a mutable copy of the parts.
     *
     * <p>
     *     For example:
     * </p>
     * <ul>
     *     <li>"/" -> []</li>
     *     <li>"/a" -> ["a"]</li>
     *     <li>"/a/bb" -> ["a", "bb"]</li>
     * </ul>
     */
    List<String> getParts() {
        return Lists.newArrayList(Iterables.filter(Splitter.on('/').split(path), new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return !Strings.isNullOrEmpty(input);
                    }
                }
        ));
    }
    //endregion


    //region > equals, hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Level that = (Level) o;

        return !(path != null ? !path.equals(that.path) : that.path != null);

    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
    //endregion

    //region > Comparable
    @Override
    public int compareTo(Level o) {
        return path.compareTo(o.path);
    }
    //endregion

    //region > toString
    @Override
    public String toString() {
        return path;
    }

    public static final class Predicates {
        private Predicates(){}

        public final static Predicate<ApplicationTenancy> childrenOf(final Level level) {
            return new Predicate<ApplicationTenancy>() {
                @Override
                public boolean apply(final ApplicationTenancy input) {
                    final Level candidate = of(input.getPath());
                    return candidate.childOf(level);
                }
            };
        }

        public final static Predicate<ApplicationTenancy> parentsOf(final Level level) {
            return new Predicate<ApplicationTenancy>() {
                @Override
                public boolean apply(final ApplicationTenancy input) {
                    final Level candidate = of(input.getPath());
                    return candidate.parentOf(level);
                }
            };
        }
    }


    //endregion

}

