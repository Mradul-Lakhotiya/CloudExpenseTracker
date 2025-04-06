package com.expensetracker.lambda;

import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import com.expensetracker.ExpenseTrackerApp;

public class LambdaScheduler {

    private static final String LAMBDA_ARN = "arn:aws:lambda:eu-north-1:054037102171:function:delete-old-s3-files";
    private static final String BUCKET_NAME = ExpenseTrackerApp.BUCKET_NAME;

    public void scheduleFileDeletion(List<String> s3Keys) {
        SchedulerClient scheduler = SchedulerClient.create();

        String payload = String.format("{\"bucket\":\"%s\", \"keys\":%s}", BUCKET_NAME, toJsonArray(s3Keys));
        String scheduleName = "delete-job-" + UUID.randomUUID();
        Instant startTime = Instant.now().plusSeconds(10); // 10 seconds from now

        // === Debug Output ===
        System.out.println();
        System.out.println("----- LambdaScheduler: Scheduling File Deletion -----");
        System.out.println("Bucket Name: " + BUCKET_NAME);
        System.out.println("S3 Keys: " + s3Keys);
        System.out.println("Payload: " + payload);
        System.out.println("Schedule Name: " + scheduleName);
        System.out.println("Schedule Time (UTC): " + formatTime(startTime));
        System.out.println("Lambda ARN: " + LAMBDA_ARN);
        System.out.println("Execution Role ARN: arn:aws:iam::054037102171:role/EventBridgeInvokeLambdaRole");
        System.out.println();

        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .name(scheduleName)
                .scheduleExpression("at(" + formatTime(startTime) + ")")
                .flexibleTimeWindow(FlexibleTimeWindow.builder().mode("OFF").build())
                .target(Target.builder()
                        .arn(LAMBDA_ARN)
                        .roleArn("arn:aws:iam::054037102171:role/EventBridgeInvokeLambdaRole")
                        .input(payload)
                        .build())
                .build();

        try {
            CreateScheduleResponse response = scheduler.createSchedule(request);
            System.out.println("Schedule created successfully: " + response.toString());
        } catch (Exception e) {
            System.out.println("Error while creating schedule:");
            e.printStackTrace();
        }

        System.out.println("------------------------------------------------------");
    }

    private String toJsonArray(List<String> keys) {
        return keys.stream()
                .map(key -> "\"" + key + "\"")
                .reduce((a, b) -> a + "," + b)
                .map(str -> "[" + str + "]")
                .orElse("[]");
    }

    private String formatTime(Instant time) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(time);
    }
}
