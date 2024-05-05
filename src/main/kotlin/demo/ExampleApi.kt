package demo

import arrow.core.Either
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

/**
 * Based on the Ktorfit Quickstart https://foso.github.io/Ktorfit/quick-start/
 */
interface ExampleApi {
    @GET("people/{peopleId}/")
    suspend fun getPersonAsString(
        @Path("peopleId") id: String,
    ): String

    @GET("people/{peopleId}/")
    suspend fun getPersonAsSerializable(
        @Path("peopleId") id: String,
    ): PersonResponse

    @GET("people/{peopleId}/")
    suspend fun getPersonAsEither(
        @Path("peopleId") id: String,
    ): Either<Exception, PersonResponse>

    @GET("people/{peopleId}/")
    fun nonSuspendedGetPersonAsEither(
        @Path("peopleId") id: String,
    ): Either<Exception, PersonResponse>

    @GET("people/{peopleId}/")
    suspend fun getPersonAsHttpResponse(
        @Path("peopleId") id: String,
    ): Either<Exception, HttpResponse>
}

@Serializable
data class PersonResponse(val name: String, val birth_year: String, val films: List<String>)
