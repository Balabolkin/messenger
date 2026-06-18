package com.eltex.messengerapp.di

import com.eltex.messengerapp.BuildConfig
import com.eltex.messengerapp.domain.AppException
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor {
                it.proceed(
                    it.request()
                        .newBuilder()
                        .header("Api-Key", BuildConfig.API_KEY)
                        .header("Authorization", BuildConfig.Authorization)
                        .build()
                )
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()

    @Singleton
    @Provides
    fun provideKtorHttpClient(okHttpClient: OkHttpClient) = HttpClient(OkHttp) {
        engine {
            preconfigured = okHttpClient
        }

        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                }
            )
        }

        expectSuccess = true

        HttpResponseValidator {
            handleResponseException {
                when (it) {
                    is ResponseException -> {
                        when (it.response.status) {
                            HttpStatusCode.Forbidden, HttpStatusCode.Unauthorized -> {
                                throw AppException.Forbidden()
                            }

                            else -> throw AppException.UnknownException(
                                it.response.status.value, it.message,
                            )
                        }
                    }

                    is UnresolvedAddressException, is UnknownHostException -> throw AppException.NetworkException()

                    else -> throw it
                }
            }
        }

        defaultRequest {
            url("https://eltex-android.ru/api/")
            contentType(ContentType.Application.Json)
        }
    }
}