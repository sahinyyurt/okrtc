package com.sahiny.okrtc.data.client

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.sahiny.okrtc.domain.client.WebRTCClient
import com.sahiny.okrtc.domain.event.PeerConnectionEvent
import com.sahiny.okrtc.domain.event.PeerConnectionEvent.OnAddStream
import com.sahiny.okrtc.domain.repository.WebRTCRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.webrtc.*
import org.webrtc.PeerConnection.RTCConfiguration
import javax.inject.Inject
import java.util.UUID;

internal class WebRTCClientImpl @Inject constructor(
    private val webRTCRepository: WebRTCRepository,
    private val database: FirebaseFirestore
) : WebRTCClient {
    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }

    private var rootEglBase: EglBase? = null

    private var localAudioTrack: AudioTrack? = null

    private var localVideoTrack: VideoTrack? = null

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }

    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    private lateinit var videoCapture: CameraVideoCapturer

    private val iceServer =
        listOf(PeerConnection.IceServer.builder(STUN_SERVER_URL).createIceServer(),
            PeerConnection.IceServer.builder(TURN_SERVER_URL)
                .setPassword("free").setUsername("free")
                .createIceServer())

    private val rtcConfig by lazy {
        createRTCConfig()
    }

    private fun createRTCConfig(): RTCConfiguration{
        val rtc = RTCConfiguration(iceServer)
        rtc.sdpSemantics = PeerConnection.SdpSemantics.PLAN_B
        return rtc
    }

    private val peerConnection by lazy { buildPeerConnection() }

    private val _eventFlow = MutableSharedFlow<PeerConnectionEvent>()

    val eventFlow = _eventFlow

    private val isMicEnabled = MutableStateFlow(true)
    private val isVideoEnabled = MutableStateFlow(true)


    init {
        collectState()
        rootEglBase = EglBase.create()
    }

    private fun collectState() {
        CoroutineScope(Dispatchers.IO).launch {
            isMicEnabled.collect{
                localAudioTrack?.setEnabled(it)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            isVideoEnabled.collect{
                localVideoTrack?.setEnabled(it)
            }
        }
    }

    override fun getVideoCapture(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    override fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase?.eglBaseContext, null)
    }

    override fun destroySurfaceView(view: SurfaceViewRenderer) = view.run {
        pauseVideo()
        release()
    }

    override fun startLocalView(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase?.eglBaseContext)
        (videoCapture as VideoCapturer).initialize(
            surfaceTextureHelper,
            localVideoOutput.context,
            localVideoSource.capturerObserver
        )
        videoCapture.startCapture(320, 240, 60)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack("audio_" + UUID.randomUUID(), audioSource);
        localVideoTrack = peerConnectionFactory.createVideoTrack( "video_" +  UUID.randomUUID(), localVideoSource)
        localVideoTrack?.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)

        peerConnection?.addStream(localStream)

    }

    override fun initVideoCapture(context: Application) {
        videoCapture = getVideoCapture(context)
    }

    override fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials(FIELD_TRIALS)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    override fun buildPeerConnectionFactory(): PeerConnectionFactory = PeerConnectionFactory.builder().apply {
        setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase?.eglBaseContext))
        setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase?.eglBaseContext, true, true))
        setOptions(PeerConnectionFactory.Options().apply {
            disableEncryption = true
            disableNetworkMonitor = true
        })
    }.createPeerConnectionFactory()

    override fun createPeerConnectionObserver(): PeerConnection.Observer =
        object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(p0: IceCandidate?) {
                CoroutineScope(Dispatchers.IO).launch {
                    p0?.let {
                        _eventFlow.emit(PeerConnectionEvent.OnIceCandidate(it))
                    }
                }
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {
                Log.d(TAG,"onAddStream")
                CoroutineScope(Dispatchers.IO).launch {
                    p0?.let {
                        _eventFlow.emit(OnAddStream(it))
                    }
                }
            }

            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {
                Log.d(TAG,"onRenegotiationNeeded")
            }
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                Log.d(TAG,"p1:" + p1?.size)
            }
        }

    override fun buildPeerConnection() =
        peerConnectionFactory.createPeerConnection(rtcConfig, createPeerConnectionObserver())

    override fun PeerConnection.call(roomID: String) {
        createOffer(
            createSdpObserver() { sdp, observer -> setLocalSdp(observer, sdp, roomID) },
            constraints
        )
    }

    override fun sendIceCandidate(candidate: IceCandidate?, isJoin: Boolean, roomID: String) =
        runBlocking {
            webRTCRepository.sendIceCandidate(candidate, isJoin, roomID)
        }

    override fun PeerConnection.answer(roomID: String) {
        createAnswer(
            createSdpObserver() { sdp, observer -> setLocalSdp(observer, sdp, roomID) },
            constraints
        )
    }

    override fun setLocalSdp(
        observer: SdpObserver,
        sdp: SessionDescription,
        roomID: String
    ) {
        peerConnection?.setLocalDescription(observer, sdp)
        database.collection("calls").document(roomID).set(
            hashMapOf<String, Any>(
                "sdp" to sdp.description,
                "type" to sdp.type
            )
        )
    }

    override fun onRemoteSessionReceived(description: SessionDescription) {
        peerConnection?.setRemoteDescription(createSdpObserver(), description)
    }

    override fun createSdpObserver(setLocalDescription: ((SessionDescription, SdpObserver) -> Unit)?) =
        object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                setLocalDescription?.let { function -> p0?.let { sdp -> function(sdp, this) } }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }

    override fun addCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    override fun getEvent(): SharedFlow<PeerConnectionEvent> = eventFlow.asSharedFlow()
    override fun toggleVoice() {
        isMicEnabled.value = !isMicEnabled.value
    }

    override fun toggleVideo() {
        isVideoEnabled.value = !isVideoEnabled.value
    }

    override fun closeSession(roomID:String) {
        if (localAudioTrack == null) return

        try {
            videoCapture.stopCapture()
            videoCapture.dispose()
            localVideoSource?.dispose()
            localAudioTrack?.dispose()
            localVideoTrack?.dispose()
            peerConnection?.close()
            localAudioTrack = null;
            localVideoTrack = null;
            database.collection("calls").document(roomID).set(
                hashMapOf<String, String>(
                    "type" to "END_CALL"
                )
            )

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun answer(roomID: String) =
        peerConnection?.answer(roomID)

    override fun call(roomID: String) =
        peerConnection?.call(roomID)

    companion object {
        private const val TAG = "WebRTCClient"
        private const val LOCAL_STREAM_ID = "local_track"
        private const val FIELD_TRIALS = "WebRTC-H264HighProfile/Enabled/"
        private const val STUN_SERVER_URL = "stun:stun.l.google.com:19302"
        private const val TURN_SERVER_URL = "turn:freestun.net:3478"
    }
}
