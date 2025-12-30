package org.example.batchprocessing;

import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class SectorAccountFieldSetMapper implements FieldSetMapper<SectorAccount> {

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy.MM");

    @Override
    public SectorAccount mapFieldSet(FieldSet fs) {
        String seriesReference = fs.readString("Series_reference");
        String periodStr = fs.readString("period");
        YearMonth period = (periodStr == null || periodStr.isBlank())
                ? null
                : YearMonth.parse(periodStr.trim(), PERIOD_FMT);

        String value = fs.readString("value");
        String status = fs.readString("Status");
        String units = fs.readString("Units");
        int magnitude = fs.readInt("Magnitude");
        String seasonality = fs.readString("seasonality");
        String snaAccount = fs.readString("SNA_Account");
        String transaction = fs.readString("Transaction");
        String transactionLabel = fs.readString("Transaction_Label");
        String assetType = fs.readString("Asset_type");
        String assetTypeLabel = fs.readString("Asset_type_label");

        int sector = fs.readInt("Sector");
        String sectorName = fs.readString("Sector_name");
        String subject = fs.readString("Subject");

        int year = fs.readInt("year");
        int month = fs.readInt("month");

        return new SectorAccount(
                seriesReference, period, value, status, units, magnitude, seasonality,
                snaAccount, transaction, transactionLabel, assetType, assetTypeLabel,
                sector, sectorName, subject, year, month
        );
    }
}
