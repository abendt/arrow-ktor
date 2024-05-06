package arrow

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.left
import arrow.core.right
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.KtorfitResult
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking

class ArrowEitherConverterFactory : Converter.Factory {
    override fun suspendResponseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.SuspendResponseConverter<HttpResponse, Either<*, *>>? {
        if (typeData.typeInfo.type == Either::class) {
            return object : Converter.SuspendResponseConverter<HttpResponse, Either<*, *>> {
                override suspend fun convert(result: KtorfitResult): Either<Any, Any> =
                    result.fold(::Left) {
                        readBody(it, typeData)
                    }

                @Deprecated("deprecated in interface")
                override suspend fun convert(response: HttpResponse): Either<Throwable, Any> {
                    throw AssertionError("required by the interface but shouldn't be used directly!")
                }
            }
        }
        return null
    }

    private suspend fun readBody(
        httpResponse: HttpResponse,
        typeData: TypeData,
    ): Either<Any, Any> =
        try {
            httpResponse.body<Any>(typeData.typeArgs[1].typeInfo).right()
        } catch (ex: Exception) {
            ex.left()
        }

    override fun responseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.ResponseConverter<HttpResponse, Either<*, *>>? {
        if (typeData.typeInfo.type == Either::class) {
            return object : Converter.ResponseConverter<HttpResponse, Either<*, *>> {
                override fun convert(getResponse: suspend () -> HttpResponse): Either<Any, Any> =
                    runBlocking {
                        try {
                            readBody(getResponse(), typeData)
                        } catch (ex: Exception) {
                            ex.left()
                        }
                    }
            }
        }
        return null
    }
}

internal inline fun <T> KtorfitResult.fold(
    onFailure: (Throwable) -> T,
    onSuccess: (HttpResponse) -> T,
): T =
    when (this) {
        is KtorfitResult.Failure -> onFailure(throwable)
        is KtorfitResult.Success -> onSuccess(this.response)
    }
