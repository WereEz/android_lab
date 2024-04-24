package com.example.android.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.android.CustomPoint
import com.example.android.MapManager
import com.example.android.R
import com.example.android.databinding.FragmentHomeBinding
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.TextStyle
import okhttp3.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        MapManager.savedCameraPosition?.let { cameraPosition ->
            mapView.getMapWindow().getMap().move(cameraPosition)
        }
        val mapId = arguments?.getString("map") ?: -1
        if (mapId=="1"){
            fetchPointsAndAddToMap()
            fetchWeatherAndSetTemperature()
        } else {
            for (place in  MapManager.pointsList) {
                addPointToMap(place.point, place.name, place.xid)

            }
            if (MapManager.weather != null) {
                binding.temperatureTextView.text = MapManager.weather
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        val currentCameraPosition = mapView.getMapWindow().getMap().cameraPosition
        MapManager.savedCameraPosition = currentCameraPosition
        Log.d("MapPosition", "Saving camera position: ${currentCameraPosition.target.latitude}")
        super.onDestroyView()
        _binding = null
    }

    private fun fetchPointsAndAddToMap() {
        val baseUrl = "https://api.opentripmap.com/0.1/ru/places/radius?"
        val apiKey = "5ae2e3f221c38a28845f05b68c75a17a04b6be53c8ed44b386646482"
        val currentCameraPosition = mapView.getMapWindow().getMap().cameraPosition
        val latitude = currentCameraPosition.target.latitude
        val longitude = currentCameraPosition.target.longitude
        val radius = 1000 // Радиус поиска в метрах
        val url = "$baseUrl&radius=$radius&lon=$longitude&lat=$latitude&src_attr=wikidata&apikey=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                responseBody?.let { parseAndAddPointsToMap(it) }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Error", "Failed to fetch points: ${e.message}")
            }
        })
    }

    private fun parseAndAddPointsToMap(responseBody: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            val uniqueNames = HashSet<String>() // Множество для хранения уникальных имен
            if (jsonObject.has("error")) {
                val errorMessage = jsonObject.getString("error")
                Log.e("API Error", errorMessage)
                // Обработка сообщения об ошибке здесь, если необходимо
            } else {
                val featuresArray = jsonObject.getJSONArray("features")
                for (i in 0 until featuresArray.length()) {
                    val featureObject = featuresArray.getJSONObject(i)
                    val geometryObject = featureObject.getJSONObject("geometry")
                    val coordinatesArray = geometryObject.getJSONArray("coordinates")
                    val latitude = coordinatesArray.getDouble(1)
                    val longitude = coordinatesArray.getDouble(0)
                    val propertiesObject = featureObject.getJSONObject("properties")
                    val name = propertiesObject.getString("name")
                    val xid = propertiesObject.getString("xid")
                    val point = Point(latitude, longitude)
                    if (name.isNotEmpty() && !uniqueNames.contains(name)) { // Проверяем, не было ли уже такого имени
                        uniqueNames.add(name)
                        MapManager.pointsList.add(CustomPoint(point,name,xid))
                        activity?.runOnUiThread {
                            addPointToMap(point, name, xid)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("JSON Parsing Error", "Failed to parse JSON: ${e.message}")
        }
    }

    private fun addPointToMap(point: Point, label: String, xid: String) {
        navController = requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
       val placemarkTapListener = MapObjectTapListener { mapObject, _ ->
            val bundle = Bundle().apply {
                putString("xid", xid)
            }
            navController.navigate(R.id.fragment_landmark, bundle)
           true
        }
        val placemark = mapView.map.mapObjects.addPlacemark(point).apply {
            setText(
                label,
                TextStyle().apply {
                    size = 10f
                    placement = TextStyle.Placement.BOTTOM
                    offset = 1f
                }
            )
        }
        placemark.setIcon(ImageProvider.fromResource(binding.root.context, R.drawable.flag))
        placemark.addTapListener(placemarkTapListener)
        }
    private fun fetchWeatherAndSetTemperature() {
        val currentCameraPosition = mapView.getMapWindow().getMap().cameraPosition
        val latitude = currentCameraPosition.target.latitude
        val longitude = currentCameraPosition.target.longitude
        val apiKey = "d9b98972e3bea41ef642b2afbe3ef566"
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                responseBody?.let { parseAndSetTemperature(it) }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Error", "Failed to fetch weather: ${e.message}")
            }
        })
    }

    private fun parseAndSetTemperature(responseBody: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            val mainObject = jsonObject.getJSONObject("main")
            val temperatureKelvin = mainObject.getDouble("temp")
            val temperatureCelsius = temperatureKelvin - 273.15
            activity?.runOnUiThread {
                binding.temperatureTextView.text  = "${temperatureCelsius.toInt()}°C"
                MapManager.weather = "${temperatureCelsius.toInt()}°C"
            }
        } catch (e: JSONException) {
            Log.e("JSON Parsing Error", "Failed to parse JSON: ${e.message}")
        }
    }
}

