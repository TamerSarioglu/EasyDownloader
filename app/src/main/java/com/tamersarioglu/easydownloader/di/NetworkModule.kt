package com.tamersarioglu.easydownloader.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

import com.tamersarioglu.easydownloader.data.remote.api.VideoDownloaderApiService
import com.tamersarioglu.easydownloader.data.remote.config.NetworkConfig
import com.tamersarioglu.easydownloader.data.remote.interceptor.AuthInterceptor
import com.tamersarioglu.easydownloader.data.remote.interceptor.RetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing network-related dependencies
 * Configures Retrofit, OkHttp, and related networking components
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // DataStore extension property
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = NetworkConfig.DATASTORE_NAME
    )

    /**
     * Provides DataStore for preferences storage
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * Provides JSON serializer configuration
     */
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    }

    /**
     * Provides HTTP logging interceptor for debug builds
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // Enable logging for debug builds - you can change this based on build variant
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provides configured OkHttp client with interceptors and timeouts
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(retryInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Provides configured Retrofit instance with Kotlinx Serialization
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(NetworkConfig.CONTENT_TYPE_JSON.toMediaType())
            )
            .build()
    }

    /**
     * Provides API service implementation
     */
    @Provides
    @Singleton
    fun provideVideoDownloaderApiService(retrofit: Retrofit): VideoDownloaderApiService {
        return retrofit.create(VideoDownloaderApiService::class.java)
    }
}