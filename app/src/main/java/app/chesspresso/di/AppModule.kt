package app.chesspresso.di

import android.content.Context
import app.chesspresso.api.LobbyApiService
import app.chesspresso.auth.data.AuthApi
import app.chesspresso.auth.data.AuthRepository
import app.chesspresso.data.api.AuthApi as JwtAuthApi
import app.chesspresso.data.api.GameApi
import app.chesspresso.data.network.AuthInterceptor
import app.chesspresso.data.storage.TokenStorage
import app.chesspresso.service.LobbyService
import app.chesspresso.websocket.WebSocketManager
import app.chesspresso.websocket.StompWebSocketService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenStorage(@ApplicationContext context: Context): TokenStorage {
        return TokenStorage(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStorage: TokenStorage): AuthInterceptor {
        return AuthInterceptor(tokenStorage)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // Android Emulator localhost
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideJwtAuthApi(retrofit: Retrofit): JwtAuthApi {
        return retrofit.create(JwtAuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGameApi(retrofit: Retrofit): GameApi {
        return retrofit.create(GameApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLobbyApiService(retrofit: Retrofit): LobbyApiService {
        return retrofit.create(LobbyApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideLobbyService(
        lobbyApiService: LobbyApiService,
        gson: Gson
    ): LobbyService {
        return LobbyService(lobbyApiService, gson)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        jwtAuthApi: JwtAuthApi,
        tokenStorage: TokenStorage,
        @ApplicationContext context: Context,
        webSocketService: StompWebSocketService
    ): AuthRepository {
        return AuthRepository(authApi, jwtAuthApi, tokenStorage, context, webSocketService)
    }

    @Provides
    @Singleton
    fun provideWebSocketManager(): WebSocketManager {
        return WebSocketManager
    }

    @Provides
    @Singleton
    fun provideStompWebSocketService(
        tokenStorage: TokenStorage
    ): StompWebSocketService {
        return StompWebSocketService(tokenStorage)
    }
}
