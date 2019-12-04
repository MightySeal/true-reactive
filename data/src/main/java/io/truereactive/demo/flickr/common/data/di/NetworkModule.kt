package io.truereactive.demo.flickr.common.data.di

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.truereactive.demo.flickr.common.data.BuildConfig
import io.truereactive.demo.flickr.common.data.api.FlickrApi
import io.truereactive.demo.flickr.common.data.device.NetworkStateRepository
import io.truereactive.demo.flickr.common.data.repository.PhotosRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Singleton
@Component(modules = [NetworkModule::class])
interface DataComponent {

    fun exposePhotosRepository(): PhotosRepository

    fun exposeNetworkStateRepository(): NetworkStateRepository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): DataComponent
    }
}

@Module
internal abstract class NetworkModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val original = chain.request()
                        val originalUrl = original.url

                        val newUrl = originalUrl.newBuilder()
                            .addQueryParameter("api_key", BuildConfig.FLICKR_KEY)
                            .addQueryParameter("format", "json")
                            .addQueryParameter("nojsoncallback", "1")
                            .build()

                        val newRequest = original.newBuilder().url(newUrl).build()

                        return chain.proceed(newRequest)
                    }
                }).addInterceptor(HttpLoggingInterceptor().apply {
                    // level = HttpLoggingInterceptor.Level.BODY
                    level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
        }

        @Provides
        @JvmStatic
        fun provideMoshi(): Moshi {
            return Moshi.Builder()
                .build()
        }

        // TODO: lazy okhttp
        @Provides
        @JvmStatic
        fun provideApi(okHttp: OkHttpClient, moshi: Moshi): FlickrApi {
            return Retrofit.Builder()
                .baseUrl(FlickrApi.ENDPOINT)
                .client(okHttp)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(FlickrApi::class.java)
        }
    }
}