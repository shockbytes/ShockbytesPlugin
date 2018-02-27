package at.shockbytes.plugin.service.dependency.api

import at.shockbytes.plugin.service.dependency.model.api.JitpackIoDependency
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface JitpackIoApi {

    @GET("builds/{package}/{artifact}/latest")
    fun resolveDependencyVersion(@Path("package") pack: String,
                                 @Path("artifact") artifact: String): Observable<JitpackIoDependency>

    companion object {

        const val SERVICE_ENDPOINT = "https://jitpack.io/api/"
    }

}