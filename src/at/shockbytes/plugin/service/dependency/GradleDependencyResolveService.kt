package at.shockbytes.plugin.service.dependency

import at.shockbytes.plugin.service.dependency.api.ClojarsApi
import at.shockbytes.plugin.service.dependency.api.JitpackIoApi
import at.shockbytes.plugin.service.dependency.api.MavenCentralApi
import at.shockbytes.plugin.service.dependency.model.GradleDependency
import at.shockbytes.plugin.service.dependency.model.api.GoogleDependency
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class GradleDependencyResolveService {

    private var mavenCentralApi: MavenCentralApi
    private var jitpackIoApi: JitpackIoApi
    private var clojarsApi: ClojarsApi

    private var okHttpClient: OkHttpClient

    init {
        okHttpClient = initializeOkHttpClient()
        mavenCentralApi = initializeMavenCentralApi()
        jitpackIoApi = initializeJitpackIoApi()
        clojarsApi = initializeClojarsApi()
    }

    private fun initializeMavenCentralApi(): MavenCentralApi {
        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(MavenCentralApi.SERVICE_ENDPOINT)
                .build()
                .create(MavenCentralApi::class.java)
    }

    private fun initializeJitpackIoApi(): JitpackIoApi {
        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(JitpackIoApi.SERVICE_ENDPOINT)
                .build()
                .create(JitpackIoApi::class.java)
    }

    private fun initializeClojarsApi(): ClojarsApi {
        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(ClojarsApi.SERVICE_ENDPOINT)
                .build()
                .create(ClojarsApi::class.java)
    }

    private fun initializeOkHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(loggingInterceptor)
        return clientBuilder.build()
    }

    fun resolveDependencyVersion(dep: GradleDependency,
                                 endpoint: GradleDependency.EndPoint): Observable<GradleDependency> {

        return when (endpoint) {

            GradleDependency.EndPoint.MAVEN_CENTRAL -> {
                mavenCentralApi.resolveDependencyVersion(dep.query)
                        .map { it.asGradleDependency(dep.title, dep.statement, dep.query) }
            }
            GradleDependency.EndPoint.JITPACK_IO -> {
                val split = dep.query.split("/")
                jitpackIoApi.resolveDependencyVersion(split[0], split[1])
                        .map { it.asGradleDependency(dep.title, dep.statement, dep.query) }
            }
            GradleDependency.EndPoint.GOOGLE -> {
                // TODO Replace with api call
                Observable.just(GoogleDependency(dep.version))
                        .map { it.asGradleDependency(dep.title, dep.statement, dep.query) }
            }
            GradleDependency.EndPoint.CLOJARS -> {
                val split = dep.query.split("/")
                clojarsApi.resolveDependencyVersion(split[0], split[1])
                        .map { it.asGradleDependency(dep.title, dep.statement, dep.query) }
            }
        }
    }


}