package se.umu.arsu0013.prtracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.IllegalArgumentException
import java.util.*

private const val TAG = "LiftListFragment"

class LiftListFragment : Fragment() {

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
        Log.d(TAG, "Number of lifts gotten from database: ${liftListViewModel.liftListLiveData.value?.size}")

        val view = inflater.inflate(R.layout.fragment_lift_list, container, false)
        liftRecyclerView = view.findViewById(R.id.lift_recycler_view) as RecyclerView
        // without giving the recyclerView a layoutManager, the app will crash
        liftRecyclerView.layoutManager = LinearLayoutManager(context)
        liftRecyclerView.adapter = adapter

        val swipeHandler = object : SwipeDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                liftListViewModel.deleteLift(viewHolder.absoluteAdapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(liftRecyclerView)

        floatingActionButton = view.findViewById(R.id.button_fab)
        addActionListeners()

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liftListViewModel.liftListLiveData.observe(
            viewLifecycleOwner,
            Observer { lifts ->
                lifts?.let {
                    Log.d(TAG, "Received ${lifts.size} lifts from database")
                    updateUI(lifts)
                }
            }
        )
    }


    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }


    private fun updateUI(lifts: List<Lift>) {
        adapter = LiftAdapter(lifts)
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

        init {
            // itemView is the view for the entire row
            itemView.setOnClickListener(this)
        }

        fun bind(lift: Lift) {
            this.lift = lift
            dateTextView.text = DateTextFormatter.format(lift.date)
            exerciseTextView.text = this.lift.exercise
            weightTextView.text = this.lift.weight.toString()
        }


        override fun onClick(v: View?) {
            callbacks?.onLiftSelected(lift.id)
        }
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
            val lift = lifts[position]
            holder.bind(lift)
        }
    }
}