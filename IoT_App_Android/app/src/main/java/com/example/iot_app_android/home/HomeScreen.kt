package com.example.iot_app_android.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.iot_app_android.ui.theme.IoT_App_AndroidTheme

@Composable
fun HomeScreen() {
    Text("Home View")
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    IoT_App_AndroidTheme {
        HomeScreen()
    }
}