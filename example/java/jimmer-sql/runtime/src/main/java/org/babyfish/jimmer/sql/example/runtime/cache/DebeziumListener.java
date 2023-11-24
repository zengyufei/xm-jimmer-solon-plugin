//package org.babyfish.jimmer.sql.example.runtime.cache;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.babyfish.jimmer.sql.JSqlClient;
//import org.babyfish.jimmer.sql.event.binlog.BinLog;
//import org.noear.solon.annotation.Component;
//import org.noear.solon.annotation.Condition;
//
//// -----------------------------
//// If you are a beginner, please ignore this class,
//// for non-cache mode, this class will never be used.
//// -----------------------------
//@Condition(onProperty = "${solon.env} = 'debezium'")
//@Component
//public class DebeziumListener {
//
//    private static final ObjectMapper MAPPER = new ObjectMapper();
//
//    private final BinLog binLog;
//
//    public DebeziumListener(JSqlClient sqlClient) {
//        this.binLog = sqlClient.getBinLog();
//    }
//
//    @KafkaListener(topicPattern = "debezium\\..*")
//    public void onDebeziumEvent(
//            @Payload(required = false) String json,
//            Acknowledgment acknowledgment
//    ) throws JsonProcessingException {
//        if (json != null) { // Debezium sends an empty message after deleting a message
//            JsonNode node = MAPPER.readTree(json);
//            String tableName = node.get("source").get("table").asText();
//            binLog.accept(
//                    tableName,
//                    node.get("before"),
//                    node.get("after")
//            );
//        }
//        acknowledgment.acknowledge();
//    }
//}
//
///*----------------Documentation Links----------------
//https://babyfish-ct.github.io/jimmer/docs/mutation/trigger#listen-to-message-queue
//---------------------------------------------------*/
