package com.daqmobile.imulink.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Sends UDP datagrams to a single receiver (unicast).
 * Opens one socket for the lifetime of a streaming session,
 * then closes it cleanly when stop() is called.
 */
class UdpSender {

    private var socket: DatagramSocket? = null
    private var targetAddress: InetAddress? = null
    private var targetPort: Int = 5005

    /**
     * Open the socket and resolve the target IP.
     * Call this once before starting to send.
     * Returns true if successful, false if the IP is invalid.
     */
    suspend fun open(ip: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            close()
            targetAddress = InetAddress.getByName(ip)
            targetPort    = port
            socket        = DatagramSocket()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Send one CSV line. Safe to call from any coroutine —
     * switches to IO dispatcher automatically.
     * Silently drops the packet if the socket is not open.
     */
    suspend fun send(csv: String) = withContext(Dispatchers.IO) {
        try {
            val sock = socket ?: return@withContext
            val addr = targetAddress ?: return@withContext
            val bytes  = csv.toByteArray(Charsets.UTF_8)
            val packet = DatagramPacket(bytes, bytes.size, addr, targetPort)
            sock.send(packet)
        } catch (_: Exception) {
            // Silently ignore send errors — network may be temporarily unavailable
        }
    }

    /**
     * Close the socket. Safe to call multiple times.
     */
    fun close() {
        socket?.close()
        socket = null
    }

    val isOpen: Boolean get() = socket != null && !socket!!.isClosed
}
