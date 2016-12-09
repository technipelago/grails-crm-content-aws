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

import spock.lang.Specification

/**
 * Test AWS S3 storage.
 */
class AwsContentSpec extends Specification {

    def crmContentService

    def "create file"() {
        when:
        def folder = crmContentService.createFolder(null, "test", "Test folder")
        def content = crmContentService.createResource("Hello World!", "hello.txt", folder, [title: "A classic message"])

        then:
        content.metadata.contentType == 'text/plain'
        content.text == "Hello World!"
        crmContentService.deleteReference(content)
    }
}
