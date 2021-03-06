package no.nav.omsorgspengermidlertidigalene.redis

import io.ktor.util.*
import io.lettuce.core.RedisClient
import no.nav.omsorgspengermidlertidigalene.Configuration

class RedisConfig(private val redisConfigurationProperties: RedisConfigurationProperties) {

    @KtorExperimentalAPI
    fun redisClient(configuration: Configuration): RedisClient {
        redisConfigurationProperties.startInMemoryRedisIfMocked()
        return RedisClient.create("redis://${configuration.getRedisHost()}:${configuration.getRedisPort()}")
    }

}