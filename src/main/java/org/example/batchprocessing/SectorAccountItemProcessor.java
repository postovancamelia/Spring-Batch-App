
package org.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class SectorAccountItemProcessor implements ItemProcessor<SectorAccount, SectorAccount> {

    private static final Logger log = LoggerFactory.getLogger(SectorAccountItemProcessor.class);

    @Override
    public SectorAccount process(final SectorAccount sectorAccount) {
        if (sectorAccount == null) return null;

        // simple normalization: trim selected strings
        String status = trim(sectorAccount.status());
        String units = trim(sectorAccount.units());
        String seasonality = trim(sectorAccount.seasonality());
        String snaAccount = trim(sectorAccount.snaAccount());
        String transaction = trim(sectorAccount.transaction());
        String transactionLabel = trim(sectorAccount.transactionLabel());
        String assetType = trim(sectorAccount.assetType());
        String assetTypeLabel = trim(sectorAccount.assetTypeLabel());
        String sectorName = trim(sectorAccount.sectorName());
        String subject = trim(sectorAccount.subject());
        String seriesReference = trim(sectorAccount.seriesReference());

        SectorAccount transformed = new SectorAccount(
                seriesReference,
                sectorAccount.period(),
                sectorAccount.value(),
                status,
                units,
                sectorAccount.magnitude(),
                seasonality,
                snaAccount,
                transaction,
                transactionLabel,
                assetType,
                assetTypeLabel,
                sectorAccount.sector(),
                sectorName,
                subject,
                sectorAccount.year(),
                sectorAccount.month()
        );

        log.info("Transformed item: {} -> {}", sectorAccount, transformed);

        return transformed;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
