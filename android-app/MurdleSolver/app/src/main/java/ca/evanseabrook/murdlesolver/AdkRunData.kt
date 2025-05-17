package ca.evanseabrook.murdlesolver
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import android.util.Base64

const val mimeType = "image/jpeg"
const val adkAppName = "murdle_solver_agent"
const val messageRole = "user"

@OptIn(ExperimentalUuidApi::class)
val adkUserId = Uuid.random().toString()

@OptIn(ExperimentalUuidApi::class)
val adkSessionId = Uuid.random().toString()

@JsonClass(generateAdapter = true)
data class InlineData (
    @Json(name = "mimeType") val dataMimeType: String = mimeType,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class Part (
    @Json(name = "inlineData") val inlineData: InlineData? = null,
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class NewMessage (
    @Json(name = "role") val role: String = messageRole,
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class AdkRunRequest (
    @Json(name = "app_name") val appName: String = adkAppName,
    @Json(name = "user_id") val userId: String = adkUserId,
    @Json(name = "session_id") val sessionId: String = adkSessionId,
    @Json(name = "new_message") val newMessage: NewMessage
)

fun createAdkRunRequest(data: ByteArray): AdkRunRequest {
    val base64EncodedImage: String = Base64.encodeToString(data, Base64.NO_WRAP)

    val inlineData = InlineData(data = base64EncodedImage)
    val textPart = Part(text = "Decode this image")
    val part = Part(inlineData = inlineData)
    val newMessage = NewMessage(parts = listOf(textPart, part))

    return AdkRunRequest(newMessage = newMessage)

}