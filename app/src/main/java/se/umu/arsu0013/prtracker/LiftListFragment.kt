package se.umu.arsu0013.prtracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

private const val TAG = "LiftListFragment"

class LiftListFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var sortSpinner: Spinner
    private lateinit var liftRecyclerView: RecyclerView
    private var adapter: LiftAdapter? = LiftAdapter(emptyList())

    private lateinit var floatingActionButton: FloatingActionButton

    // Interface for hosting activities to respond to clicks
    interface Callbacks {
        fun onLiftSelected(liftId: UUID)
    }

    private var callbacks: Callbacks? = null

    private val liftListViewModel: LiftListViewModel by lazy {
        ViewModelProvider(this).get(LiftListViewModel::class.java)
    }

    companion object {
        fun newInstance(): LiftListFragment {
            return LiftListFragment()
        }
    }

    // Required in order for the Fragment to receive callbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    // called when the fragment is attached to an activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // unchecked cast enforces that the hosting activity must implement the Callbacks interface
        callbacks = context as Callbacks?
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(
            TAG,
            "Number of lifts gotten from database: ${liftListViewModel.liftListLiveData.value?.size}"
        )

        val view = inflater.inflate(R.layout.fragment_lift_list, container, false)
        liftRecyclerView = view.findViewById(R.id.lift_recycler_view) as RecyclerView
        // without giving the recyclerView a layoutManager, the app will crash
        liftRecyclerView.layoutManager = LinearLayoutManager(context)
        liftRecyclerView.adapter = adapter

        // Have to delete with id, as position of holder varies with sorting
        val swipeHandler = object : SwipeDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val liftHolder = viewHolder as LiftHolder
                val liftId = liftHolder.getLiftId()
                liftListViewModel.deleteLiftWithId(liftId)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(liftRecyclerView)

        floatingActionButton = view.findViewById(R.id.button_fab)
        addActionListeners()

        return view
    }



    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_lift_list, menu)
        sortSpinner = menu.findItem(R.id.sort_lists).actionView as Spinner
        configureSortSpinner()


        liftListViewModel.liftListLiveData.observe(
            viewLifecycleOwner,
            Observer { lifts ->
                // Spinner has to be configured before this can be done
                lifts?.let {
                    Log.d(TAG, "Received ${lifts.size} lifts from database")
                    sortByCurrentSelection(lifts)?.let { lifts -> updateUI(lifts) }
                }
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "Menu item selected: $item")
        return super.onOptionsItemSelected(item)
    }


    private fun updateUI(lifts: List<Lift>) {
        adapter = sortByCurrentSelection(lifts)?.let { LiftAdapter(it) }
        liftRecyclerView.adapter = adapter
    }


    private fun addActionListeners() {
        floatingActionButton.setOnClickListener {
            val lift = Lift()
            liftListViewModel.addLift(lift)
            callbacks?.onLiftSelected(lift.id)
            Log.d(TAG, "Added lift with Id ${lift.id}}")
        }
    }


    private inner class LiftHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        private lateinit var lift: Lift

        private val dateTextView: TextView = itemView.findViewById(R.id.lift_date)
        private val exerciseTextView: TextView = itemView.findViewById(R.id.lift_exercise)
        private val weightTextView: TextView = itemView.findViewById(R.id.lift_weight)
        private val weightTypeTextView: TextView = itemView.findViewById(R.id.lift_weight_type)

        init {
            // itemView is the view for the entire row
            itemView.setOnClickListener(this)
        }

        fun bind(lift: Lift) {
            this.lift = lift
            dateTextView.text = DateTextFormatter.format(lift.date)
            exerciseTextView.text = this.lift.exercise
            weightTextView.text = this.lift.weight.toString()
            weightTypeTextView.text = this.lift.weightType.toString()
        }


        override fun onClick(v: View?) {
            callbacks?.onLiftSelected(lift.id)
        }

        fun getLiftId() = this.lift.id
    }


    private inner class LiftAdapter(var lifts: List<Lift>) : RecyclerView.Adapter<LiftHolder>() {
        // Is only called until enough views to fill the screen have been created
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiftHolder {
            val view = layoutInflater.inflate(R.layout.list_item_lift, parent, false)
            return LiftHolder(view)
        }

        override fun getItemCount(): Int {
            return lifts.size
        }

        // Do only minimal work in this function, otherwise the scrolling won't be smooth
        override fun onBindViewHolder(holder: LiftHolder, position: Int) {
            val lift = sortByCurrentSelection(lifts)?.get(position)
            lift?.let { holder.bind(it) }
        }
    }


    private fun configureSortSpinner() {
        val spinnerStringArray = arrayListOf<String>(
            getString(R.string.most_recent),
            getString(R.string.least_recent),
            getString(R.string.heaviest_first),
            getString(R.string.lightest_first),
            getString(R.string.exercise_alphabetical)
        )

        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            spinnerStringArray
        )
        sortSpinner.adapter = arrayAdapter
        sortSpinner.onItemSelectedListener = this
    }

    private fun sortByCurrentSelection(lifts: List<Lift>): List<Lift>? {
        val manager = SortLiftManager()
        return when(sortSpinner.selectedItem.toString()) {
            getString(R.string.exercise_alphabetical) -> manager.sortByAlphabetical(lifts)
            getString(R.string.most_recent) -> manager.sortByMostRecentFirst(lifts)
            getString(R.string.least_recent) -> manager.sortByLeastRecentFirst(lifts)
            getString(R.string.heaviest_first) -> manager.sortByHeaviestFirst(lifts)
            getString(R.string.lightest_first) -> manager.sortByLightestFirst(lifts)
            else -> manager.sortByMostRecentFirst(lifts)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val manager = SortLiftManager()
        manager.onItemSelected(
            requireContext(),
            parent,
            position,
            liftListViewModel.liftListLiveData.value
        )?.let {
            updateUI(
                it
            )
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}