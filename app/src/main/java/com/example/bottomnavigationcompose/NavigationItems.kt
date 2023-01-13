package com.example.bottomnavigationcompose

sealed class NavigationItems(var route: String, var icon: Int, var title: String) {

    object Home : NavigationItems("home", R.drawable.baseline_home_24, "Home")
    object Map : NavigationItems("map", R.drawable.baseline_map_24, "Map")
    object Settings : NavigationItems("settings", R.drawable.baseline_settings_24, "Settings")
    object Profile : NavigationItems("profile", R.drawable.baseline_profile_circle_24, "Profile")

}