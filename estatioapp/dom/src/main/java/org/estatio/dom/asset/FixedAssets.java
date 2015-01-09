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
package org.estatio.dom.asset;

import java.util.List;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Hidden;
import org.estatio.dom.UdoDomainService;
import org.estatio.dom.utils.StringUtils;

@DomainService(menuOrder = "10", repositoryFor = FixedAsset.class)
@Hidden
public class FixedAssets extends UdoDomainService<FixedAsset> {

    public FixedAssets() {
        super(FixedAssets.class, FixedAsset.class);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @Hidden
    public List<FixedAsset> matchAssetsByReferenceOrName(final String searchPhrase) {
        return allMatches("matchByReferenceOrName", 
                "regex", StringUtils.wildcardToCaseInsensitiveRegex(searchPhrase));
    }
    
    // //////////////////////////////////////

    @Hidden
    public List<FixedAsset> autoComplete(final String searchPhrase) {
        return matchAssetsByReferenceOrName("*".concat(searchPhrase).concat("*"));
    }

}
