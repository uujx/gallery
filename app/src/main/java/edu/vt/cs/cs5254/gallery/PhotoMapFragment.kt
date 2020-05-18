package edu.vt.cs.cs5254.gallery

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.vt.cs.cs5254.gallery.api.GalleryItem

private const val TAG = "PhotoMapFragment"

class PhotoMapFragment : MapViewFragment(), GoogleMap.OnMarkerClickListener {

    private lateinit var thumbnailDownloader: ThumbnailDownloader<Marker>
    private var geoGalleryItemMap: Map<String, GalleryItem> = emptyMap()

    private val photoMapViewModel: PhotoMapViewModel by lazy {
        ViewModelProvider(this).get(PhotoMapViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { marker, bitmap ->
            setMarkerIcon(marker, bitmap)
        }

        lifecycle.addObserver(
            thumbnailDownloader.fragmentLifecycleObserver
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )

        return onCreateMapView(
            inflater,
            container,
            savedInstanceState,
            R.layout.fragment_photo_map,
            R.id.map_view
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onMapViewCreated(
            view,
            savedInstanceState
        ) { googleMap ->
            googleMap.setOnMarkerClickListener(this@PhotoMapFragment)
            updateUI()
        }

        photoMapViewModel.geoGalleryItemMapLiveData.observe(
            viewLifecycleOwner,
            Observer {
                geoGalleryItemMap = it
                updateUI()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifecycleObserver
        )
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val id = p0?.tag as String
        Log.i(TAG, "The marker $id was clicked")

        val galleryItem = geoGalleryItemMap[id]
        val photoPageUri = galleryItem?.photoPageUri ?: return false

        val intent = PhotoPageActivity.newInstance(requireContext(), photoPageUri)
        startActivity(intent)

        return true
    }

    private fun updateUI() {

        // guard
        if (!isAdded || geoGalleryItemMap.values.isEmpty() || !mapIsInitialized()) {
            return
        }

        Log.i(TAG, "Gallery has has " + geoGalleryItemMap.size + " items")

        // remove all markers, overlays, etc. from the map
        googleMap.clear()

        val bounds = LatLngBounds.Builder()

        for (item in geoGalleryItemMap.values) {
            // log the information of each gallery item with a valid lat-lng
            Log.i(
                TAG,
                "Item id=${item.id} " +
                        "lat=${item.latitude} long=${item.longitude} " +
                        "title=${item.title}"
            )
            // create a lan-lng point for the item and add it to the lat-lng bounds
            val itemPoint = LatLng(item.latitude.toDouble(), item.longitude.toDouble())
            bounds.include(itemPoint)

            // create a marker for the item and add it to the map
            val itemMarker = MarkerOptions().position(itemPoint).title(item.title)
            val marker = googleMap.addMarker(itemMarker)
            marker.tag = item.id

            thumbnailDownloader.queueThumbnail(marker, item.url)
        }

        Log.i(TAG, "Expecting ${geoGalleryItemMap.size} markers on the map")
    }


}
