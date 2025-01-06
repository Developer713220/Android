package com.example.voiceassistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*

class MainActivity : Activity() {
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // درخواست مجوز برای دسترسی به حافظه خارجی
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        // Initialize TextToSpeech
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = tts.setLanguage(Locale("fa"))
                if (langResult == TextToSpeech.LANG_MISSING_DATA
                    || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "زبان فارسی پشتیبانی نمی‌شود.", Toast.LENGTH_SHORT).show()
                } else {
                    speak("دستیار صوتی آماده است.")
                }
            } else {
                Toast.makeText(this, "TextToSpeech initialization failed.", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Toast.makeText(this@MainActivity, "خطا در شناسایی صدا", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val command = matches[0]
                    handleCommand(command)
                    startListening() // دوباره شروع به گوش دادن
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        startListening()
    }

    private fun speak(message: String) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun handleCommand(command: String) {
        when {
            command.contains("پژو") -> speak("بله")
            command.contains("تو چی هستی") -> speak("من یک مدل از الگوریتم‌های برنامه‌نویسی هستم که توسط شرکت ایران‌خودرو توسعه داده شدم. من اینجام که در رانندگی کمک شما باشم.")
            command.contains("جُک بگو") -> speak("داشتم غصه می خوردم توش مو بود دیگه نخوردم هاهاهاهاها")
            command.contains("به علیرضا سلام کن") -> speak("سلام آقا علیرضا حال شما خوبی آقا شرمنده نشناختم این روزا سرم خیلی شلوغ دیگه هوش و حواس برام نمونده اتفاق دیروز صحبت شما بود با الکسا و سیری رفته بودیم یتیمچه بزنیم که سیری تراشکاری میخواستم فرستادمش پیش شما حالا نمی‌دونم اومد یا نه ؟")
            command.contains("پخش موزیک") -> playMusic()
        }
    }
    private fun startListening() {
        val intent = Intent(Intent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(Intent.EXTRA_LANGUAGE_MODEL, Intent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(Intent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer.startListening(intent)
    }

    private fun playMusic() {
        mediaPlayer?.release()  // آزادسازی MediaPlayer قبلی در صورت وجود

        val musicPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath + "/sample_music.mp3"
        val musicFile = File(musicPath)
        if (musicFile.exists()) {
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(musicPath)
                    prepare()
                    start()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "خطا در پخش موزیک: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "فایل موسیقی پیدا نشد.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        speechRecognizer.destroy()
        mediaPlayer?.release()
        super.onDestroy()
    }
}