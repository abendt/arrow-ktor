package demo

import arrow.ArrowEitherConverterFactory
import de.jensklingenberg.ktorfit.Ktorfit
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ExampleApiSpec : StringSpec({

    "suspend GET -> String" {
        val exampleApi = buildApiClient()

        exampleApi.getPerson() shouldContain "Luke Skywalker"
    }

    "suspend GET -> KotlinX Serializable" {
        val ktorClient =
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }

        val exampleApi =
            buildApiClient {
                it.httpClient(ktorClient)
            }

        exampleApi.getPersonJson().let {
            it.name shouldBe "Luke Skywalker"
            it.birth_year shouldBe "19BBY"
            it.films.shouldNotBeEmpty()
        }
    }

    "suspend GET -> Either<Exception, KotlinX Serializable>" {

        val ktorClient =
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }

        val exampleApi =
            buildApiClient {
                it.httpClient(ktorClient)
                    .converterFactories(ArrowEitherConverterFactory())
            }

        exampleApi.getPersonArrow().shouldBeRight()
            .let {
                it.name shouldBe "Luke Skywalker"
                it.birth_year shouldBe "19BBY"
                it.films.shouldNotBeEmpty()
            }
    }

    "GET -> Either<Exception, KotlinX Serializable>" {
        val ktorClient =
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }

        val exampleApi =
            buildApiClient {
                it.httpClient(ktorClient)
                    .converterFactories(ArrowEitherConverterFactory())
            }

        exampleApi.getPersonArrow2().shouldBeRight()
            .let {
                it.name shouldBe "Luke Skywalker"
                it.birth_year shouldBe "19BBY"
                it.films.shouldNotBeEmpty()
            }
    }

    "suspend GET -> Either<Exception, HttpResponse>" {
        val ktorClient =
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }

        val exampleApi =
            buildApiClient {
                it.httpClient(ktorClient)
                    .converterFactories(ArrowEitherConverterFactory())
            }

        exampleApi.getPersonKtor()
            .shouldBeRight().let {
                it.status shouldBe HttpStatusCode.OK
            }
    }

    "Either.Left contains HTTP error" {
        val ktorClient =
            HttpClient {

                expectSuccess = true

                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }

                install(Logging) {
                    level = LogLevel.ALL

                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                println(message)
                            }
                        }
                }
            }

        val exampleApi =
            buildApiClient {
                it.httpClient(ktorClient)
                    .converterFactories(ArrowEitherConverterFactory())
            }

        exampleApi.getPersonArrowFailing()
            .shouldBeLeft()
            .let {
                it.shouldBeInstanceOf<ClientRequestException>()

                it.response.status shouldBe HttpStatusCode.NotFound
            }
    }

    "Either.Left contains Serialization Error" {
        val ktorClient =
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }

        val exampleApi =
            buildApiClient {
                it.httpClient(ktorClient)
                    .converterFactories(ArrowEitherConverterFactory())
            }

        exampleApi.getPersonArrowFailing()
            .shouldBeLeft()
            .let {
                it.shouldBeInstanceOf<JsonConvertException>()
            }
    }
})

private fun buildApiClient(customizer: (Ktorfit.Builder) -> Unit = {}): ExampleApi {
    val ktorfit =
        Ktorfit.Builder().baseUrl("https://swapi.dev/api/")
            .also(customizer)
            .build()

    return ktorfit.create()
}
