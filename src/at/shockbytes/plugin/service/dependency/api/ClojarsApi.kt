package at.shockbytes.plugin.service.dependency.api

import at.shockbytes.plugin.service.dependency.model.api.ClojarsDependency
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface ClojarsApi {

    @GET("artifacts/{package}/{artifact}")
    fun resolveDependencyVersion(@Path("package") pack: String,
                                 @Path("artifact") artifact: String): Observable<ClojarsDependency>

    companion object {

        const val SERVICE_ENDPOINT = "https://clojars.org/api/"
    }

}