package se.umu.arsu0013.prtracker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.util.*

private const val TAG = "MainActivity"
private const val REQUEST_VIDEO_CAPTURE = 1
class MainActivity : AppCompatActivity(), LiftListFragment.Callbacks {

    private var currentLiftId: UUID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = LiftListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }


    override fun onLiftSelected(liftId: UUID) {
        Log.d(TAG, "MainActivity.onLiftSelected: $liftId")
        val fragment = LiftDetailFragment.newInstance(liftId, false)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            val fragment = LiftDetailFragment.newInstance(currentLiftId!!, true)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}