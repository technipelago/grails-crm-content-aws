/*
 * Copyright (c) 2016 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.content.aws

import grails.plugins.crm.content.CrmContentProvider
import grails.plugins.crm.content.CrmContentProviderFactory

/**
 * Amazon Web Services Content Provider Factory.
 */
class AwsContentProviderFactory implements CrmContentProviderFactory {

    def crmContentProvider

    @Override
    CrmContentProvider getProvider(String filename, long length, Object reference, String username) {
        crmContentProvider
    }

    @Override
    CrmContentProvider getProvider(URI resourceURI) {
        if(resourceURI.scheme == 's3') {
            return crmContentProvider
        }
        return null
    }

    @Override
    List<CrmContentProvider> getProviders() {
        [crmContentProvider]
    }
}
