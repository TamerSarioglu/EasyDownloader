package com.tamersarioglu.easydownloader.domain.usecase.video

data class SubmitVideoParams(
    val url: String
)

data class GetVideoStatusParams(
    val videoId: String
)

object GetUserVideosParams