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
import androidx.documentfile.provider.DocumentFile
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
                    liftDetailViewModel.lift?.let { it1 ->
                        requireContext().contentResolver.openOutputStream(
                            it1.videoPath)
                    }

                try {
                    if (inputStream != null && outputStream != null) {
                        FileUtils.copy(inputStream, outputStream)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            configureThumbnail()
            configureVideoView()
            //saveLift(lift)
            Log.d(TAG, "Uri of selected video is: $uri")
            Log.d(TAG, "Uri of lift video is: ${liftDetailViewModel.lift?.videoPath}")
            Log.d(TAG, "Thumbnail Image visible: ${thumbnailImage.isVisible}")
        }


    private val recordVideoResult = registerForActivityResult(ActivityResultContracts.TakeVideo()) {
        configureThumbnail()
        configureVideoView()
        //saveLift(lift)
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
                    liftDetailViewModel.lift = lift
                    Log.d(TAG, "Lift videoPath is: ${liftDetailViewModel.lift!!.videoPath}")
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
            // fix deprecated calls if there is time
            liftDetailViewModel.lift?.let { lift ->
                DatePickerFragment.newInstance(lift.date).apply {
                    setTargetFragment(this@LiftDetailFragment, REQUEST_DATE)
                    show(this@LiftDetailFragment.requireFragmentManager(), DIALOG_DATE)
                }
            }
        }

        setOnClickListeners()

    }

    // in order to automatically save edits when the user exits the detail view
    override fun onStop() {
        Log.d(TAG, "Saved lift with id ${liftDetailViewModel.lift?.id}")
        super.onStop()
        liftDetailViewModel.lift?.let { saveLift(it) }
    }


    private fun saveLift(lift: Lift) {
        lift.exercise = exerciseText.text.toString()
        lift.weight = weightText.text.toString().toDouble()
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
        exerciseText.setText(liftDetailViewModel.lift?.exercise)
        weightText.setText(liftDetailViewModel.lift?.weight.toString())
        descriptionText.setText(liftDetailViewModel.lift?.description)
        dateButton.text = liftDetailViewModel.lift?.let { DateTextFormatter.format(it.date) }
        when (liftDetailViewModel.lift?.weightType) {
            WeightType.KILOGRAMS -> weightTypeButton.isChecked = false
            WeightType.POUNDS -> weightTypeButton.isChecked = true
        }
        configureWeightTypeButton()
        configureVideoView()
        configureThumbnail()
    }

    private fun configureThumbnail() {
        try {
            val metaDataRetriever = MediaMetadataRetriever()
            if (videoFileExists()) {
                metaDataRetriever.setDataSource(requireContext(), liftDetailViewModel.lift?.videoPath)
                val bitMap = metaDataRetriever.frameAtTime
                thumbnailImage.setImageBitmap(Bitmap.createScaledBitmap(bitMap!!, 900, 1024, true))
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception thrown in configureThumbnail()")
            e.printStackTrace()
        }

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
            liftDetailViewModel.lift?.weightType = when (liftDetailViewModel.lift?.weightType) {
                WeightType.KILOGRAMS -> WeightType.POUNDS
                WeightType.POUNDS -> WeightType.KILOGRAMS
                else -> WeightType.KILOGRAMS
            }
        }
    }


    override fun onDateSelected(date: Date) {
        liftDetailViewModel.lift?.date = date
        dateButton.text = DateTextFormatter.format(date)
    }


    private fun configureVideoPath() {
        val dir = requireContext().filesDir
        val file = File(dir, liftDetailViewModel.lift?.id.toString().plus(".mp4"))
        liftDetailViewModel.lift?.videoPath = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }


    private fun recordNewVideo() {
        recordVideoResult.launch(liftDetailViewModel.lift?.videoPath)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun selectVideoFromGallery() = selectVideoResult.launch("video/*")


    private fun configureVideoView() {

        val mediaController = MediaController(requireContext())
        videoView.setMediaController(mediaController)
        if (videoFileExists()) {
            videoView.setVideoURI(liftDetailViewModel.lift?.videoPath)
        }
    }


    private fun videoFileExists(): Boolean {
        val dir = requireContext().filesDir
        val file = File(dir, liftDetailViewModel.lift?.id.toString().plus(".mp4"))
        Log.d(TAG, "Length of video file: ${file.length()}")
        return file.length() != 0L
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