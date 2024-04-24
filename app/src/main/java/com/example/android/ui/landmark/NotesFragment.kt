package com.example.android.ui.landmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.android.MainActivity
import com.example.android.R
import com.example.android.databinding.FragmentNotesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recordDao = MainActivity.database.recordDao()
        val xid = arguments?.getString("xid") ?: ""
        val title = arguments?.getString("title") ?: ""
        lifecycleScope.launch(Dispatchers.IO) {
            val records = recordDao.getRecordsByXid(xid)

            withContext(Dispatchers.Main) {
                val containerLayout: LinearLayout = binding.containerLayout
                containerLayout.removeAllViews()

                records.forEach { similarRecord ->
                    val itemView =
                        layoutInflater.inflate(R.layout.item_notes, containerLayout, false)
                    val textView = itemView.findViewById<TextView>(R.id.textDescription)
                    textView.text = similarRecord.description

                    containerLayout.addView(itemView)
                }
            }
        }
        binding.buttonAdd.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.add_note, arguments)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
