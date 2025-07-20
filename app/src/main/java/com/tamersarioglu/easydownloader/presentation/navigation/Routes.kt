package com.tamersarioglu.easydownloader.presentation.navigation

object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    
    const val REGISTRATION = "registration"
    const val LOGIN = "login"
    
    const val VIDEO_SUBMISSION = "video_submission"
    const val VIDEO_LIST = "video_list"
    const val VIDEO_DETAIL = "video_detail/{videoId}"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    
    fun videoDetail(videoId: String) = "video_detail/$videoId"
}