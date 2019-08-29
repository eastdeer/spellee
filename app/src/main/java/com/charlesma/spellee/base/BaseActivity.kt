package com.charlesma.spellee.base

import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import android.content.Context
import android.app.KeyguardManager
import android.view.WindowManager
import com.charlesma.spellee.R
import com.charlesma.spellee.util.AnalyticsUtil
import com.google.firebase.analytics.FirebaseAnalytics


abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    TextToSpeech.OnInitListener {

    companion object {
        const val MY_DATA_CHECK_CODE = 9527
    }

    private var mTts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewStub.layoutResource = getContentLayoutResId()
        viewStub.visibility = View.VISIBLE

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val appBarConf = AppBarConfiguration(
            setOf(
                R.id.dashBoardFragment,
                R.id.navigation_score,
                R.id.navigationCurriculum,
                R.id.navigationWordList,
                R.id.rankFragment
            ), drawer_layout
        )
        val navController = findNavController(R.id.fragment_navihost)
        toolbar.setupWithNavController(navController, appBarConf)

    }

    @LayoutRes
    abstract fun getContentLayoutResId(): Int

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        fragment_navihost.findNavController().popBackStack()

        when (item.itemId) {
            R.id.nav_scores -> fragment_navihost.findNavController().navigate(R.id.action_global_navigationScore)
            R.id.nav_curriculum -> fragment_navihost.findNavController().navigate(R.id.action_global_navigationTest)
            R.id.nav_wordlist -> fragment_navihost.findNavController().navigate(R.id.action_global_navigationWordList)
            R.id.nav_ranks -> findNavController(R.id.fragment_navihost).navigate(R.id.action_dashBoardFragment_to_rankFragment)
            //R.id.nav_replay -> findNavController(R.id.nav_view).navigate(R.id.action_dashBoardFragment_to)
            R.id.nav_feedback -> {
                fragment_navihost.findNavController().navigate(R.id.scoreHistoryListFragment)
            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    protected fun initialTextToSpeech() {
        val checkIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        checkIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        checkIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        checkIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak")

        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //check if permission request is necessary
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                MY_DATA_CHECK_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = TextToSpeech(this, this)
            } else {
                // missing data, install it
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS && mTts != null) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC)
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

            val result = mTts!!.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(applicationContext, "Language not supported", Toast.LENGTH_SHORT)
                    .show()
                onTtsInitFailed()
            } else {
                onTtsInitSuccess()
            }
        } else {
            onTtsInitFailed()
            Toast.makeText(getApplicationContext(), "Init failed", Toast.LENGTH_SHORT).show()
        }
    }

    abstract fun onTtsInitSuccess()
    abstract fun onTtsInitFailed()

    override fun onDestroy() {
        mTts?.let { it.shutdown() }
        super.onDestroy()
    }

    protected fun textToSpeech(text: String) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        mTts?.speak("$text", TextToSpeech.QUEUE_ADD, params, "")
        // mTts.speak(myText2, TextToSpeech.QUEUE_ADD, null);
    }

    protected fun disableLockScreen(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }

}
