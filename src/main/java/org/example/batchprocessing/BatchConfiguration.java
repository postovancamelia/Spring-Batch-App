
package org.example.batchprocessing;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;


/**
 * Spring Batch configuration that reads {@code SectorAccount} records from a CSV located
 * on the classpath, validates and transforms them via a composite processor, and writes
 * the results to an output CSV file. The output file path can be provided as a job
 * parameter named {@code outputFile}.
 *
 * <p><strong>Flow:</strong></p>
 * <ol>
 *   <li><strong>Reader</strong> — {@link #reader()} reads from
 *       {@code na-isal-june-2025-quarter-institutional-sector-accounts.CSV} on the classpath,
 *       skipping the header row and mapping columns to {@code SectorAccount}.</li>
 *   <li><strong>Processors</strong> — a {@link CompositeItemProcessor} chains
 *       {@link #validationProcessor()} (filters invalid items) and
 *       {@link #transformProcessor()} (normalizes/trims fields).</li>
 *   <li><strong>Writer</strong> — {@link #csvWriter(String)} writes to a CSV with a fixed header;
 *       the destination is controlled by the {@code outputFile} job parameter (defaults to

 *       {@code output-sector-accounts.csv}).</li>
 *   <li><strong>Job/Step</strong> — {@link #importUserJob(JobRepository, Step, JobCompletionNotificationListener)}
 *       runs a single step {@link #step1(JobRepository, org.springframework.transaction.PlatformTransactionManager, FlatFileItemReader, CompositeItemProcessor, FlatFileItemWriter)}
 *       configured with chunk size {@code 3}.</li>
 * </ol>
 *
 * <p><strong>Usage Example (Job Parameters):</strong></p>
 * <pre>
 * outputFile=/tmp/sector-accounts-clean.csv
 * </pre>
 */


@Configuration
public class BatchConfiguration {

    // -------- Reader --------
    @Bean
    @StepScope
    public FlatFileItemReader<SectorAccount> reader(){

        return new FlatFileItemReaderBuilder<SectorAccount>()
                .name("sectorAccountReader")
                .resource(new ClassPathResource("na-isal-june-2025-quarter-institutional-sector-accounts.CSV"))
                .delimited()
                .delimiter(",")
                .names(
                        "Series_reference", "period", "value", "Status", "Units", "Magnitude", "seasonality",
                        "SNA_Account", "Transaction", "Transaction_Label", "Asset_type", "Asset_type_label",
                        "Sector", "Sector_name", "Subject", "year", "month"
                )
                .fieldSetMapper(new SectorAccountFieldSetMapper())
                .linesToSkip(1) // skip header row
                .build();
    }

    // -------- Processors (validation + transformation) --------
    @Bean
    public SectorAccountValidationProcessor validationProcessor() {
        return new SectorAccountValidationProcessor(); // filters invalid items
    }

    @Bean
    public SectorAccountItemProcessor transformProcessor() {
        return new SectorAccountItemProcessor(); // trims and normalizes
    }

    @Bean
    public CompositeItemProcessor<SectorAccount, SectorAccount> sectorAccountCompositeProcessor(
            SectorAccountValidationProcessor validationProcessor,
            SectorAccountItemProcessor transformProcessor) {
        CompositeItemProcessor<SectorAccount, SectorAccount> cip = new CompositeItemProcessor<>();
        cip.setDelegates(java.util.List.of(validationProcessor, transformProcessor));
        return cip;
    }

    // -------- Writer (CSV) --------
    @Bean
    @StepScope
    public FlatFileItemWriter<SectorAccount> csvWriter(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['outputFile'] ?: 'output-sector-accounts.csv'}")
            String outputFile) {

        BeanWrapperFieldExtractor<SectorAccount> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{
                "seriesReference", "period", "value", "status", "units", "magnitude", "seasonality",
                "snaAccount", "transaction", "transactionLabel", "assetType", "assetTypeLabel",
                "sector", "sectorName", "subject", "year", "month"
        });

        DelimitedLineAggregator<SectorAccount> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);

        return new FlatFileItemWriterBuilder<SectorAccount>()
                .name("sectorAccountCsvWriter")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(aggregator)
                .headerCallback(w -> w.write(
                        "seriesReference,period,value,status,units,magnitude,seasonality," +
                                "snaAccount,transaction,transactionLabel,assetType,assetTypeLabel," +
                                "sector,sectorName,subject,year,month"))
                .append(false)
                .encoding("UTF-8")
                .build();
    }

    // -------- Job & Step --------
    @Bean
    public Job importUserJob(JobRepository jobRepository,
                             Step step1,
                             JobCompletionNotificationListener listener) {
        return new JobBuilder("importUserJob", jobRepository)
                .listener(listener)
                .start(step1)
                .build();
    }


    @Bean
    public Step step1(JobRepository jobRepository,
                      org.springframework.transaction.PlatformTransactionManager transactionManager,
                      FlatFileItemReader<SectorAccount> reader,
                      CompositeItemProcessor<SectorAccount, SectorAccount> processor,
                      FlatFileItemWriter<SectorAccount> writer) {

        return new StepBuilder("step1", jobRepository)
                .<SectorAccount, SectorAccount>chunk(3)               // prefer plain chunk(..)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .transactionManager(transactionManager)               // set TM explicitly
                .build();
    }

}

