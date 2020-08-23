package com.localtest.s3imdbtitleuploader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@SpringBootApplication(exclude = {ContextInstanceDataAutoConfiguration.class})
public class S3ImdbTitleUploaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(S3ImdbTitleUploaderApplication.class, args);
	}

}
