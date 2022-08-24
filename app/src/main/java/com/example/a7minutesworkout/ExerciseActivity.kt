package com.example.a7minutesworkout

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a7minutesworkout.databinding.ActivityExerciseBinding
import com.example.a7minutesworkout.databinding.DialogCustomBackConfirmationBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var restTimer: CountDownTimer? = null
    private var restProgress = 0
    private var restTimerDuration:Long=10

    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0
    private var exerciseTimerDuration:Long=30

    private var binding:ActivityExerciseBinding? = null

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null

    private var exerciseAdapter:ExerciseStatusAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarExercise)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        exerciseList = Constants.defaultExerciseList()

        tts = TextToSpeech(this, this)

        binding?.toolbarExercise?.setNavigationOnClickListener {
//            onBackPressed()
            customDialogForBackButton()
        }
//        binding?.flProgressBar?.visibility= View.INVISIBLE
        setUpRestView()
        setUpExerciseStatusRecyclerView()

    }

    private fun setUpExerciseStatusRecyclerView(){
        binding?.rvExerciseStatus?.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        exerciseAdapter= ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter=exerciseAdapter
    }

    private fun setUpRestView() {

        try {
//            val soundURI = Uri.parse(
//                "android.resource://com.example.a7minutesworkout" + R.raw.press_start
//            )
            player = MediaPlayer.create(applicationContext, R.raw.press_start)
            player?.isLooping = false
            player?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvExerciseName?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.tvUpcomingLabel?.visibility = View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.VISIBLE
        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }
        binding?.tvUpcomingExerciseName?.text =
            exerciseList!![currentExercisePosition + 1].getName()
        setRestProgressBar()
    }

    private fun setUpExerciseView() {

        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE
        binding?.tvExerciseName?.visibility = View.VISIBLE
        binding?.flExerciseView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        binding?.tvUpcomingLabel?.visibility = View.INVISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.INVISIBLE
        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        speakOut(exerciseList!![currentExercisePosition].getName())

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExerciseName?.text = exerciseList!![currentExercisePosition].getName()


        setExerciseProgressBar()
    }

    private fun setRestProgressBar() {
        binding?.progressBar?.progress = restProgress

        restTimer = object : CountDownTimer(restTimerDuration*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10 - restProgress).toString()
            }

            override fun onFinish() {
//                Toast.makeText(
//                    this@ExerciseActivity,
//                    "Here now we will start the exercise",
//                    Toast.LENGTH_SHORT
//                ).show()
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                //adapter should be notified
                exerciseAdapter!!.notifyDataSetChanged()

                setUpExerciseView()

            }
        }.start()
    }

    private fun setExerciseProgressBar() {
        binding?.progressBarExercise?.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(exerciseTimerDuration*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                binding?.progressBarExercise?.progress = 30 - exerciseProgress
                binding?.tvTimerExercise?.text = (30 - exerciseProgress).toString()
            }

            override fun onFinish() {
//                Toast.makeText(
//                    this@ExerciseActivity,
//                    "30 seconds are over , lets go to the rest view",
//                    Toast.LENGTH_SHORT
//                ).show()

                exerciseList!![currentExercisePosition].setIsSelected(false)
                exerciseList!![currentExercisePosition].setIsCompleted(true)
                //adapter should be notified
                exerciseAdapter!!.notifyDataSetChanged()
                if (currentExercisePosition < exerciseList?.size!! - 1) {
                    setUpRestView()
                } else {
//                    Toast.makeText(
//                        this@ExerciseActivity,
//                        "Congratulations! You have completed the 7 minutes workout",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    //Added by me
//                    speakOut("Congratulations! You have completed the 7 minutes workout")
//                    Timer().schedule(4000) {
//                        finish()
//                    }
                    finish()
                    val intent= Intent(this@ExerciseActivity,FinishActivity::class.java)
                    startActivity(intent)

                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }

        if(player!=null){
            player!!.stop()
        }

        binding = null
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            //todo try different languages
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

//    override fun onBackPressed() {
//        customDialogForBackButton()
////        super.onBackPressed()
//    }

    private fun customDialogForBackButton() {
        val customDialog = Dialog(this)
        val dialogBinding=DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.btnYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()

//        /*Set the screen content from a layout resource.
//    The resource will be inflated, adding all top-level views to the screen.*/
//        customDialog.setContentView(R.layout.dialog_custom_back_confirmation)
//        customDialog.findViewById<TextView>(R.id.tvYes).setOnClickListener {
////            Toast.makeText(applicationContext, "clicked submit", Toast.LENGTH_LONG).show()
//            onBackPressed()
//            customDialog.dismiss() // Dialog will be dismissed
//        }
//        customDialog.findViewById<TextView>(R.id.tvNo).setOnClickListener {
////            Toast.makeText(applicationContext, "clicked cancel", Toast.LENGTH_LONG).show()
//            customDialog.dismiss()
//        }
//        //Start the dialog and display it on screen.
//        customDialog.show()
    }
}