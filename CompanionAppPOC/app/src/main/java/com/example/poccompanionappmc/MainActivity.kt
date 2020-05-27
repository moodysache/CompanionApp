package com.example.poccompanionappmc

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {


    private val deviceManager: CompanionDeviceManager by lazy {
        getSystemService(CompanionDeviceManager::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .setNamePattern(Pattern.compile("WSBC699000403K"))
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(true)
            .build()

        deviceManager.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {

                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    startIntentSenderForResult(
                        chooserLauncher,
                        123, null, 0, 0, 0
                    )
                }

                override fun onFailure(error: CharSequence?) {
                    // Handle failure
                }
            }, null
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            123 -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val deviceToPair: BluetoothDevice =
                        data!!.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    val result = deviceToPair.createBond()
                    if (result) {
                        Toast.makeText(
                            this,
                            "Paired successful with ${deviceToPair.name} with address ${deviceToPair.address}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    deviceToPair.connectGatt(this, true, object : BluetoothGattCallback() {
                        override fun onConnectionStateChange(
                            gatt: BluetoothGatt,
                            status: Int,
                            newState: Int
                        ) {
                            super.onConnectionStateChange(gatt, status, newState)

                            when (newState) {
                                STATE_CONNECTED -> {
                                    this@MainActivity.runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "State changed into CONNECTED",
                                            Toast.LENGTH_LONG

                                        ).show()

                                        val intent =
                                            packageManager.getLaunchIntentForPackage("com.specialized.exampleapp")
                                        this@MainActivity.startActivity(intent);
                                    }
                                }
                                STATE_DISCONNECTED -> {
                                    this@MainActivity.runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "State changed into DISCONNECTED",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                }
                                STATE_CONNECTING -> {
                                    this@MainActivity.runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "State changed into CONNECTING",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                STATE_DISCONNECTING -> {
                                    this@MainActivity.runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "State changed into DISCONNECTING",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }

                            }
                        }
                    })
                }
            }
        }
    }

    companion object {
        /** The profile is in disconnected state  */
        const val STATE_DISCONNECTED = 0

        /** The profile is in connecting state  */
        const val STATE_CONNECTING = 1

        /** The profile is in connected state  */
        const val STATE_CONNECTED = 2

        /** The profile is in disconnecting state  */
        const val STATE_DISCONNECTING = 3
    }


}
