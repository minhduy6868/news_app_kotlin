import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class StringHelper {
    suspend fun uploadImageToCloudinary(imageFile: File): String? {
        val cloudName = "dwrmsia23"
        val uploadPreset = "alo123"
        val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

        return try {
            withContext(Dispatchers.IO) {
                val fileRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())

                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", imageFile.name, fileRequestBody)
                    .addFormDataPart("upload_preset", uploadPreset) // Phải ở sau phần file
                    .build()

                val request = Request.Builder()
                    .url(uploadUrl)
                    .post(multipartBody)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    json.getString("secure_url")
                } else {
                    println("Upload failed: ${response.code}")
                    println("Response body: $responseBody")
                    throw Exception("Image upload failed: Could not retrieve secure URL.")
                }
            }
        } catch (e: Exception) {
            println("Exception in uploadImageToCloudinary: ${e.message}")
            null
        }
    }
}
