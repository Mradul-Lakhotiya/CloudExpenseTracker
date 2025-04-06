package com.expensetracker.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import com.expensetracker.ExpenseTrackerApp;

import java.util.Map;

public class DeleteOldS3FileHandler implements RequestHandler<Map<String, String>, String> {

    private static final S3Client s3Client = S3Client.create();
    private static final String BUCKET_NAME = ExpenseTrackerApp.BUCKET_NAME;

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        context.getLogger().log("🔄 DeleteOldS3FileHandler triggered.");
        context.getLogger().log("Received event: " + event.toString());

        String s3Key = event.get("s3Key");

        if (s3Key == null || s3Key.isEmpty()) {
            String msg = "❌ Error: 's3Key' missing in input.";
            context.getLogger().log(msg);
            return msg;
        }

        try {
            context.getLogger().log("Attempting to delete from bucket: " + BUCKET_NAME + ", key: " + s3Key);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            String successMsg = "✅ Successfully deleted file: " + s3Key;
            context.getLogger().log(successMsg);
            return successMsg;

        } catch (Exception e) {
            String errMsg = "❌ Deletion failed for key: " + s3Key + " | Error: " + e.getMessage();
            context.getLogger().log(errMsg);
            return errMsg;
        }
    }
}
