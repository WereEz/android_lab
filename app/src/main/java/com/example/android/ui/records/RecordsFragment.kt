package com.example.android.ui.records
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.MainActivity
import com.example.android.R
import com.example.android.databinding.FragmentRecordsBinding
import com.example.android.sampledata.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
        val recyclerView: RecyclerView = binding.recordsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launch(Dispatchers.IO) {
            val recordDao = MainActivity.database.recordDao()
            val recordsList = recordDao.getAllRecords()
            launch(Dispatchers.Main) {
                recyclerView.adapter = RecordsAdapter(recordsList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RecordsAdapter(private var records: List<Record>) :
        RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_museum, parent, false)
            return RecordViewHolder(view)

        }

        override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
            val record = records[position]
            holder.bind(record)
            holder.editButton.setOnClickListener {
                val bundle = Bundle().apply {
                    putLong("recordId", record.id)
                }
                navController.navigate(R.id.fragment_edit, bundle)
            }
            holder.deleteButton.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val recordDao = MainActivity.database.recordDao()
                    recordDao.deleteRecord(record)
                    records = recordDao.getAllRecords()
                }
                notifyDataSetChanged()
            }
            holder.nameTextView.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("xid", record.xid)
                }
                navController.navigate(R.id.fragment_landmark, bundle)
            }
        }

        override fun getItemCount(): Int {
            return records.size
        }

        inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
            val editButton: ImageView = itemView.findViewById(R.id.editImageView)
            val deleteButton: ImageView = itemView.findViewById(R.id.deleteImageView)

            fun bind(record: Record) {
                nameTextView.text = record.title
                descriptionTextView.text = record.description
            }
        }
    }
}