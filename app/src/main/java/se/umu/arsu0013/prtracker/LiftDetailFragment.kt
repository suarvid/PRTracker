package se.umu.arsu0013.prtracker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import java.io.File
import java.util.*

private const val ARG_LIFT_ID = "lift_id"
private const val ARG_VIDEO_CAPTURED = "video_captured"
private const val TAG = "LiftDetailFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_VIDEO_CAPTURE = 1
private const val RESULT_OK = -1

class LiftDetailFragment : Fragment(), DatePickerFragment.Callbacks {

    interface Callbacks {
        fun dispatchTakeVideoIntent(liftId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var videoUri: Uri
    private var videoCaptured: Boolean? = null
    private lateinit var lift: Lift
    private lateinit var exerciseText: EditText
    private lateinit var weightText: EditText
    private lateinit var dateButton: Button
    private lateinit var descriptionText: EditText
    private lateinit var videoView: VideoView
    private lateinit var thumbnailImage: ImageView
    private lateinit var weightTypeButton: ToggleButton


    val launcher = registerForActivityResult<Uri, Bitmap>(
        ActivityResultContracts.TakeVideo()) {

    }


    private val liftDetailViewModel: LiftDetailViewModel by lazy {
        ViewModelProvider(this).get(LiftDetailViewModel::class.java)
    }


    companion object {
        fun newInstance(liftId: UUID, videoCaptured: Boolean): LiftDetailFragment {
            val args = Bundle().apply {
                putSerializable(ARG_LIFT_ID, liftId)
                putBoolean(ARG_VIDEO_CAPTURED, videoCaptured)
            }

            return LiftDetailFragment().apply {
                arguments = args
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lift = Lift()
        val liftId: UUID = arguments?.getSerializable(ARG_LIFT_ID) as UUID
        videoCaptured = arguments?.getSerializable(ARG_VIDEO_CAPTURED) as Boolean
        Log.d(TAG, "lift id received from args bundle: $liftId")
        liftDetailViewModel.loadLift(liftId)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lift_detail, container, false)
        exerciseText = view.findViewById(R.id.editTextLiftExercise) as EditText
        weightText = view.findViewById(R.id.editTextLiftWeight) as EditText
        dateButton = view.findViewById(R.id.buttonDate) as Button
        descriptionText = view.findViewById(R.id.editTextDescription) as EditText
        videoView = view.findViewById(R.id.videoViewLift) as VideoView
        thumbnailImage = view.findViewById(R.id.imageViewCameraIcon) as ImageView
        weightTypeButton = view.findViewById(R.id.toggleButtonWeightType) as ToggleButton

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liftDetailViewModel.liftLiveData.observe(
            viewLifecycleOwner,
            Observer { lift ->
                lift?.let {
                    this.lift = lift
                    updateUI()
                }
            }
        )
    }


    override fun onStart() {
        super.onStart()
        dateButton.setOnClickListener {
            //TODO: fix these deprecated calls if there is time
            DatePickerFragment.newInstance(lift.date).apply {
                setTargetFragment(this@LiftDetailFragment, REQUEST_DATE)
                show(this@LiftDetailFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        thumbnailImage.setOnClickListener {
            dispatchTakeVideoIntent()
        }

        videoView.setOnClickListener {
            dispatchTakeVideoIntent()
        }

    }

    // in order to automatically save edits when the user exits the detail view
    override fun onStop() {
        Log.d(TAG, "Saved lift with id ${lift.id}")
        super.onStop()
        saveLift(this.lift)
    }


    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun saveLift(lift: Lift) {
        lift.exercise = exerciseText.text.toString()
        lift.weight = weightText.text.toString().toInt()
        saveLiftDate(lift)
        lift.description = descriptionText.text.toString()
        liftDetailViewModel.saveLift(lift)
    }

    private fun saveLiftDate(lift: Lift) {
        val calendar = Calendar.getInstance()
        val dateText = dateButton.text.toString()
        val dateComponents = dateText.split("/")
        calendar.set(Calendar.DAY_OF_MONTH, dateComponents[0].toInt())
        calendar.set(Calendar.MONTH, dateComponents[1].toInt() - 1)
        calendar.set(Calendar.YEAR, dateComponents[2].toInt())
        lift.date = calendar.time
    }

    private fun updateUI() {
        exerciseText.setText(lift.exercise)
        weightText.setText(lift.weight.toString())
        descriptionText.setText(lift.description)
        dateButton.text = DateTextFormatter.format(lift.date)
        when (lift.weightType) {
            WeightType.KILOGRAMS -> weightTypeButton.isChecked = false
            WeightType.POUNDS -> weightTypeButton.isChecked = true
        }
        configureWeightTypeButton()
        configureVideoUri()
        configureVideoView()

    }


    private fun configureWeightTypeButton() {
        weightTypeButton.setOnClickListener {
            lift.weightType = when (lift.weightType) {
                WeightType.KILOGRAMS -> WeightType.POUNDS
                WeightType.POUNDS -> WeightType.KILOGRAMS
            }
        }
    }


    override fun onDateSelected(date: Date) {
        lift.date = date
        dateButton.text = DateTextFormatter.format(date)
    }


    //TODO: Change this to registerForActivityResult instead of this deprecated way
    private fun dispatchTakeVideoIntent() {
        val dir = requireContext().filesDir
        val file = File(dir, lift.id.toString().plus(".mp4"))
        val videoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        this.videoUri = videoUri
        val intent: Intent = ActivityResultContracts.TakeVideo().createIntent(requireContext(), videoUri)
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG, "Request Code OK on REQUEST_VIDEO_CAPTURE with Uri $videoUri")
            val dir = requireContext().filesDir
            configureVideoView()

            Log.d(TAG, "onActivityResult")
        }
    }

    private fun configureVideoUri() {
        val dir = requireContext().filesDir
        val file = File(dir, lift.id.toString().plus(".mp4"))
        this.videoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
    }

    /*
        TODO: Clean this up, and make sure that the black image with a camera does not flash
        before the image changes to the existing thumbnail
        Probably only have to change the order of function calls
     */
    private fun configureVideoView() {
        if (videoFileExists()) {
            val mediaController = MediaController(requireContext())
            videoView.setMediaController(mediaController)
            thumbnailImage.isVisible = false
            videoView.setVideoURI(videoUri)
            val metaDataRetreiver = MediaMetadataRetriever()
            metaDataRetreiver.setDataSource(requireContext(), videoUri)
            val bitMap = metaDataRetreiver.frameAtTime
            thumbnailImage.setImageBitmap(bitMap)
            thumbnailImage.isVisible = true
            thumbnailImage.setOnClickListener {
                thumbnailImage.isVisible = false
                videoView.start()
                videoView.setOnCompletionListener {
                    thumbnailImage.isVisible = true
                }
            }
        }
    }

    private fun videoFileExists(): Boolean {
        val dir = requireContext().filesDir
        val file = File(dir, lift.id.toString().plus(".mp4"))
        Log.d(TAG, "File with uri: $videoUri exists: ${file.exists()}")
        return file.exists()
    }
}