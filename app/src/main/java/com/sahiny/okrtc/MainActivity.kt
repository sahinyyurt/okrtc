package com.sahiny.okrtc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sahiny.okrtc.domain.state.FireStoreState
import com.sahiny.okrtc.presentation.view.MainScreen
import com.sahiny.okrtc.presentation.viewmodel.FireStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: FireStoreViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkCameraAndAudioPermission()
        setContent {
            MainScreen(viewModel = viewModel)
        }
        collectState()
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is FireStoreState.EnterRoom -> {
                        val intent =
                            Intent(this@MainActivity, WebRTCConnectActivity::class.java).apply {
                                putExtra("roomID", it.roomId)
                                putExtra("isJoin", it.isJoin)
                            }
                        startActivity(intent)
                    }

                    is FireStoreState.RoomAlreadyEnded -> {
                        Toast.makeText(this@MainActivity,"Ended",Toast.LENGTH_SHORT).show()
                    }

                    is FireStoreState.RoomNotFound -> {
                        Toast.makeText(this@MainActivity,"Room Not Found",Toast.LENGTH_SHORT).show()
                    }

                    is FireStoreState.RoomIdEmpty -> {
                        Toast.makeText(this@MainActivity,"Please type Room ID",Toast.LENGTH_SHORT).show()
                    }
                    is FireStoreState.Idle -> {

                    }
                }
            }
        }
    }

    private fun checkCameraAndAudioPermission() {
        Log.e(TAG, "checkPermission")
        if (ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                AUDIO_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraAndAudioPermission()
        }
    }

    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
        Log.e(TAG, "RequestPermission")
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                CAMERA_PERMISSION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                AUDIO_PERMISSION
            ) && !dialogShown
        ) {
            showPermissionRationDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    CAMERA_PERMISSION,
                    AUDIO_PERMISSION
                ),
                CAMERA_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showPermissionRationDialog() {
        Log.e(TAG, "showPermissionDialog")
        AlertDialog.Builder(this).apply {
            setTitle("Camera And Audio Permission Required")
            setMessage("This app need the camera and audio to function")
            setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraAndAudioPermission(true)
            }
            setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                permissionDenied()
            }
        }
    }

    private fun permissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "RTCActivity"
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }
}