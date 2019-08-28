package com.charlesma.spellee

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.charlesma.spellee.base.BaseActivity
import com.charlesma.spellee.splash.SplashActivity
import com.charlesma.spellee.viewmodel.MainActivityViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : BaseActivity() {

    lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[MainActivityViewModel::class.java]
        viewModel.textToSpeechLiveData.observe(this, Observer {
             textToSpeech("$it")
        })

        initialTextToSpeech()

        initializeDrawer()

        disableLockScreen()
    }

    private fun initializeDrawer(){
        val navigationView =  findViewById(R.id.nav_view) as NavigationView
        val header = navigationView.getHeaderView(0)

        val textViewUserEmail = header.findViewById(R.id.username) as TextView
        FirebaseAuth.getInstance().currentUser?.email?.let {
            textViewUserEmail.text = it
        }

        val textViewSignout:TextView = header.findViewById(R.id.signout)
        textViewSignout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController(R.id.fragment_navihost).navigate(R.id.action_global_splashActivity)
            // startActivity(Intent(applicationContext,SplashActivity::class.java))
            finish()
        }

    }

    override fun onPostResume() {
        super.onPostResume()
        fetchRemoteConfig()
    }

    override fun getContentLayoutResId(): Int {
        return R.layout.content_main
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }



    override fun onTtsInitSuccess() {
        viewModel.textToSpeechReadyLiveData.postValue(true)
    }

    override fun onTtsInitFailed() {
        viewModel.textToSpeechReadyLiveData.postValue(false)
    }



    private fun fetchRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setDefaults(R.xml.firebase_remote_default)

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
//            .setMinimumFetchIntervalInSeconds(4200)
            .build()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.fetch(60).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this@MainActivity, "Fetch Succeeded",
                    Toast.LENGTH_SHORT
                ).show()

                // After config data is successfully fetched, it must be activated before newly fetched
                // values are returned.
                remoteConfig.activateFetched()
            } else {
                Toast.makeText(
                    this@MainActivity, "Fetch Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@MainActivity, exception.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
}
