package com.example.fusedlocation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.fusedlocation.ui.theme.FusedLocationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityRecognition = ActivityRecognition(applicationContext)
        activityRecognition.initRecognition(this.lifecycleScope)

        setContent {
            FusedLocationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                    //FusedLocationProvider(applicationContext)
                    //startActivityRecogonition(applicationContext)

                    var statusValue by remember { mutableStateOf("Initialized value") }
                    ShowStatus(statusValue)

                    Text(
                        text = statusValue,
                        color = Color.Red,
                        modifier = Modifier.padding(25.dp)
                    )

//                    LaunchedEffect(statusValue) {
//                        ShowStatus(statusValue)
//                    }

//                    LaunchedEffect(Unit) {
//                        lifecycleScope.launch(Dispatchers.IO) {
//                            while (true) {
//                                delay(3000)
//                                statusValue = activityRecognition.latestStatus
//                                //println("#### from UI")
//                            }
//                        }
//                    }
                    LaunchedEffect(Unit) {
                        lifecycleScope.launch {
                            activityRecognition.updateStatus().collect {
                                statusValue = it
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

fun FusedLocationProvider(context: Context) {
    val provider = LocationProvider(context)
    provider.startLocationUpdate()
}

fun startActivityRecogonition(context: Context) {

}

@Composable
fun ShowStatus(status: String) {

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FusedLocationTheme {
        Greeting("Android")
    }
}