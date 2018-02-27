package at.shockbytes.plugin.service.dependency.api

import at.shockbytes.plugin.service.dependency.model.api.MavenCentralDependency
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface MavenCentralApi {

    @GET("select")
    fun resolveDependencyVersion(@Query("q", encoded = true) query: String,
                                 @Query("rows") rows: Int = 1,
                                 @Query("wt") format: String = "json"): Observable<MavenCentralDependency>

    companion object {

        const val SERVICE_ENDPOINT = "https://search.maven.org/solrsearch/"
    }

}