package com.krsolutions.yetanotherlauncher
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/*
 * Linux command to send UDP:
 * #socat - UDP-DATAGRAM:192.168.1.255:11111,broadcast,sp=11111
 */
class UDPListenerService : Service() {
    //Boolean shouldListenForUDPBroadcast = false;
    var socket: DatagramSocket? = null
    @Throws(Exception::class)
    private fun listenAndWaitAndThrowIntent(broadcastIP: InetAddress, port: Int) {
        val recvBuf = ByteArray(15000)
        if (socket == null || socket!!.isClosed) {
            socket = DatagramSocket(port, broadcastIP)
            socket!!.broadcast = true
        }
        //socket.setSoTimeout(1000);
        val packet = DatagramPacket(recvBuf, recvBuf.size)
        Log.d("UDP", "Waiting for UDP broadcast")
        socket!!.receive(packet)
        val senderIP = packet.address.hostAddress
        val message = String(packet.data).trim { it <= ' ' }
        Log.e("UDP", "Got UDP broadcast from $senderIP, message: $message")
        broadcastIntent(senderIP, message)
        socket!!.close()
    }

    private fun broadcastIntent(senderIP: String, message: String) {
        val intent = Intent(UDP_BROADCAST)
        intent.putExtra("sender", senderIP)
        intent.putExtra("message", message)
        sendBroadcast(intent)
    }

    var UDPBroadcastThread: Thread? = null
    fun startListenForUDPBroadcast() {
        Log.d("UDP", "start listening")
        UDPBroadcastThread = Thread {
            try {
                val broadcastIP = InetAddress.getByName("255.255.255.255")
                val port = 50169
                while (shouldRestartSocketListen) {
                    listenAndWaitAndThrowIntent(broadcastIP, port)
                }
                //if (!shouldListenForUDPBroadcast) throw new ThreadDeath();
            } catch (e: Exception) {
                Log.i(
                    "UDP",
                    "no longer listening for UDP broadcasts cause of error " + e.message
                )
            }
        }
        UDPBroadcastThread!!.start()
    }

    private var shouldRestartSocketListen = true
    fun stopListen() {
        shouldRestartSocketListen = false
        socket!!.close()
    }

    override fun onCreate() {}
    override fun onDestroy() {
        stopListen()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        shouldRestartSocketListen = true
        startListenForUDPBroadcast()
        Log.d("UDP", "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        var UDP_BROADCAST = "UDPBroadcast"
    }
}
