package com.localtest.s3imdbtitleuploader.configuration;

import com.localtest.s3imdbtitleuploader.data.entity.Title;
import com.localtest.s3imdbtitleuploader.data.repository.TitleRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableRetry
public class BatchConfiguration {

    private final int DEFAULT_CHUNK_SIZE = 5000;
    private final int DEFAULT_PAGE_SIZE = 5000;

    private EntityManager em;


    public BatchConfiguration(EntityManagerFactory emf) {
        this.em = emf.createEntityManager();
    }

    @Bean
    public Job s3ImdbTitleUploader(final JobBuilderFactory jobBuilderFactory,
                                   Step partitionStep) {
        return jobBuilderFactory.get("s3ImdbTitleUploader")
            .incrementer(new RunIdIncrementer())

            .start(partitionStep)
            .build();
    }

    private long getRowsCount(EntityManager em) {
        return em.createQuery("SELECT COUNT(t) FROM Title t", Long.class).getSingleResult();
    }

    @Bean
    public Step partitionStep(StepBuilderFactory stepBuilderFactory,
                              Step writeTitlesToFileStep) {

        long rowCount = getRowsCount(this.em);
        ResourcePartitioner partitioner = new ResourcePartitioner((int) rowCount);

        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("BATCH-");

        return stepBuilderFactory.get("partitionStep")
            .partitioner("writeTitlesToFileStep", partitioner)
            .step(writeTitlesToFileStep)
            .gridSize(1) // Will be calculated automatically by partitioner
            .taskExecutor(taskExecutor)
            .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Title> titleWriter(@Value("#{stepExecutionContext['maxItemCount']}") Long maxItemCount) throws IOException {
        final String DEFAULT_FILE_CONTEXT_PATH = "target/generated/data";
        final String fileName = String.format("titles-%d.txt", maxItemCount);

        Resource resource = new FileSystemResource(DEFAULT_FILE_CONTEXT_PATH + "/" + fileName);

        return new FlatFileItemWriterBuilder<Title>()
            .name("titleReader")
            .resource(resource)
            .lineAggregator(new PassThroughLineAggregator<>())
            .append(true)
            .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Title> titleReader(@Value("#{stepExecutionContext['currentItemCount']}") Long currentItemCount,
                                                   @Value("#{stepExecutionContext['maxItemCount']}") Long maxItemCount,
                                                   TitleRepository repository) {
        return new RepositoryItemReaderBuilder<Title>()
            .currentItemCount(currentItemCount.intValue())
            .maxItemCount(maxItemCount.intValue())
            .repository(repository)
            .name("titleReader")
            .methodName("findAll")
            .sorts(Map.of("id", Sort.Direction.ASC))
            .pageSize(DEFAULT_PAGE_SIZE)
            .build();
    }

    @Bean
    public Step writeTitlesToFileStep(StepBuilderFactory stepBuilderFactory,
                                      RepositoryItemReader<Title> reader,
                                      FlatFileItemWriter<Title> writer) {
        return stepBuilderFactory.get("writeTitlesToFileStep")
            .<Title, Title>chunk(DEFAULT_CHUNK_SIZE)
            .reader(reader)
            .writer(new ItemWriter<Title>() {
                @Override
                public void write(List<? extends Title> list) throws Exception {
                    throw new CannotCreateTransactionException("Aaaa");
                }
            })
            .faultTolerant()
            .retryLimit(5)
            .retry(CannotCreateTransactionException.class)
            .build();
    }
}
