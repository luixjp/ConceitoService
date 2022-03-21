package br.edu.ifsp.scl.sdm.conceitoservice

import android.content.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.sdm.conceitoservice.LifetimeStartedService.Companion.EXTRA_LIFETIME
import br.edu.ifsp.scl.sdm.conceitoservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val activityMainActivity: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val lifetimeServiceIntent: Intent by lazy {
        Intent(this, LifetimeBoundService::class.java)
    }

    private lateinit var lifetimeBoundService: LifetimeBoundService

    private var connected = false

    private val serviceConnection : ServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            lifetimeBoundService = (binder as LifetimeBoundService.LifetimeBoundServiceBinder).getService()
            connected = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            connected = false
        }

    }

    private inner class LifetimeServiceHandler(lifetimeServiceLooper: Looper) :
        Handler(lifetimeServiceLooper) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            if(connected) {
                runOnUiThread{
                    activityMainActivity.serviceLifetimeTv.text = lifetimeBoundService.lifetime.toString()
                }

                obtainMessage().also {
                    sendMessageDelayed(it, 1000)
                }
            }
        }
    }

    private lateinit var lifetimeServiceHandler: LifetimeServiceHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainActivity.root)

        HandlerThread("LifetimeHandlerThread").apply {
            start()
            lifetimeServiceHandler = LifetimeServiceHandler(this.looper)
        }

        with(activityMainActivity) {
            iniciarServicoBt.setOnClickListener{
                bindService(lifetimeServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                lifetimeServiceHandler.obtainMessage().also {
                    lifetimeServiceHandler.sendMessageDelayed(it, 1000)
                }
            }
            finalizarServicoBt.setOnClickListener{
                unbindService(serviceConnection)
                connected = false
            }
        }
    }
}

/* Main Activity com o STARTED SERVICE

package br.edu.ifsp.scl.sdm.conceitoservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.edu.ifsp.scl.sdm.conceitoservice.LifetimeStartedService.Companion.EXTRA_LIFETIME
import br.edu.ifsp.scl.sdm.conceitoservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val activityMainActivity: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val lifetimeServiceIntent: Intent by lazy {
        Intent(this, LifetimeStartedService::class.java)
    }

    /* BroadcastReceiver que recebe o lifetime do serviço */
    private val receiveLifetimeBr: BroadcastReceiver by lazy {
        object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.getIntExtra(EXTRA_LIFETIME, 0).also { lifetime ->
                    activityMainActivity.serviceLifetimeTv.text = lifetime.toString()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(receiveLifetimeBr, IntentFilter("ACTION_RECEIVE_LIFETIME"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiveLifetimeBr)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainActivity.root)

        with(activityMainActivity) {
            iniciarServicoBt.setOnClickListener{
                startService(lifetimeServiceIntent)
            }
            finalizarServicoBt.setOnClickListener{
                stopService(lifetimeServiceIntent)
            }
        }
    }
}

 */