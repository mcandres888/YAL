package com.krsolutions.yetanotherlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.krsolutions.yetanotherlauncher.databinding.ActivityMainBinding
import android.util.Log
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity() {
    private lateinit var resolvedApplist: List<ResolveInfo>
    lateinit var mainBinding: ActivityMainBinding
    private lateinit var myReceiver: BroadcastReceiver
    private lateinit var sharedViewModel: SharedViewModel




     fun registerReceiver(intentFilter: IntentFilter, onReceive: (intent: Intent?) -> Unit): BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                onReceive(intent)
            }
        }
        this.registerReceiver(receiver, intentFilter)
        return receiver
    }

    override fun onStart() {
        super.onStart()

        Log.d("UDP reciever", "On start")
        myReceiver = registerReceiver(IntentFilter("UDPBroadcast")) { intent ->
            when (intent?.action) {
                "UDPBroadcast" -> {
                    Log.d("UDP reciever", "device id " + (intent?.extras?.get("message") ?: "None" ))
                    stopService(Intent(this, UDPListenerService::class.java))
                    val serverIp = intent?.extras?.get("message").toString()
                    sharedViewModel.setServerIp(serverIp)
                }
            }
        }
        startService(Intent(this, UDPListenerService::class.java))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(myReceiver)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)
        resolvedApplist = packageManager
            .queryIntentActivities(Intent(Intent.ACTION_MAIN,null)
                .addCategory(Intent.CATEGORY_LAUNCHER),0)
        val appList = ArrayList<AppBlock>()

        // manually add on the appblock
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        sharedViewModel.setServerIp("http://192.168.2.93/YAL.apk")

        for (ri in resolvedApplist) {
            //Log.d("MCA", "package " + ri.activityInfo.packageName )
/*
            if(ri.activityInfo.packageName!=this.packageName) {
                val app = AppBlock(
                    ri.activityInfo.packageName,
                    ri.activityInfo.loadIcon(packageManager),
                    ri.activityInfo.packageName
                )
                appList.add(app)
            }

*/
            if(ri.activityInfo.packageName=="com.thousandminds.luckypaybettingdashboard") {
                val app = AppBlock(
                    "Casador Dashboard",
                    ri.activityInfo.loadIcon(packageManager),
                    ri.activityInfo.packageName
                )
                appList.add(app)
            }

            if(ri.activityInfo.packageName=="com.droidlogic.FileBrower") {
                val app = AppBlock(
                    "Update Dashboard (USB)",
                    ri.activityInfo.loadIcon(packageManager),
                    ri.activityInfo.packageName
                )
                appList.add(app)
            }
            if(ri.activityInfo.packageName=="com.mbx.settingsmbox") {
                val app = AppBlock(
                    "Setup Wifi Connection",
                    ri.activityInfo.loadIcon(packageManager),
                    ri.activityInfo.packageName
                )
                appList.add(app)
            }

            if(ri.activityInfo.packageName=="com.android.tv.settings") {
                val app = AppBlock(
                    "Device Settings",
                    ri.activityInfo.loadIcon(packageManager),
                    ri.activityInfo.packageName
                )
                appList.add(app)
            }

            if(ri.activityInfo.packageName=="com.android.chrome") {
                val app = AppBlock(
                    "Update from Server",
                    ri.activityInfo.loadIcon(packageManager),
                    ri.activityInfo.packageName
                )
                appList.add(app)
            }
        }
        mainBinding.appList.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL )
        mainBinding.appList.adapter = Adapter(this, sharedViewModel).also {
            it.passAppList(appList.sortedWith(
                Comparator<AppBlock> { o1, o2 -> o1?.appName?.compareTo(o2?.appName?:"",true)?:0; }
            ))
        }

        startActivity(
            packageManager.getLaunchIntentForPackage("com.thousandminds.luckypaybettingdashboard")
        )


    }
}
