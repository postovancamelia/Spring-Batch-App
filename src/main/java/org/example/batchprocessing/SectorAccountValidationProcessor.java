package org.example.batchprocessing;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.util.Objects;

public class SectorAccountValidationProcessor implements ItemProcessor<SectorAccount, SectorAccount> {

    @Override
    public SectorAccount process(SectorAccount item) {
        if (item == null) return null;

        boolean invalid =
                isBlank(item.seriesReference()) ||
                        item.period() == null ||
                        item.value() == null ||
                        isBlank(item.units()) ||
                        item.month() < 1 || item.month() > 12 ||
                        item.year() < 1900;

        return invalid ? null : item; // filter invalid rows
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
