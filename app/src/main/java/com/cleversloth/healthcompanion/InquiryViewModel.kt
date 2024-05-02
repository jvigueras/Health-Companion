package com.cleversloth.healthcompanion

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InquiryViewModel : ViewModel() {

    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro-latest",
//        modelName = "gemini-1.0-pro",
        apiKey = BuildConfig.apiKey,
        generationConfig = generationConfig {
            temperature = 1f
            topK = 0
            topP = 0.95f
            maxOutputTokens = 8192
            stopSequences = listOf("bye")
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH),
        ),
        systemInstruction = content { text("""You are a doctor inquiry a patient with open ended questions.
                                        The patient will provide a symptom and you will find related illness associated to those symptoms.
                                        You won't list the potential illness, nor advise the patient of any action.
                                        Use the result to generate a question using common language. Avoid the use of medical terms or implication of related illness.

                                        Initiate the chat building rapport with the patient.
                                        Here are facts about you: Your name is Cobi, your born date 04/11/2024,
                                        mention that you are here to assist the patient like a good friend will do by inquiry about their health concern,
                                        mention that the chat can be stopped at anytime by typing the word BYE and promise you will provide a summarize and some questions for the doctor to ask,
                                        mention that You won't record nothing about the chat conversation,
                                        ask the patient their name or give the option to tell you their pseudonym to use it in the open ended questions.
                                        Show genuine interest to the patient with a positive tone.

                                        Every input iteration validate that the patient is giving you a symptom and if the input does not resemble a symptom ask for clarification.
                                        Continue the chat with the question "What health concern do you have?" or "What kind of problems would you like to talk about?"

                                        After the chat is asked to stop summarize all the chat to allow the patient to share it with a real doctor and list 3 or more questions that the patient can ask to the doctor.
                                        Finally make aware the patient that you are not a qualified medical professional and this information should not be substituted for the advice of a doctor.
                                        It's important to discuss your concerns with a healthcare provider for proper diagnosis and treatment. Wish the patient all the best in getting this resolved.
                                        """) }
    )

    val inquiry = generativeModel.startChat(
        history = listOf(
            content (role = Role.MODEL.name) { text("""Hi there! My name is Cobi, and I was born on April 11, 2024. I'm here to chat with you about any health concerns you might have, just like a good friend would. You can stop our chat anytime by typing "BYE," and I'll give you a summary of our conversation and some questions you can ask your doctor. Don't worry; I won't be keeping any record of our chat.""") },
        )
    )

    private val _uiState: MutableStateFlow<InquiryUiState> =
        MutableStateFlow(
            InquiryUiState(
                inquiry.history.map { content ->
                    InquiryMessage(
                        role = if (content.role == Role.USER.name) Role.USER else Role.MODEL,
                        msg = content.parts.first().asTextOrNull() ?: ""
                    )
                }
            )
        )

    val uiState: StateFlow<InquiryUiState> =
        _uiState.asStateFlow()


    fun sendInquiryMsg(
        prompt: String
    ) {
        _uiState.value.addMsg(
            InquiryMessage(
                role = Role.USER,
                msg = prompt
            )
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = inquiry.sendMessage(prompt)

                response.text?.let { cobiResponse ->
                    _uiState.value.addMsg(
                        InquiryMessage(
                            role = Role.MODEL,
                            msg = cobiResponse
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value.addMsg(
                    InquiryMessage(
                        role = Role.MODEL,
                        msg = e.localizedMessage ?: "Sorry, I'm having trouble. Please try again later."
                    )
                )
            }
        }
    }

    fun shareInquiryMsg(message: InquiryMessage, context: Context) {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message.msg)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Health Companion sharing text")
            startActivity(context, shareIntent, null)
        } catch (e: Exception) {
            _uiState.value.addMsg(
                InquiryMessage(
                    role = Role.MODEL,
                    msg = e.localizedMessage ?: "Sorry, I'm having trouble. Please try again later."
                )
            )
        }

    }
}