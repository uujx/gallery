package edu.vt.cs.cs5254.gallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface FlickrApi {

    @GET(
        "/services/rest/?method=flickr.interestingness.getList" +
                "&api_key=e07f7efe1a4e9b0ce763fa0e76df4b28" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s,geo"
    )
    fun fetchPhotos(): Call<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>
}