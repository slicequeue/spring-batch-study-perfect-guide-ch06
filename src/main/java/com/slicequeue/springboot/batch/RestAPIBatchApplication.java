package com.slicequeue.springboot.batch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

@EnableBatchProcessing
@SpringBootApplication
public class RestAPIBatchApplication {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();

    }

    @Bean
    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step 1 ran today!");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @RestController
    public static class JobLunchingController {

        @Autowired
        private JobLauncher jobLauncher;

        @Autowired
        private ApplicationContext context;

        @PostMapping(path = "/run")
        public ExitStatus runJob(@RequestBody JobLauncherRequest request) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
            Job job = this.context.getBean(request.getName(), Job.class);

            return this.jobLauncher.run(job, request.getJobParameters())
                    .getExitStatus();
        }


    }

    public static class JobLauncherRequest {
        private String name;

        private Properties jobParameters;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Properties getJobParamsProperties() {
            return jobParameters;
        }

        public void setJobParamsProperties(Properties jobParameters) {
            this.jobParameters = jobParameters;
        }

        public JobParameters getJobParameters() {
            Properties properties = new Properties();
            properties.putAll(this.jobParameters);
            return new JobParametersBuilder(properties)
                    .toJobParameters();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(RestAPIBatchApplication.class, args);
    }

}
