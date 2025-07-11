package com.bnpparibas.bp2s.combo.comboservices.library.kafka.unit.context;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.context.KafkaErrorMetadataContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorMetadataContextTest {

    @Test
    void shouldStoreAndClearValues() {
        KafkaErrorMetadataContext.put("k1", "v1");
        assertThat(KafkaErrorMetadataContext.get("k1")).contains("v1");
        assertThat(KafkaErrorMetadataContext.getAll()).containsEntry("k1", "v1");
        assertThat(KafkaErrorMetadataContext.getAll()).size().isEqualTo(1);

        KafkaErrorMetadataContext.clear();
        assertThat(KafkaErrorMetadataContext.get("k1")).isEmpty();
        assertThat(KafkaErrorMetadataContext.getAll()).isEmpty();
    }
}
