package com.eltex.messengerapp.di

import com.eltex.messengerapp.BuildConfig
import com.eltex.messengerapp.domain.AppException
import com.eltex.messengerapp.datastore.AuthDataStore
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
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authDataStore: AuthDataStore): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = runBlocking { authDataStore.getToken().first() }
                val userId = runBlocking { authDataStore.getUserId().first() }
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("X-Auth-Token", token)
                }
                if (!userId.isNullOrEmpty()) {
                    requestBuilder.addHeader("X-User-Id", userId)
                }
                chain.proceed(requestBuilder.build())
            }

            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
    }

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

        install(Logging) {
            level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
            logger = io.ktor.client.plugins.logging.Logger.SIMPLE
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
            url("https://study-chat.eltex-co.ru/")
            contentType(ContentType.Application.Json)
        }
    }
}