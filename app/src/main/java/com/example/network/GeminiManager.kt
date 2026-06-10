package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Moshi-compatible data structures for Gemini API ---
@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

// --- Retrofit Setup ---
interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

// --- High-level Gemini Assistant ---
object GeminiManager {

    suspend fun draftBiography(
        name: String,
        birthDate: String,
        deathDate: String?,
        occupation: String?,
        location: String?,
        seedBio: String,
        relationshipsSummary: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please configure your GEMINI_API_KEY in the Secrets panel."
        }

        val prompt = """
            Draft a warm, beautiful, and historical biographical narrative for my ancestor:
            - Name: $name
            - Date of Birth: $birthDate
            - Date of Death: ${deathDate ?: "Still Living or Unknown"}
            - Occupation: ${occupation ?: "Not Specified"}
            - Key Locations: ${location ?: "Not Specified"}
            - Current Brief Notes: $seedBio
            - Known Family Connections: $relationshipsSummary
            
            Synthesize these details into 3 cohesive, respectful, flowing paragraphs that read like a historical legacy record. Speak in third-person, highlight triumphs & historical times, and keep it engaging.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "You are an expert family genealogist and master historical biographer.")))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No biographical draft could be generated. Please try again."
        } catch (e: Exception) {
            "Biographer Error: ${e.message}"
        }
    }

    suspend fun analyzeTreeRelationships(
        subjectName: String,
        treeSummaryJson: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please configure your GEMINI_API_KEY in the Secrets panel."
        }

        val prompt = """
            Analyze the following family tree relationships for the key individual '$subjectName'.
            Family Tree Context:
            $treeSummaryJson
            
            Based on this genealogy configuration:
            1. Identify close and extended relationship connections.
            2. Suggest potential missing links, ancestors, or descendants we should explore next, based on generational timelines.
            3. Highlight any timeline anomalies (e.g., birth dates occurring after death dates, parents born too close to children, etc.).
            
            Provide an elegant, encouraging summary with markdown bullet points. Keep it clear, concise, and helpful.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "You are an expert AI genealogy analyst, detecting inconsistencies and proposing fruitful lines of genealogical discovery.")))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Relationship analysis not available at the moment."
        } catch (e: Exception) {
            "Analyzer Error: ${e.message}"
        }
    }

    suspend fun generateFamilySaga(
        familyName: String,
        allStoriesJson: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Please configure your GEMINI_API_KEY in the Secrets panel."
        }

        val prompt = """
            Create a unified historical family saga narrative for the '$familyName' family, weaving together these separate events and stories:
            
            $allStoriesJson
            
            Integrate these historical entries into a beautiful, chronological narrative epic. Highlight how the family adapted through generations, migrating or building their legado over time. Structure it with clear generation-based subheadings and ensure it flows like a published historical book.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "You are an epic novelist and family heritage preservation historian.")))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Family saga could not be synthesized at this time."
        } catch (e: Exception) {
            "Saga Synthesizer Error: ${e.message}"
        }
    }
}
