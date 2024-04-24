package com.example.android.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.android.MainActivity
import com.example.android.R
import com.example.android.databinding.FragmentAddNoteBinding
import com.example.android.sampledata.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFragment : Fragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButtonAdd.setOnClickListener {
            findNavController().popBackStack()
        }
        val xid = arguments?.getString("xid")
        val title = arguments?.getString("title")
        binding.saveButtonAdd.setOnClickListener {
            val recordDao = MainActivity.database.recordDao()
            val record = Record(
                title = title.toString(),
                description = binding.addTextViewEdit.text.toString(),
                xid = xid.toString()
            )
            lifecycleScope.launch(Dispatchers.IO) {
                recordDao.addRecord(record)
            }
            findNavController().navigate(R.id.fragment_landmark, arguments)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
