package demo

import arrow.core.Either
import de.jensklingenberg.ktorfit.http.GET
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

/**
 * Based on the Ktorfit Quickstart https://foso.github.io/Ktorfit/quick-start/
 */
interface ExampleApi {
    @GET("people/1/")
    suspend fun getPerson(): String

    @GET("people/1/")
    suspend fun getPersonSerializable(): PersonResponse

    @GET("people/1/")
    suspend fun getPersonEither(): Either<Exception, PersonResponse>

    @GET("people/1/")
    fun getPersonEitherNonSuspended(): Either<Exception, PersonResponse>

    @GET("people/a/")
    suspend fun getPersonEitherFailing(): Either<Exception, PersonResponse>

    @GET("people/1/")
    suspend fun getPersonKtor(): Either<Exception, HttpResponse>
}

@Serializable
data class PersonResponse(val name: String, val birth_year: String, val films: List<String>)
