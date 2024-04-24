package com.example.android.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.android.MainActivity
import com.example.android.databinding.FragmentEditBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recordId = arguments?.getLong("recordId") ?: -1
        val recordDao = MainActivity.database.recordDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val record = recordDao.getRecordById(recordId)

            withContext(Dispatchers.Main) {
                if (record != null) {
                    binding.descriptionTextViewEdit.setText(record.description)
                }

                binding.backButton.setOnClickListener {
                    findNavController().popBackStack()
                }

                binding.saveButton.setOnClickListener {
                    if (record != null) {
                        record.description = binding.descriptionTextViewEdit.text.toString()
                        // Вызываем метод для обновления записи в базе данных
                        lifecycleScope.launch(Dispatchers.IO) {
                            recordDao.updateRecord(record)
                        }
                    }
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
