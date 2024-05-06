package demo

import arrow.ArrowEitherConverterFactory
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import de.jensklingenberg.ktorfit.Ktorfit
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.wiremock.ListenerMode
import io.kotest.extensions.wiremock.WireMockListener
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

class ExampleApiSpec : FreeSpec({

    val wireMock = WireMockServer(WireMockConfiguration.options().dynamicPort())
    register(WireMockListener(wireMock, ListenerMode.PER_TEST))

    fun buildApiClient(customizer: (Ktorfit.Builder) -> Unit = {}): ExampleApi {
        val ktorfit =
            Ktorfit.Builder().baseUrl(wireMock.baseUrl() + "/")
                .also(customizer)
                .build()

        return ktorfit.create()
    }

    @Language("json")
    fun aPersonJson(): String = """{"name": "Luke Skywalker", "birth_year": "19BBY", "films": ["film1"]}"""

    fun givenPersonResponse(
        status: Int,
        @Language("json") body: String = aPersonJson(),
    ) {
        wireMock.stubFor(
            WireMock.get(WireMock.anyUrl()).willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(status)
                    .withBody(body),
            ),
        )
    }

    "suspend GET" - {
        "return as String" {
            givenPersonResponse(200)

            val exampleApi = buildApiClient()

            exampleApi.getPersonAsString("1") shouldContain "Luke Skywalker"
        }

        "return as KotlinX Serializable" {
            val ktorClient =
                HttpClient {
                    installJson()
                }

            val exampleApi =
                buildApiClient {
                    it.httpClient(ktorClient)
                }

            givenPersonResponse(200)

            exampleApi.getPersonAsSerializable("1").let {
                it.name shouldBe "Luke Skywalker"
                it.birth_year shouldBe "19BBY"
                it.films.shouldNotBeEmpty()
            }
        }

        "return as Either<Exception, KotlinX Serializable>" {
            val ktorClient =
                HttpClient {
                    installJson()
                }

            val exampleApi =
                buildApiClient {
                    it.httpClient(ktorClient)
                        .converterFactories(ArrowEitherConverterFactory())
                }

            givenPersonResponse(200)

            exampleApi.getPersonAsEither("1").shouldBeRight()
                .let {
                    it.name shouldBe "Luke Skywalker"
                    it.birth_year shouldBe "19BBY"
                    it.films.shouldNotBeEmpty()
                }
        }

        "return as Either<Exception, HttpResponse>" {
            val ktorClient =
                HttpClient {
                    installJson()
                }

            val exampleApi =
                buildApiClient {
                    it.httpClient(ktorClient)
                        .converterFactories(ArrowEitherConverterFactory())
                }

            givenPersonResponse(200)

            exampleApi.getPersonAsHttpResponse("1")
                .shouldBeRight().let {
                    it.status shouldBe HttpStatusCode.OK
                }
        }

        "can return HTTP errors on Left" {
            val ktorClient =
                HttpClient {
                    expectSuccess = true

                    installJson()

                    installLogging()
                }

            val exampleApi =
                buildApiClient {
                    it.httpClient(ktorClient)
                        .converterFactories(ArrowEitherConverterFactory())
                }

            givenPersonResponse(404)

            exampleApi.getPersonAsEither("1")
                .shouldBeLeft()
                .shouldBeInstanceOf<ClientRequestException>()
                .let {
                    it.response.status shouldBe HttpStatusCode.NotFound
                }
        }

        "can return Serialization errors on Left" {
            val ktorClient =
                HttpClient {
                    installJson()
                }

            val exampleApi =
                buildApiClient {
                    it.httpClient(ktorClient)
                        .converterFactories(ArrowEitherConverterFactory())
                }

            givenPersonResponse(404, "{}")

            exampleApi.getPersonAsEither("doesNotExist")
                .shouldBeLeft()
                .let {
                    it.shouldBeInstanceOf<JsonConvertException>()
                }
        }
    }

    "non suspended GET" - {
        "return as Either<Exception, KotlinX Serializable>" {
            val ktorClient =
                HttpClient {
                    installJson()
                }

            val exampleApi =
                buildApiClient {
                    it.httpClient(ktorClient)
                        .converterFactories(ArrowEitherConverterFactory())
                }

            givenPersonResponse(200)

            exampleApi.nonSuspendedGetPersonAsEither("1").shouldBeRight()
                .let {
                    it.name shouldBe "Luke Skywalker"
                    it.birth_year shouldBe "19BBY"
                    it.films.shouldNotBeEmpty()
                }
        }

        "can return HTTP errors on Left" {
            val ktorClient =
                HttpClient {
                    expectSuccess = true
                    installJson()
                }

            val exampleApi =
                buildApiClient {
                    it.httpClient(ktorClient)
                        .converterFactories(ArrowEitherConverterFactory())
                }

            givenPersonResponse(404)

            exampleApi.nonSuspendedGetPersonAsEither("1")
                .shouldBeLeft()
                .shouldBeInstanceOf<ClientRequestException>()
        }

        "can return Serialization errors on Left" {
            val ktorClient =
                HttpClient {
                    expectSuccess = true

                    installJson()

                    // installLogging()
                }

            val exampleApi =
                buildApiClient {
                    it.httpClient(ktorClient)
                        .converterFactories(ArrowEitherConverterFactory())
                }

            givenPersonResponse(200, "{}")

            exampleApi.nonSuspendedGetPersonAsEither("1")
                .shouldBeLeft()
                .shouldBeInstanceOf<JsonConvertException>()
        }
    }
})

private fun HttpClientConfig<*>.installLogging() {
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

private fun HttpClientConfig<*>.installJson() {
    install(ContentNegotiation) {
        json(
            Json {
                isLenient = true
                ignoreUnknownKeys = true
            },
        )
    }
}
