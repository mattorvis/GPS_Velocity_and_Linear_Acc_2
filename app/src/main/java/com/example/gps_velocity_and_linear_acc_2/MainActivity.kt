package com.example.gps_velocity_and_linear_acc_2

//Base imports
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
//Accelerometer imports
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
//GPS Imports
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
//Math Imports
import kotlin.math.sqrt
//Misc Imports
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager

    private var velocity: FloatArray = FloatArray(3)
    private var velocityMag: Float = 0f
    //private var alpha = 0.8f
    private var lastUpdateTime: Long = 0
    private var dt: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the location and sensor managers
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Request location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Register sensor listener for linear acceleration
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL)

        // Initialize vectors to zero
        velocity = floatArrayOf(0f, 0f, 0f)
        velocityMag = 0f
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Update the GPS location and velocity TextViews
            val locationTextView = findViewById<TextView>(R.id.location_textview)
            locationTextView.text = "GPS Position: \n\t" +
                    "Latitude: ${location.latitude} deg\n\t" +
                    "Longitude: ${location.longitude} deg\n\t" +
                    "Altitude: ${location.altitude} m\n\t"

            val velocityTextView = findViewById<TextView>(R.id.velocity_textview)
            velocityTextView.text = "GPS Speed: ${location.speed} m/s\n" +
                    "GPS Bearing: ${location.bearing} deg CW from N"
        }
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                val decimalPlaces = 3
                // Update the linear acceleration TextView
                val linearAccelerationTextView = findViewById<TextView>(R.id.linear_acceleration_textview)
                linearAccelerationTextView.text = "Linear acceleration:\n\t" +
                        "X: ${if (event.values[0] >= 0) " " else ""}%.${decimalPlaces}f\n\t".format(event.values[0]) +
                        "Y: ${if (event.values[1] >= 0) " " else ""}%.${decimalPlaces}f\n\t".format(event.values[1]) +
                        "Z: ${if (event.values[2] >= 0) " " else ""}%.${decimalPlaces}f".format(event.values[2])

                // Calculate and display linear acceleration magnitude
                val accMag = sqrt(event.values[0]*event.values[0] +
                        event.values[1]*event.values[1] +
                        event.values[2]*event.values[2])
                val linearAccelerationMagTextView = findViewById<TextView>(R.id.linear_acceleration_mag_textview)
                linearAccelerationMagTextView.text = "Acceleration Magnitude: %.${decimalPlaces}f  m/s^2".format(accMag)

                // Calculate velocity from accelerometer data
                val currentTime = System.currentTimeMillis()
                dt = (currentTime - lastUpdateTime) /1f
                if (dt >= 20) {
                    velocity[0] += event.values[0] * dt / 1000
                    velocity[1] += event.values[1] * dt / 1000
                    velocity[2] += event.values[2] * dt / 1000
                    velocityMag = sqrt(velocity[0]*velocity[0] +
                            velocity[1]*velocity[1] +
                            velocity[2]*velocity[2]) * dt / 1000
                    val accelerometerVelocityTextView = findViewById<TextView>(R.id.accelerometer_velocity_textview)
                    accelerometerVelocityTextView.text = "Accelerometer Velocity: %.${decimalPlaces}f m/s".format(velocityMag)
                    lastUpdateTime = currentTime
                }

            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        sensorManager.unregisterListener(sensorEventListener)
    }
}
