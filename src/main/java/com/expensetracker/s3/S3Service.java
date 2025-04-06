package com.expensetracker.s3;

import java.nio.file.Paths;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class S3Service {

    public static void createBucketIfNotExists(S3Client s3, String bucketName) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3.headBucket(headBucketRequest);
            System.out.println("✅ Bucket already exists: " + bucketName);
        } 
        catch (NoSuchBucketException e) {
            System.out.println("🔨 Creating bucket: " + bucketName);
            CreateBucketRequest request = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3.createBucket(request);
            System.out.println("✅ Bucket created: " + bucketName);
        } 
        catch (S3Exception ex) {
            System.err.println("❌ Failed to check or create bucket: " + ex.awsErrorDetails().errorMessage());
        }
    }

    // TODO: Use Some Kind Of frontend to get the file from user assume it will return the s3 file key 
    public static void uploadLocalFileToS3(S3Client s3, String bucketName, String filePath, String keyName) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentType("text/csv")
                .build();

        s3.putObject(putRequest, RequestBody.fromFile(Paths.get(filePath)));
        System.out.println("✅ Local CSV uploaded to S3 as: " + keyName);
    }
}
