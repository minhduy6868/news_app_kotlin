package com.gk.news_pro.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface HeyGenApiService {
    @POST("v2/video/generate")
    suspend fun createVideo(
        @Header("x-api-key") apiKey: String,
        @Body request: HeyGenVideoRequest
    ): HeyGenVideoResponse

    @GET("v1/video_status.get")
    suspend fun getVideoStatus(
        @Header("x-api-key") apiKey: String,
        @Query("video_id") videoId: String
    ): HeyGenVideoStatusResponse
}

data class HeyGenVideoRequest(
    @SerializedName("caption") val caption: Boolean = false,
    @SerializedName("title") val title: String? = null,
    @SerializedName("callback_id") val callbackId: String? = null,
    @SerializedName("video_inputs") val videoInputs: List<VideoInput>,
    @SerializedName("dimension") val dimension: CharacterSettings.Dimension? = null,
    @SerializedName("folder_id") val folderId: String? = null,
    @SerializedName("callback_url") val callbackUrl: String? = null
)

data class HeyGenVideoResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: VideoData?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: ErrorResponse?
)

data class HeyGenVideoStatusResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: VideoStatusData?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: ErrorResponse?
)

data class VideoData(
    @SerializedName("video_id") val videoId: String?
)

data class VideoStatusData(
    @SerializedName("video_id") val videoId: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("duration") val duration: Double?,
    @SerializedName("caption_url") val captionUrl: String?,
    @SerializedName("gif_url") val gifUrl: String?
)

data class ErrorResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?
)

data class VideoInput(
    @SerializedName("character") val character: CharacterSettings?,
    @SerializedName("voice") val voice: CharacterSettings.VoiceSettings,
    @SerializedName("background") val background: CharacterSettings.BackgroundSettings? = null
)

sealed class CharacterSettings {

    data class AvatarSettings(
        @SerializedName("type") val type: String = "avatar",
        @SerializedName("avatar_id") val avatarId: String,
        @SerializedName("scale") val scale: Float = 1.0f,
        @SerializedName("avatar_style") val avatarStyle: String? = null,
        @SerializedName("offset") val offset: Offset = Offset(),
        @SerializedName("matting") val matting: Boolean = false,
        @SerializedName("circle_background_color") val circleBackgroundColor: String? = null
    ) : CharacterSettings()

    data class TalkingPhotoSettings(
        @SerializedName("type") val type: String = "talking_photo",
        @SerializedName("talking_photo_id") val talkingPhotoId: String,
        @SerializedName("scale") val scale: Float = 1.0f,
        @SerializedName("talking_photo_style") val talkingPhotoStyle: TACropStyle? = null,
        @SerializedName("offset") val offset: Offset = Offset(),
        @SerializedName("talking_style") val talkingStyle: TPExpression = TPExpression.STABLE,
        @SerializedName("expression") val expression: TPExpressionStyle = TPExpressionStyle.DEFAULT,
        @SerializedName("super_resolution") val superResolution: Boolean = false,
        @SerializedName("matting") val matting: Boolean = false,
        @SerializedName("circle_background_color") val circleBackgroundColor: String? = null
    ) : CharacterSettings()

    data class Offset(
        @SerializedName("x") val x: Float = 0.0f,
        @SerializedName("y") val y: Float = 0.0f
    )

    enum class TACropStyle {
        SQUARE, CIRCLE
    }

    enum class TPExpression {
        STABLE, EXPRESSIVE
    }

    enum class TPExpressionStyle {
        DEFAULT, HAPPY
    }

    sealed class VoiceSettings

    data class TextVoiceSettings(
        @SerializedName("type") val type: String = "text",
        @SerializedName("voice_id") val voiceId: String,
        @SerializedName("input_text") val text: String
    ) : VoiceSettings()

    data class AudioVoiceSettings(
        @SerializedName("type") val type: String = "audio",
        @SerializedName("audio_url") val audioUrl: String
    ) : VoiceSettings()

    data class SilenceVoiceSettings(
        @SerializedName("type") val type: String = "silence",
        @SerializedName("duration") val duration: Int
    ) : VoiceSettings()

    sealed class BackgroundSettings

    data class ColorBackground(
        @SerializedName("type") val type: String = "color",
        @SerializedName("value") val value: String
    ) : BackgroundSettings()

    data class ImageBackground(
        @SerializedName("type") val type: String = "image",
        @SerializedName("url") val url: String
    ) : BackgroundSettings()

    data class VideoBackground(
        @SerializedName("type") val type: String = "video",
        @SerializedName("url") val url: String
    ) : BackgroundSettings()

    data class Dimension(
        @SerializedName("width") val width: Int,
        @SerializedName("height") val height: Int
    )
}