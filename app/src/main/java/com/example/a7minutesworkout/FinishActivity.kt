package com.example.a7minutesworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.a7minutesworkout.databinding.ActivityFinishBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FinishActivity : AppCompatActivity() {

    private var binding:ActivityFinishBinding?=null

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityFinishBinding.inflate(layoutInflater)
        setContentView(binding?.root)

//        tts = TextToSpeech(this,this)

        setSupportActionBar(binding?.toolbarFinishActivity)

//        speakOut("Congratulations! You have completed the 7 minutes workout")

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding?.toolbarFinishActivity?.setNavigationOnClickListener {
            onBackPressed()
        }

        binding?.btnFinish?.setOnClickListener {
            finish()
        }

        val historyDAO=(application as WorkoutApp).db.historyDao()
        addDateToDatabase(historyDAO)
    }
//    private fun speakOut(text: String) {
//        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
//    }
//
//    override fun onInit(status: Int) {
//        if (status == TextToSpeech.SUCCESS) {
//            //todo try different languages
//            val result = tts!!.setLanguage(Locale.US)
//
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TTS", "The language specified is not supported!")
//            }
//        } else {
//            Log.e("TTS", "Initialization Failed!")
//        }
//    }

    private fun addDateToDatabase(historyDAO: HistoryDAO){

        val c=Calendar.getInstance()
        val dateTime=c.time
        Log.e("Date: ",""+dateTime)

        val sdf= SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        val date=sdf.format(dateTime)
        Log.e("Formatted Date: ",""+date)

        lifecycleScope.launch {
            historyDAO.insert(HistoryEntity(date))
            Log.e("Date: ","Added...")
        }

    }

}