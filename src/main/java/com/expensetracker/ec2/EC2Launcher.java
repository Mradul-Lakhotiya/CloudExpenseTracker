package com.expensetracker.ec2;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Instance;


public class EC2Launcher {

	public static void launchInstance() {
		// TODO: The Region is Hard Coded rn so mke it gentralized
		Region region = Region.EU_NORTH_1;
		Ec2Client ec2 = Ec2Client.builder().region(region).build();
		SsmClient ssm = SsmClient.builder().region(region).build();
	
		// 1. ✅ Retrieve the latest Amazon Linux 2 AMI ID
		String amiAlias = "/aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2";
		GetParameterRequest parameterRequest = GetParameterRequest.builder()
				.name(amiAlias)
				.build();
		GetParameterResponse parameterResponse = ssm.getParameter(parameterRequest);
		String amiId = parameterResponse.parameter().value();
	
		// 2. 🚀 Launch the instance
		RunInstancesRequest runRequest = RunInstancesRequest.builder()
				.imageId(amiId)
				.instanceType(InstanceType.T3_MICRO)
				.maxCount(1)
				.minCount(1)
				.build();
	
		RunInstancesResponse runResponse = ec2.runInstances(runRequest);
		String instanceId = runResponse.instances().get(0).instanceId();
		System.out.println("🚀 EC2 instance launched with ID: " + instanceId);
	
		// 3. 🏷️ Tag the instance
		Tag tag = Tag.builder().key("Name").value("ExpenseTrackerInstance").build();
		CreateTagsRequest tagRequest = CreateTagsRequest.builder()
				.resources(instanceId)
				.tags(tag)
				.build();
		ec2.createTags(tagRequest);
		System.out.println("🏷️ Instance tagged as: ExpenseTrackerInstance");
	
		// 4. 🌐 Get the Public IP
		DescribeInstancesRequest describeRequest = DescribeInstancesRequest.builder()
				.instanceIds(instanceId)
				.build();
	
		// Wait briefly to ensure public IP is assigned
		try {
			Thread.sleep(5000); // 5 seconds
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		DescribeInstancesResponse describeResponse = ec2.describeInstances(describeRequest);
		Reservation reservation = describeResponse.reservations().get(0);
		Instance instance = reservation.instances().get(0);
	
		String publicIp = instance.publicIpAddress();
		System.out.println("🌐 Public IP address: " + (publicIp != null ? publicIp : "Not available yet"));
	
		ec2.close();
		ssm.close();
	}
		
}
