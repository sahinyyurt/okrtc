package com.sahiny.okrtc.domain.client

import android.app.Application
import android.content.Context
import com.sahiny.okrtc.domain.event.PeerConnectionEvent
import kotlinx.coroutines.flow.SharedFlow
import org.webrtc.CameraVideoCapturer
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

interface WebRTCClient {
    fun getVideoCapture(context: Context): CameraVideoCapturer

    fun initSurfaceView(view: SurfaceViewRenderer)

    fun destroySurfaceView(view: SurfaceViewRenderer)

    fun startLocalView(localVideoOutput: SurfaceViewRenderer)

    fun initVideoCapture(context: Application)

    fun initPeerConnectionFactory(context: Application)

    fun buildPeerConnectionFactory(): PeerConnectionFactory

    fun createPeerConnectionObserver(): PeerConnection.Observer

    fun buildPeerConnection(): PeerConnection?

    fun PeerConnection.call(roomID: String)

    fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean, roomID: String)

    fun PeerConnection.answer(roomID: String)

    fun setLocalSdp(
        observer: SdpObserver,
        sdp: SessionDescription,
        roomID: String
    )

    fun onRemoteSessionReceived(description: SessionDescription)

    fun createSdpObserver(setLocalDescription: ((SessionDescription, SdpObserver) -> Unit)? = null): SdpObserver

    fun addCandidate(iceCandidate: IceCandidate?)

    fun answer(roomID: String): Unit?

    fun call(roomID: String): Unit?

    fun getEvent(): SharedFlow<PeerConnectionEvent>

    fun toggleVoice()

    fun toggleVideo()

    fun closeSession(roomID: String)

}