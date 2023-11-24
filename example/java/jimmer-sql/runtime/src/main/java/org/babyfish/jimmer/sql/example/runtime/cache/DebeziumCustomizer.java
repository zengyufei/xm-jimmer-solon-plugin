package org.babyfish.jimmer.sql.example.runtime.cache;

import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.example.model.BookProps;
import org.babyfish.jimmer.sql.runtime.Customizer;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Condition;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

// -----------------------------
// If you are a beginner, please ignore this class,
// for non-cache mode, this class will never be used.
// -----------------------------
@Condition(onProperty = "${solon.env} = 'debezium'")
@Component
public class DebeziumCustomizer implements Customizer {

    private static final Schema BOOK_PRICE_SCHEMA =
            // `BOOK.PRICE` of postgres is `NUMERIC(10, 2)`
            Decimal.schema(2);

    @Override
    public void customize(JSqlClient.Builder builder) {
        builder.setBinLogPropReader(
                LocalDateTime.class,
                (prop, jsonNode) -> {
                    return Instant.ofEpochMilli(
                            jsonNode.asLong() / 1000
                    ).atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
        );
        builder.setBinLogPropReader(
                BookProps.PRICE,
                (prop, jsonNode) -> {
                    byte[] bytes = Base64.getDecoder().decode(jsonNode.asText());
                    return Decimal.toLogical(BOOK_PRICE_SCHEMA, bytes);
                }
        );
    }
}

/*----------------Documentation Links----------------
https://babyfish-ct.github.io/jimmer/docs/mutation/trigger#listen-to-message-queue
---------------------------------------------------*/
