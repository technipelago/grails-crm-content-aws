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

class CrmContentAwsGrailsPlugin {
    def version = "2.4.2-SNAPSHOT"
    def grailsVersion = "2.4 > *"
    def pluginExcludes = [
                "src/groovy/grails/plugins/crm/content/aws/TestSecurityDelegate.groovy",
                "grails-app/views/error.gsp"
        ]
    def loadAfter = ['crmContent']
    def title = "GR8 CRM AWS Content Plugin"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
Provide storage of GR8 CRM content in Amazon S3 buckets.
'''

    def documentation = "http://gr8crm.github.io/plugins/crm-content-aws/"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/technipelago/grails-crm-content-aws/issues"]
    def scm = [url: "https://github.com/technipelago/grails-crm-content-aws"]

    def doWithSpring = {
        awsContentProvider(grails.plugins.crm.content.aws.AwsContentProvider) { bean ->
            bean.autowire = 'byName'
            bean.primary = true // To make DefaultContentRouter select this provider before the local file system provider.
        }
    }
}
