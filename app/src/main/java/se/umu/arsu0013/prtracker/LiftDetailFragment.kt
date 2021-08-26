package se.umu.arsu0013.prtracker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.FileUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import java.io.*
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.collections.HashMap

private const val ARG_LIFT_ID = "lift_id"
private const val ARG_VIDEO_CAPTURED = "video_captured"
private const val TAG = "LiftDetailFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0


class LiftDetailFragment : Fragment(), DatePickerFragment.Callbacks {


    private var videoCaptured: Boolean? = null
    private lateinit var lift: Lift
    private lateinit var exerciseText: EditText
    private lateinit var weightText: EditText
    private lateinit var dateButton: Button
    private lateinit var descriptionText: EditText
    private lateinit var videoView: VideoView
    private lateinit var thumbnailImage: ImageView
    private lateinit var weightTypeButton: ToggleButton
    private lateinit var recordNewVideoButton: Button
    private lateinit var selectExistingVideoButton: Button


    @RequiresApi(Build.VERSION_CODES.Q)
    private val selectVideoResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Here we want to copy the selected file to the "conventional" uri
            uri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val outputStream =
                    requireContext().contentResolver.openOutputStream(this.lift.videoPath)

                try {
                    if (inputStream != null && outputStream != null) {
                        FileUtils.copy(inputStream, outputStream)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            configureVideoView()
            this.thumbnailImage.isVisible = true
            saveLift(lift)
            Log.d(TAG, "Uri of selected video is: $uri")
            Log.d(TAG, "Uri of lift video is: ${this.lift.videoPath}")
            Log.d(TAG, "Thumbnail Image visible: ${thumbnailImage.isVisible}")
        }


    private val recordVideoResult = registerForActivityResult(ActivityResultContracts.TakeVideo()) {
        configureVideoView()
        this.thumbnailImage.isVisible = true
        saveLift(lift)
        Log.d(TAG, "Thumbnail Image visible: ${thumbnailImage.isVisible}")
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
        weightTypeButton = view.findViewById(R.id.toggleButtonWeightType) as ToggleButton
        thumbnailImage = view.findViewById(R.id.imageViewVideoThumbnail) as ImageView
        recordNewVideoButton = view.findViewById(R.id.buttonRecordNewVideo) as Button
        selectExistingVideoButton = view.findViewById(R.id.buttonSelectExistingVideo) as Button
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liftDetailViewModel.liftLiveData.observe(
            viewLifecycleOwner,
            Observer { lift ->
                lift?.let {
                    this.lift = lift
                    Log.d(TAG, "Lift videoPath is: ${this.lift.videoPath}")
                    configureVideoPath()
                    updateUI()
                }
            }
        )

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()
        dateButton.setOnClickListener {
            //TODO: fix these deprecated calls if there is time
            DatePickerFragment.newInstance(lift.date).apply {
                setTargetFragment(this@LiftDetailFragment, REQUEST_DATE)
                show(this@LiftDetailFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        setOnClickListeners()

    }

    // in order to automatically save edits when the user exits the detail view
    override fun onStop() {
        Log.d(TAG, "Saved lift with id ${lift.id}")
        super.onStop()
        saveLift(this.lift)
    }


    private fun saveLift(lift: Lift) {
        lift.exercise = exerciseText.text.toString()
        lift.weight = weightText.text.toString().toInt()
        saveLiftDate(lift)
        lift.description = descriptionText.text.toString()
        Log.d(TAG, "lift video uri in saveLift(): ${lift.videoPath}")
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
        configureVideoView()
        configureThumbnail()
    }

    private fun configureThumbnail() {
        thumbnailImage.isVisible = videoFileExists()
        try {
            val metaDataRetriever = MediaMetadataRetriever()
            metaDataRetriever.setDataSource(requireContext(), lift.videoPath)
            val bitMap = metaDataRetriever.frameAtTime
            thumbnailImage.setImageBitmap(Bitmap.createScaledBitmap(bitMap!!, 560, 480, true))

        } catch (e: Exception) {
            e.printStackTrace()
        }

        //TODO: Kopiera över den valda filen så att videons uri alltid pekar på den här mappen
        // plus lyftets id plus file extension, borde göra så att appen inte kraschar
        thumbnailImage.setOnClickListener {
            thumbnailImage.isVisible = false
            videoView.start()
            videoView.setOnCompletionListener {
                thumbnailImage.isVisible = true
            }
        }
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


    private fun configureVideoPath() {
        val dir = requireContext().filesDir
        val file = File(dir, lift.id.toString().plus(".mp4"))
        this.lift.videoPath = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    private fun recordNewVideo() {
        recordVideoResult.launch(this.lift.videoPath)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun selectVideoFromGallery() = selectVideoResult.launch("video/*")


    /*
        TODO: Clean this up, and make sure that the black image with a camera does not flash
        before the image changes to the existing thumbnail
        Probably only have to change the order of function calls
     */
    private fun configureVideoView() {
        if (videoFileExists()) {
            val mediaController = MediaController(requireContext())
            videoView.setMediaController(mediaController)
            videoView.setVideoURI(this.lift.videoPath)
        }
    }

    private fun videoFileExists(): Boolean {
        return this.lift.videoPath.toString() != ""
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setOnClickListeners() {
        recordNewVideoButton.setOnClickListener {
            recordNewVideo()
        }

        selectExistingVideoButton.setOnClickListener {
            selectVideoFromGallery()
        }
    }
}