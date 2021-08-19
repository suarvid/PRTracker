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
class MainActivity : AppCompatActivity(), LiftListFragment.Callbacks, LiftDetailFragment.Callbacks {

    private var currentFragment: Fragment? = null
    private var currentLiftId: UUID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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


    //TODO: Maybe will work if this is done in the actual fragment instead of here
    override fun dispatchTakeVideoIntent(liftId: UUID) {

        currentLiftId = liftId
        val videoUri = Uri.parse(this.filesDir.absolutePath.plus(liftId.toString()))
        Log.d(TAG, "Saving video file to uri $videoUri")
        val intent: Intent = ActivityResultContracts.TakeVideo().createIntent(this, videoUri)
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
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