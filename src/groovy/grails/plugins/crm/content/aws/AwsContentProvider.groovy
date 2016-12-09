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

import com.amazonaws.services.s3.model.*
import grails.plugins.crm.content.CrmContentProvider
import grails.plugins.crm.core.WebUtils
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * Amazon Web Services Content Provider.
 */
class AwsContentProvider implements CrmContentProvider {

    public static final String scheme = "s3"

    def amazonWebService
    def grailsApplication

    private String getBucketName() {
        grailsApplication.config.crm.content.s3.bucket ?: 'grails'
    }

    private Map<String, Object> parseMetadata(String key, ObjectMetadata awsMeta) {
        Map<String, Object> md = [:]
        Map<String, String> userMetadata = awsMeta.userMetadata
        if (userMetadata) {
            md.putAll(userMetadata)
        }
        md.uri = scheme + "://" + key
        md.contentType = awsMeta.contentType
        md.bytes = awsMeta.contentLength
        md['size'] = WebUtils.bytesFormatted(awsMeta.contentLength)
        md.icon = 'page_white' // TODO implement mapping of content type or extension to icon
        md.created = awsMeta.lastModified
        md.modified = awsMeta.lastModified
        md.hash = awsMeta.contentMD5
        md.encrypted = false

        return md
    }

    @Override
    Map<String, Object> create(InputStream content, String contentType, String name, String principal) {
        String key = UUID.randomUUID().toString()
        Map<String, String> userMetadata = [:]
        ObjectMetadata metadata = new ObjectMetadata()
        metadata.setUserMetadata(userMetadata)
        metadata.setContentType(contentType)

        final PutObjectResult result = amazonWebService.s3.putObject(new PutObjectRequest(getBucketName(), key, content, metadata))

        parseMetadata(key, result.metadata)
    }

    @Override
    Map<String, Object> update(URI uri, InputStream content, String contentType) {
        String key = uri.getHost()
        Map<String, String> userMetadata = [:]
        ObjectMetadata metadata = new ObjectMetadata()
        metadata.setUserMetadata(userMetadata)
        metadata.setContentType(contentType)

        final PutObjectResult result = amazonWebService.s3.putObject(new PutObjectRequest(getBucketName(), key, content, metadata))

        parseMetadata(key, result.metadata)
    }

    @Override
    Reader getReader(URI uri, String charsetName) {
        final String key = uri.getHost()
        final S3Object object = amazonWebService.s3.getObject(new GetObjectRequest(getBucketName(), key))
        // NOTE Don't forget to close the reader! => objectData.close()
        new InputStreamReader(object.getObjectContent(), charsetName ?: 'UTF-8')
    }

    @Override
    long read(OutputStream buf, URI uri) {
        final String key = uri.getHost()
        final S3Object object = amazonWebService.s3.getObject(new GetObjectRequest(getBucketName(), key))
        final long len = object.objectMetadata.contentLength
        final InputStream inputStream = object.getObjectContent()

        try {
            buf << inputStream
            buf.flush()
        } finally {
            inputStream.close()
        }

        return len
    }

    @Override
    Object withInputStream(URI uri,
                           @ClosureParams(value = SimpleType.class, options = "java.io.InputStream") Closure work) {
        final String key = uri.getHost()
        final S3Object object = amazonWebService.s3.getObject(new GetObjectRequest(getBucketName(), key))
        final InputStream inputStream = object.getObjectContent()

        try {
            work.call(inputStream)
        } finally {
            inputStream.close()
        }
    }

    @Override
    boolean copy(URI from, URI to) {
        final String fromKey = from.getHost()
        final S3Object object = amazonWebService.s3.getObject(new GetObjectRequest(getBucketName(), fromKey))
        final InputStream inputStream = object.getObjectContent()

        String toKey = to.getHost() // UUID.randomUUID().toString()
        Map<String, String> userMetadata = [:]
        ObjectMetadata metadata = new ObjectMetadata()
        metadata.setUserMetadata(userMetadata)
        metadata.setContentType(object.objectMetadata.contentType)

        final PutObjectResult result = amazonWebService.s3.putObject(new PutObjectRequest(getBucketName(), toKey, inputStream, metadata))

        return true
    }

    @Override
    boolean delete(URI uri) {
        final String key = uri.getHost()

        amazonWebService.s3.deleteObject(new DeleteObjectRequest(getBucketName(), key))

        return true
    }

    @Override
    Map<String, Object> getMetadata(URI uri) {
        final String key = uri.getHost()
        final S3Object object = amazonWebService.s3.getObject(new GetObjectRequest(getBucketName(), key))

        parseMetadata(key, object.objectMetadata)
    }

    @Override
    long getLength(URI uri) {
        final String key = uri.getHost()
        final S3Object object = amazonWebService.s3.getObject(new GetObjectRequest(getBucketName(), key))

        object.objectMetadata.contentLength
    }

    @Override
    long getLastModified(URI uri) {
        final String key = uri.getHost()
        final S3Object object = amazonWebService.s3.getObject(new GetObjectRequest(getBucketName(), key))

        object.objectMetadata.lastModified.time
    }

    @Override
    long check(@ClosureParams(value = SimpleType.class, options = "java.net.URI") Closure<Boolean> worker) {
        long size = 0
        final ObjectListing objectListing = amazonWebService.s3.listObjects(getBucketName())
        for (S3ObjectSummary summary in objectListing.getObjectSummaries()) {
            long length = summary.size
            String key = summary.key
            def clone = worker.rehydrate(summary, this, this)
            clone.resolveStrategy = Closure.DELEGATE_FIRST
            if (clone.call(new URI("$scheme://$key"))) {
                size += length
            }
        }
        size
    }
}
