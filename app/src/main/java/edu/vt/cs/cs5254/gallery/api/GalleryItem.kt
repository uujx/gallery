package edu.vt.cs.cs5254.gallery.api

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class GalleryItem(
    val id: String = "",
    val title: String = "",
    val owner: String = "",
    val latitude: String = "",
    val longitude: String = "",
    @SerializedName("url_s")val url: String = ""
) {
    val photoPageUri: Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()
        }
}