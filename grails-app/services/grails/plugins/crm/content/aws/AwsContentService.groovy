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

/**
 * Amazon S3 Content Service.
 */
class AwsContentService {

    def amazonWebService

    def createBucket(String bucketName) {
        amazonWebService.s3.createBucket(bucketName)
    }

    List listBuckets() {
        amazonWebService.s3.listBuckets()
    }

    List listFiles(String bucketName) {
        ObjectListing objectListing = amazonWebService.s3.listObjects(bucketName)
        objectListing.objectSummaries.collect { S3ObjectSummary summary ->
            [key: summary.key, lastModified: summary.lastModified, size: summary.size]
        }
    }

    List listFiles(String bucketName, String path) {
        ObjectListing objectListing = amazonWebService.s3.listObjects(bucketName, path)
        objectListing.objectSummaries.collect { S3ObjectSummary summary ->
            [key: summary.key, lastModified: summary.lastModified, size: summary.size]
        }
    }

    def uploadFile(String bucketName, String path, File file) {
        amazonWebService.s3.putObject(new PutObjectRequest(bucketName, path, file).withCannedAcl(CannedAccessControlList.PublicRead))
    }

    def downloadFile(String bucketName, String path, File file) {
        amazonWebService.s3.getObject(new GetObjectRequest(bucketName, path), file)
    }

    def deleteFile(String bucketName, String path) {
        amazonWebService.s3.deleteObject(bucketName, path)
    }
}
