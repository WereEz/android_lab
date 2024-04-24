package com.example.android

import android.content.Context
import android.os.Bundle
import androidx.room.Room
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.android.databinding.ActivityMainBinding
import com.example.android.sampledata.AppDatabase
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView


data class CustomPoint(
    val point: Point,
    val name: String,
    val xid: String
)

object MapManager {
     var mapView: MapView? = null
     var isInitialized = false
     var weather: String? = null
     var pointsList = mutableListOf<CustomPoint>()
     var savedCameraPosition: CameraPosition = CameraPosition(
        Point(56.0102141,92.8678746),
        15.0f,
        0.0f,
        0.0f
    )
    fun initialize(context: Context) {
        if (!isInitialized) {
            MapKitFactory.setApiKey("02aa03fd-6392-43a4-a11e-bd7fd1fd3389")
            MapKitFactory.setLocale("ru_RU")

            MapKitFactory.initialize(context)
            mapView = MapView(context)
            isInitialized = true

        }
    }
}


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView
    companion object {
        lateinit var database: AppDatabase
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapManager.initialize(this)
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "my-database").build()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        mapView = findViewById(R.id.mapView)
        // Настройка BottomNavigationView
        binding.navView.setOnItemSelectedListener  { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_home) {
                        navController.navigate(R.id.navigation_home)
                        true
                    } else {
                        mapView = findViewById(R.id.mapView)
                        val currentCameraPosition = mapView.getMapWindow().getMap().cameraPosition
                        MapManager.savedCameraPosition = currentCameraPosition
                        val bundle = Bundle().apply {
                            putString("map", "1")
                        }
                        navController.navigate(R.id.navigation_home, bundle)
                        true
                    }
                }
                R.id.navigation_records -> {
                    navController.navigate(R.id.navigation_records)
                    true
                }
                else -> false
            }
        }
        navController.navigate(R.id.navigation_home)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
