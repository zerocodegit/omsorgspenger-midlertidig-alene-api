package no.nav.omsorgspengermidlertidigalene.kafka

import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.omsorgspengermidlertidigalene.felles.Metadata
import no.nav.omsorgspengermidlertidigalene.felles.formaterStatuslogging
import no.nav.omsorgspengermidlertidigalene.felles.somJson
import no.nav.omsorgspengermidlertidigalene.søknad.søknad.KomplettSøknad
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject
import org.slf4j.LoggerFactory

class SøknadKafkaProducer(
    val kafkaConfig: KafkaConfig
) : HealthCheck {
    private companion object {
        private val NAME = "SøknadProducer"
        private val TOPIC_USE = TopicUse(
            name = Topics.MOTTATT_OMS_MIDLERTIDIG_ALENE,
            valueSerializer = SøknadSerializer()
        )

        private val logger = LoggerFactory.getLogger(SøknadKafkaProducer::class.java)
    }

    private val producer = KafkaProducer<String, TopicEntry<JSONObject>>(
        kafkaConfig.producer(NAME),
        TOPIC_USE.keySerializer(),
        TOPIC_USE.valueSerializer
    )

    internal fun produce(
        søknad: KomplettSøknad,
        metadata: Metadata
    ) {
        if (metadata.version != 1) throw IllegalStateException("Kan ikke legge søknad med versjon ${metadata.version} til prosessering.")

        val recordMetaData = producer.send(
            ProducerRecord(
                TOPIC_USE.name,
                søknad.søknadId,
                TopicEntry(
                    metadata = metadata,
                    data = JSONObject(søknad.somJson())
                )
            )
        ).get()

        logger.info(formaterStatuslogging(søknad.søknadId, "sendes til topic ${TOPIC_USE.name} med offset '${recordMetaData.offset()}' til partition '${recordMetaData.partition()}'"))
    }

    internal fun stop() = producer.close()

    override suspend fun check(): Result {
        return try {
            producer.partitionsFor(TOPIC_USE.name)
            Healthy(NAME, "Tilkobling til Kafka OK!")
        } catch (cause: Throwable) {
            logger.error("Feil ved tilkobling til Kafka", cause)
            UnHealthy(NAME, "Feil ved tilkobling mot Kafka. ${cause.message}")
        }
    }
}

private class SøknadSerializer : Serializer<TopicEntry<JSONObject>> {
    override fun serialize(topic: String, data: TopicEntry<JSONObject>) : ByteArray {
        val metadata = JSONObject()
            .put("correlationId", data.metadata.correlationId)
            .put("requestId", data.metadata.requestId)
            .put("version", data.metadata.version)

        return JSONObject()
            .put("metadata", metadata)
            .put("data", data.data)
            .toString()
            .toByteArray()
    }
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
    override fun close() {}
}