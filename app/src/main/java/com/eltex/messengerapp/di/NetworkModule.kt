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
import java.time.Duration
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authDataStore: AuthDataStore): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .addInterceptor { chain ->
                var request = chain.request()
                val url = request.url
                if (url.scheme == "http" && url.host == BuildConfig.BASE_HOST) {
                    val newUrl = url.newBuilder().scheme("https").build()
                    request = request.newBuilder().url(newUrl).build()
                }
                
                val token = authDataStore.getCachedToken()
                val userId = authDataStore.getCachedUserId()
                
                val requestBuilder = request.newBuilder()
                if (request.url.host == BuildConfig.BASE_HOST) {
                    if (!token.isNullOrEmpty()) {
                        requestBuilder.addHeader("X-Auth-Token", token)
                    }
                    if (!userId.isNullOrEmpty()) {
                        requestBuilder.addHeader("X-User-Id", userId)
                    }
                }
                
                val path = request.url.encodedPath
                if ((path.contains("/avatar") || path.contains("/ufs")) && request.url.host == BuildConfig.BASE_HOST) {
                    if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                        val newUrlBuilder = request.url.newBuilder()
                        if (request.url.queryParameter("rc_token") == null) {
                            newUrlBuilder.addQueryParameter("rc_token", token)
                        }
                        if (request.url.queryParameter("rc_uid") == null) {
                            newUrlBuilder.addQueryParameter("rc_uid", userId)
                        }
                        requestBuilder.url(newUrlBuilder.build())
                    }
                }
                
                chain.proceed(requestBuilder.build())
            }
            .addNetworkInterceptor { chain ->
                var request = chain.request()
                val url = request.url
                if (url.scheme == "http" && url.host == BuildConfig.BASE_HOST) {
                    val newUrl = url.newBuilder().scheme("https").build()
                    request = request.newBuilder().url(newUrl).build()
                }
                
                val token = authDataStore.getCachedToken()
                val userId = authDataStore.getCachedUserId()
                
                val builder = request.newBuilder()
                if (request.header("X-Auth-Token").isNullOrEmpty() && request.url.host == BuildConfig.BASE_HOST) {
                    if (!token.isNullOrEmpty()) {
                        builder.header("X-Auth-Token", token)
                    }
                    if (!userId.isNullOrEmpty()) {
                        builder.header("X-User-Id", userId)
                    }
                }
                
                val path = request.url.encodedPath
                if ((path.contains("/avatar") || path.contains("/ufs")) && request.url.host == BuildConfig.BASE_HOST) {
                    if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                        val newUrlBuilder = request.url.newBuilder()
                        if (request.url.queryParameter("rc_token") == null) {
                            newUrlBuilder.addQueryParameter("rc_token", token)
                        }
                        if (request.url.queryParameter("rc_uid") == null) {
                            newUrlBuilder.addQueryParameter("rc_uid", userId)
                        }
                        builder.url(newUrlBuilder.build())
                    }
                }
                request = builder.build()
                
                val response = chain.proceed(request)
                if (response.isRedirect) {
                    val location = response.header("Location")
                    if (location != null && location.startsWith("http://") && location.contains(BuildConfig.BASE_HOST)) {
                        val secureLocation = location.replaceFirst("http://", "https://")
                        response.newBuilder()
                            .header("Location", secureLocation)
                            .build()
                    } else {
                        response
                    }
                } else {
                    response
                }
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
            url("https://${BuildConfig.BASE_HOST}/")
        }
    }
}