package com.example.lab1_hw2_mobile_23521836

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var rootLayout: ConstraintLayout? = null
    private var tvAppTitle: TextView? = null
    private var ivSentimentIcon: ImageView? = null
    private var tvSentimentLabel: TextView? = null
    private var etInputText: EditText? = null
    private var btnSubmit: MaterialButton? = null
    private var progressBar: ProgressBar? = null
    private var cardInput: MaterialCardView? = null
    private var cardResult: MaterialCardView? = null

    private val httpClient: OkHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootLayout = findViewById(R.id.rootLayout)
        tvAppTitle = findViewById(R.id.tvAppTitle)
        ivSentimentIcon = findViewById(R.id.ivSentimentIcon)
        tvSentimentLabel = findViewById(R.id.tvSentimentLabel)
        etInputText = findViewById(R.id.etInputText)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBar)
        cardInput = findViewById(R.id.cardInput)
        cardResult = findViewById(R.id.cardResult)

        btnSubmit?.setOnClickListener {
            val input = etInputText?.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter a sentence.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            hideKeyboard()
            analyzeSentiment(input)
        }
    }

    private fun analyzeSentiment(sentence: String) {
        showLoading(true)

        val prompt = "Classify the sentiment of the following sentence as exactly one word: POSITIVE, NEGATIVE, or NEUTRAL. Reply with only that single word, nothing else. Sentence: \"$sentence\""

        val requestBodyJson = buildRequestBody(prompt)
        if (requestBodyJson == null) {
            showLoading(false)
            Toast.makeText(this, "Failed to build request.", Toast.LENGTH_SHORT).show()
            return
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

        val request: Request = Request.Builder()
            .url(ENDPOINT)
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GeminiAPI", "Network error", e)
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this@MainActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                Log.d("GeminiAPI", "Response Body: $body")
                runOnUiThread {
                    showLoading(false)
                    if (response.isSuccessful) {
                        handleApiResponse(body)
                    } else {
                        val errorMsg = try {
                            val json = JSONObject(body)
                            json.getJSONObject("error").getString("message")
                        } catch (e: Exception) {
                            "API Error: ${response.code} - ${response.message}"
                        }
                        Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                        Log.e("GeminiAPI", "Error Code: ${response.code}, Message: $errorMsg")
                    }
                }
            }
        })
    }

    private fun handleApiResponse(responseBody: String) {
        try {
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val msg = if (json.has("error")) {
                    json.getJSONObject("error").getString("message")
                } else if (json.has("promptFeedback")) {
                    "Content blocked by safety filters."
                } else {
                    "No candidates found in AI response."
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                return
            }
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val result = parts.getJSONObject(0).getString("text").trim().uppercase(Locale.getDefault())

            if (result.contains("POSITIVE")) applyTheme("POSITIVE")
            else if (result.contains("NEGATIVE")) applyTheme("NEGATIVE")
            else applyTheme("NEUTRAL")

        } catch (e: Exception) {
            Log.e("GeminiAPI", "Parse error", e)
            Toast.makeText(this, "Parse error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyTheme(sentiment: String) {
        val bgColor: Int
        val primaryColor: Int
        val label: String

        when (sentiment) {
            "POSITIVE" -> {
                bgColor = Color.parseColor("#E8F5E9")
                primaryColor = Color.parseColor("#4CAF50")
                label = "Positive"
            }
            "NEGATIVE" -> {
                bgColor = Color.parseColor("#FFEBEE")
                primaryColor = Color.parseColor("#E91E63")
                label = "Negative"
            }
            else -> {
                bgColor = Color.parseColor("#E3F2FD")
                primaryColor = Color.parseColor("#2196F3")
                label = "Neutral"
            }
        }

        rootLayout?.setBackgroundColor(bgColor)
        tvAppTitle?.setTextColor(primaryColor)
        ivSentimentIcon?.imageTintList = ColorStateList.valueOf(primaryColor)
        tvSentimentLabel?.text = label
        tvSentimentLabel?.setTextColor(primaryColor)
        btnSubmit?.backgroundTintList = ColorStateList.valueOf(primaryColor)
    }

    private fun buildRequestBody(prompt: String): JSONObject? {
        return try {
            val part = JSONObject().put("text", prompt)
            val parts = JSONArray().put(part)
            val content = JSONObject().put("parts", parts)
            val contents = JSONArray().put(content)
            JSONObject().put("contents", contents)
        } catch (e: Exception) {
            null
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
        ivSentimentIcon?.visibility = if (show) View.GONE else View.VISIBLE
        tvSentimentLabel?.visibility = if (show) View.GONE else View.VISIBLE
        btnSubmit?.isEnabled = !show
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    companion object {
        private const val API_KEY = "AIzaSyCSlGotB6cXcIiAw6qTIrocaJv7PDQyAtg"
        // Switched to gemini-1.5-flash as it is more widely compatible across regions/tiers
        private const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$API_KEY"
    }
}
