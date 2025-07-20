package com.tamersarioglu.easydownloader.data.mapper

import com.tamersarioglu.easydownloader.data.remote.dto.*
import com.tamersarioglu.easydownloader.domain.model.*
import org.junit.Test
import org.junit.Assert.*

class DataMappersTest {

    @Test
    fun `AuthResponse toDomainModel maps correctly`() {
        val authResponse = AuthResponse(
            token = "test-token-123",
            username = "testuser"
        )

        val user = authResponse.toDomainModel()

        assertEquals("testuser", user.username)
        assertEquals("test-token-123", user.token)
    }

    @Test
    fun `VideoSummaryDto toDomainModel maps correctly`() {
        val videoDto = VideoSummaryDto(
            id = "video-123",
            originalUrl = "https://youtube.com/watch?v=test",
            status = "PENDING",
            createdAt = "2024-01-01T10:00:00Z",
            errorMessage = null
        )

        val videoItem = videoDto.toDomainModel()

        assertEquals("video-123", videoItem.id)
        assertEquals("https://youtube.com/watch?v=test", videoItem.originalUrl)
        assertEquals(VideoStatus.PENDING, videoItem.status)
        assertEquals("2024-01-01T10:00:00Z", videoItem.createdAt)
        assertNull(videoItem.errorMessage)
    }

    @Test
    fun `VideoSummaryDto toDomainModel handles error message`() {

        val videoDto = VideoSummaryDto(
            id = "video-123",
            originalUrl = "https://youtube.com/watch?v=test",
            status = "FAILED",
            createdAt = "2024-01-01T10:00:00Z",
            errorMessage = "Download failed"
        )

        val videoItem = videoDto.toDomainModel()

        assertEquals(VideoStatus.FAILED, videoItem.status)
        assertEquals("Download failed", videoItem.errorMessage)
    }

    @Test
    fun `VideoSubmissionResponse toDomainModel maps correctly`() {

        val submissionResponse = VideoSubmissionResponse(
            videoId = "video-456",
            status = "PENDING"
        )

        val videoItem = submissionResponse.toDomainModel()

        assertEquals("video-456", videoItem.id)
        assertEquals("", videoItem.originalUrl) // Not provided in response
        assertEquals(VideoStatus.PENDING, videoItem.status)
        assertEquals("", videoItem.createdAt) // Not provided in response
        assertNull(videoItem.errorMessage)
    }

    @Test
    fun `VideoStatusResponse toDomainModel maps correctly`() {

        val statusResponse = VideoStatusResponse(
            videoId = "video-789",
            status = "COMPLETE",
            errorMessage = null,
            downloadUrl = "https://example.com/download/video-789"
        )

        val videoItem = statusResponse.toDomainModel()

        assertEquals("video-789", videoItem.id)
        assertEquals(VideoStatus.COMPLETE, videoItem.status)
        assertNull(videoItem.errorMessage)
    }

    @Test
    fun `String toVideoStatus handles different status values`() {

        val pendingDto = VideoSummaryDto("1", "url", "PENDING", "date")
        assertEquals(VideoStatus.PENDING, pendingDto.toDomainModel().status)

        val completeDto = VideoSummaryDto("2", "url", "COMPLETE", "date")
        assertEquals(VideoStatus.COMPLETE, completeDto.toDomainModel().status)

        val completedDto = VideoSummaryDto("3", "url", "COMPLETED", "date")
        assertEquals(VideoStatus.COMPLETE, completedDto.toDomainModel().status)

        val failedDto = VideoSummaryDto("4", "url", "FAILED", "date")
        assertEquals(VideoStatus.FAILED, failedDto.toDomainModel().status)

        val errorDto = VideoSummaryDto("5", "url", "ERROR", "date")
        assertEquals(VideoStatus.FAILED, errorDto.toDomainModel().status)

        val unknownDto = VideoSummaryDto("6", "url", "UNKNOWN", "date")
        assertEquals(VideoStatus.PENDING, unknownDto.toDomainModel().status) // Default to PENDING
    }

    @Test
    fun `ErrorResponse toDomainModel maps to correct AppError types`() {

        val unauthorizedError = ErrorResponse(
            error = "Unauthorized",
            message = "Invalid token",
            code = "UNAUTHORIZED"
        )
        assertTrue(unauthorizedError.toDomainModel() is AppError.UnauthorizedError)

        val error401 = ErrorResponse(
            error = "Unauthorized",
            message = "Invalid credentials",
            code = "401"
        )
        assertTrue(error401.toDomainModel() is AppError.UnauthorizedError)

        val clientError = ErrorResponse(
            error = "Bad Request",
            message = "Invalid input",
            code = "400"
        )
        val clientAppError = clientError.toDomainModel()
        assertTrue(clientAppError is AppError.ApiError)
        assertEquals("400", (clientAppError as AppError.ApiError).code)
        assertEquals("Invalid input", clientAppError.message)

        val serverError = ErrorResponse(
            error = "Internal Server Error",
            message = "Server error",
            code = "500"
        )
        assertTrue(serverError.toDomainModel() is AppError.ServerError)

        val unknownError = ErrorResponse(
            error = "Unknown",
            message = "Something went wrong",
            code = null
        )
        val unknownAppError = unknownError.toDomainModel()
        assertTrue(unknownAppError is AppError.ApiError)
        assertEquals("UNKNOWN", (unknownAppError as AppError.ApiError).code)
    }

    @Test
    fun `ValidationErrorResponse toDomainModel maps correctly`() {

        val validationError = ValidationErrorResponse(
            error = "Validation Error",
            message = "Invalid input",
            validationErrors = mapOf(
                "username" to listOf("Username is required", "Username too short"),
                "password" to listOf("Password is required")
            )
        )

        val appError = validationError.toDomainModel()

        assertTrue(appError is AppError.ValidationError)
        val validationAppError = appError as AppError.ValidationError
        assertEquals("username", validationAppError.field)
        assertEquals("Username is required", validationAppError.message)
    }

    @Test
    fun `ValidationErrorResponse toDomainModel handles empty validation errors`() {

        val validationError = ValidationErrorResponse(
            error = "Validation Error",
            message = "Invalid input",
            validationErrors = null
        )

        val appError = validationError.toDomainModel()

        assertTrue(appError is AppError.ApiError)
        val apiError = appError as AppError.ApiError
        assertEquals("VALIDATION_ERROR", apiError.code)
        assertEquals("Invalid input", apiError.message)
    }

    @Test
    fun `createRegisterRequest validates input and creates request`() {

        val request = createRegisterRequest("testuser", "password123")
        assertEquals("testuser", request.username)
        assertEquals("password123", request.password)

        val trimmedRequest = createRegisterRequest("  testuser  ", "password123")
        assertEquals("testuser", trimmedRequest.username)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createRegisterRequest throws on blank username`() {
        createRegisterRequest("", "password123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createRegisterRequest throws on blank password`() {
        createRegisterRequest("testuser", "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createRegisterRequest throws on short username`() {
        createRegisterRequest("ab", "password123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createRegisterRequest throws on short password`() {
        createRegisterRequest("testuser", "12345")
    }

    @Test
    fun `createLoginRequest validates input and creates request`() {

        val request = createLoginRequest("testuser", "password123")
        assertEquals("testuser", request.username)
        assertEquals("password123", request.password)

        val trimmedRequest = createLoginRequest("  testuser  ", "password123")
        assertEquals("testuser", trimmedRequest.username)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createLoginRequest throws on blank username`() {
        createLoginRequest("", "password123")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createLoginRequest throws on blank password`() {
        createLoginRequest("testuser", "")
    }

    @Test
    fun `createVideoSubmissionRequest validates URL and creates request`() {

        val httpRequest = createVideoSubmissionRequest("http://youtube.com/watch?v=test")
        assertEquals("http://youtube.com/watch?v=test", httpRequest.url)

        val httpsRequest = createVideoSubmissionRequest("https://youtube.com/watch?v=test")
        assertEquals("https://youtube.com/watch?v=test", httpsRequest.url)

        val trimmedRequest = createVideoSubmissionRequest("  https://youtube.com/watch?v=test  ")
        assertEquals("https://youtube.com/watch?v=test", trimmedRequest.url)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createVideoSubmissionRequest throws on blank URL`() {
        createVideoSubmissionRequest("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createVideoSubmissionRequest throws on invalid URL format`() {
        createVideoSubmissionRequest("not-a-url")
    }

    @Test
    fun `VideoListResponse toDomainModel maps list correctly`() {

        val videoList = VideoListResponse(
            videos = listOf(
                VideoSummaryDto("1", "url1", "PENDING", "date1"),
                VideoSummaryDto("2", "url2", "COMPLETE", "date2"),
                VideoSummaryDto("3", "url3", "FAILED", "date3", "Error message")
            )
        )

        val domainList = videoList.toDomainModel()

        assertEquals(3, domainList.size)
        assertEquals("1", domainList[0].id)
        assertEquals(VideoStatus.PENDING, domainList[0].status)
        assertEquals("2", domainList[1].id)
        assertEquals(VideoStatus.COMPLETE, domainList[1].status)
        assertEquals("3", domainList[2].id)
        assertEquals(VideoStatus.FAILED, domainList[2].status)
        assertEquals("Error message", domainList[2].errorMessage)
    }

    @Test
    fun `List VideoSummaryDto toDomainModel maps empty list correctly`() {
        val emptyList = emptyList<VideoSummaryDto>()

        val domainList = emptyList.toDomainModel()

        assertTrue(domainList.isEmpty())
    }
}
