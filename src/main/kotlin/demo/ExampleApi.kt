package demo

import arrow.core.Either
import de.jensklingenberg.ktorfit.http.GET
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

interface ExampleApi {
    @GET("people/1/")
    suspend fun getPerson(): String

    @GET("people/1/")
    suspend fun getPersonJson(): PersonResponse

    @GET("people/1/")
    suspend fun getPersonArrow(): Either<Throwable, PersonResponse>

    @GET("people/1/")
    fun getPersonArrow2(): Either<Throwable, PersonResponse>

    @GET("people/a/")
    suspend fun getPersonArrowFailing(): Either<Throwable, PersonResponse>

    @GET("people/1/")
    suspend fun getPersonKtor(): Either<Throwable, HttpResponse>
}

@Serializable
data class PersonResponse(val name: String, val birth_year: String, val films: List<String>)
