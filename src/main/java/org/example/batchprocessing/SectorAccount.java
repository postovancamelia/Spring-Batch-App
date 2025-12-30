package org.example.batchprocessing;

import java.math.BigDecimal;
import java.time.YearMonth;

public record SectorAccount(
        String seriesReference,
        YearMonth period,
        String value,
        String status,
        String units,
        int magnitude,
        String seasonality,
        String snaAccount,
        String transaction,
        String transactionLabel,
        String assetType,
        String assetTypeLabel,
        int sector,
        String sectorName,
        String subject,
        int year,
        int month
) {}
