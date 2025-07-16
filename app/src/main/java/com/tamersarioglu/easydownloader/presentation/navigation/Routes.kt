package com.tamersarioglu.easydownloader.presentation.navigation

object Routes {
    // Navigation graphs
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    
    // Authentication screens
    const val REGISTRATION = "registration"
    const val LOGIN = "login"
    
    // Main application screens
    const val VIDEO_SUBMISSION = "video_submission"
    const val VIDEO_LIST = "video_list"
    const val VIDEO_DETAIL = "video_detail/{videoId}"
    const val SETTINGS = "settings"
    const val PROFILE = "profile" // Alias for settings screen
    
    // Helper function for video detail with parameter
    fun videoDetail(videoId: String) = "video_detail/$videoId"
}