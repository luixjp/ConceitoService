package br.edu.ifsp.scl.sdm.conceitoservice

import android.app.Service
import android.content.Intent
import android.os.IBinder

class LifetimeStartedService : Service() {

    /*Contado de segundos */
    private var lifetime: Int = 0

    companion object {
        /* Para passar o lifetime entre Activity e Service */
        val EXTRA_LIFETIME = "EXTRA_LIFETIME"
    }

    /* Nossa thread de trabalho que conta segundos em background */
    private inner class WorkerThread: Thread() {

        var running = false

        override fun run() {
            running = true
            while(running) {
                //Dorme 1s
                sleep(1000)
                //Envio o lifetime para Activity
                sendBroadcast(Intent("ACTION_RECEIVE_LIFETIME").also {
                    it.putExtra(EXTRA_LIFETIME, ++lifetime)
                })
            }
        }
    }

    private lateinit var workerThread: WorkerThread

    /* Primeira função executada em qualquer serviço */
    override fun onCreate() {
        super.onCreate()
        workerThread = WorkerThread()
    }

    /* Só faz sentido se for serviço vinculado, senão retornar null */
    override fun onBind(intent: Intent): IBinder? = null

    /* Chamado pelo startService (Serviço Iniciado). Nesse metodo o serviço
    executa indefinidamente até que seja chamado o método stopSelf (próprio serviço)
    ou stopService (a partir da Activity)*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(!workerThread.running) {
            workerThread.start()
        }

        return START_STICKY
    }

    /* Última função executada */
    override fun onDestroy() {
        super.onDestroy()
        workerThread.running = false
    }

}