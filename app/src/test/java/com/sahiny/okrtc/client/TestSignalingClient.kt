package com.sahiny.okrtc.client

import com.sahiny.okrtc.data.client.SignalingClientImpl
import com.sahiny.okrtc.domain.event.SignalEvent
import com.sahiny.okrtc.domain.repository.SignalRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.webrtc.SessionDescription

class TestSignalingClient {
    @Test
    fun testOfferSDPReceived() = runTest {
        val signalRepoMock = Mockito.mock(SignalRepository::class.java)

        Mockito.`when`(signalRepoMock.connect("test")).thenAnswer{
            callbackFlow {
                backgroundScope.launch {
                    send(mapOf(("type" to "OFFER"),("sdp" to "test-offer-sdp")))
                }
                awaitClose {  }
            }
        }
        val test = SignalingClientImpl(signalRepoMock)

        val events = test.getEvent()

        backgroundScope.launch {
            events.collect{
                when(it) {
                    is SignalEvent.OfferReceived -> {
                        Assert.assertEquals(it.data.description, "test-offer-sdp")
                        Assert.assertEquals(it.data.type, SessionDescription.Type.OFFER)
                    }
                    else -> assert(false)
                }
            }
        }

        test.initialize("test")

    }

    @Test
    fun testAnswerSDPReceived() = runTest {
        val signalRepoMock = Mockito.mock(SignalRepository::class.java)

        Mockito.`when`(signalRepoMock.connect("test")).thenAnswer{
            callbackFlow {
                backgroundScope.launch {
                    send(mapOf(("type" to "ANSWER"),("sdp" to "test-answer-sdp")))
                }
                awaitClose {  }
            }
        }
        val test = SignalingClientImpl(signalRepoMock)

        val events = test.getEvent()

        backgroundScope.launch {
            events.collect{
                when(it) {
                    is SignalEvent.AnswerReceived -> {
                        Assert.assertEquals(it.data.description, "test-answer-sdp")
                        Assert.assertEquals(it.data.type, SessionDescription.Type.ANSWER)
                    }
                    else -> assert(false)
                }
            }
        }

        test.initialize("test")

    }

    @Test
    fun testICECandidateReceived() = runTest {
        val signalRepoMock = Mockito.mock(SignalRepository::class.java)

        Mockito.`when`(signalRepoMock.connect("test")).thenAnswer{
            callbackFlow {
                backgroundScope.launch {
                    send(mapOf(("sdpMid" to "1"),("sdpCandidate" to "test-candidate")))
                }
                awaitClose {  }
            }
        }
        val test = SignalingClientImpl(signalRepoMock)

        val events = test.getEvent()

        backgroundScope.launch {
            events.collect{
                when(it) {
                    is SignalEvent.IceCandidateReceived -> {
                        Assert.assertEquals(it.data.sdp, "test-answer-sdp")
                        Assert.assertEquals(it.data.sdpMid, 1)
                    }
                    else -> assert(false)
                }
            }
        }

        test.initialize("test")

    }

    @Test
    fun testEndCallReceived() = runTest {
        val signalRepoMock = Mockito.mock(SignalRepository::class.java)

        Mockito.`when`(signalRepoMock.connect("test")).thenAnswer{
            callbackFlow {
                backgroundScope.launch {
                    send(mapOf(("type" to "END_CALL")))
                }
                awaitClose {  }
            }
        }
        val test = SignalingClientImpl(signalRepoMock)

        val events = test.getEvent()

        backgroundScope.launch {
            events.collect{
                when(it) {
                    is SignalEvent.EndCallReceived -> {
                       assert(true)
                    }
                    else -> assert(false)
                }
            }
        }

        test.initialize("test")

    }
}