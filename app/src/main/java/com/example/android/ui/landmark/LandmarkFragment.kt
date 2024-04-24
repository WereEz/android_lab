package com.example.android.ui.landmark

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.android.databinding.FragmentLandmarkBinding
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException

class LandmarkFragment : Fragment() {

    private var _binding: FragmentLandmarkBinding? = null
    private val binding get() = _binding!!
    private lateinit var xid: String
    private lateinit var landmarkName: String
    private lateinit var description: String
    private lateinit var imagePath: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLandmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        xid = arguments?.getString("xid") ?: ""
        landmarkName = arguments?.getString("title") ?: ""
        description = arguments?.getString("description") ?: ""
        imagePath = arguments?.getString("imagePath") ?: ""
        Log.e("xid", "Failed to parse landmark details: ${xid}")
        Log.e("xid", "Failed to parse landmark details: ${landmarkName}")
        Log.e("xid", "Failed to parse landmark details: ${description}")
        Log.e("xid", "Failed to parse landmark details: $imagePath")
        binding.textLandmarkName.visibility = View.GONE
        binding.imageLandmark.visibility = View.GONE
        if (landmarkName.isNotEmpty() && description.isNotEmpty() && imagePath.isNotEmpty()){
            binding.textLandmarkName.text = landmarkName
            binding.textLandmarkName.visibility = View.VISIBLE
            Glide.with(this@LandmarkFragment)
                .load(imagePath)
                .into(binding.imageLandmark)
            binding.imageLandmark.visibility = View.VISIBLE
            val tabLayout = binding.tabLayout
            val viewPager = binding.viewPager
            val args = Bundle().apply {
                putString("xid", xid)
                putString("title", landmarkName)
                putString("description", description)
                putString("imagePath", imagePath)
            }
            val adapter = LandmarkPagerAdapter(childFragmentManager, args)
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)
            tabLayout.getTabAt(0)?.text = "Обзор"
            tabLayout.getTabAt(1)?.text = "Заметки"
        }
        else{
            fetchLandmarkDetails(xid)
        }
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun fetchLandmarkDetails(xid: String) {
        val baseUrl = "https://api.opentripmap.com/0.1/ru/places/xid/$xid"
        val apiKey = "5ae2e3f221c38a28845f05b68c75a17a04b6be53c8ed44b386646482"
        val url = "$baseUrl?apikey=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                responseBody?.let { parseLandmarkDetails(it, xid) }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Error", "Failed to fetch landmark details: ${e.message}")
            }
        })
    }

    private fun parseLandmarkDetails(responseBody: String, xid: String) {
        try {
            val jsonObject = JSONObject(responseBody)
            Log.e("JSON Parsing Error", "Failed to parse landmark details: ${jsonObject}")
            landmarkName = jsonObject.getString("name")
            imagePath = jsonObject.optString("image", "")
            val wiki = jsonObject.getJSONObject("wikipedia_extracts")
            description = wiki.optString("text")
            activity?.runOnUiThread {
                binding.textLandmarkName.text = landmarkName
                binding.textLandmarkName.visibility = View.VISIBLE
                if (imagePath.isNotEmpty()) {
                    loadImage(imagePath)

                }
            }

        } catch (e: JSONException) {
            Log.e("JSON Parsing Error", "Failed to parse landmark details: ${e.message}")
        }
    }
    private fun loadImage(imageUrl: String) {
        if (imageUrl.startsWith("https://commons.wikimedia.org/")) {
            fetchImageFromUrl(imageUrl)
        } else {
            Glide.with(this@LandmarkFragment)
                .load(imageUrl)
                .into(binding.imageLandmark)
            setupViewPager()
        }
    }
    private fun fetchImageFromUrl(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    imagePath = getImageUrl(it).toString()
                    if (imagePath != null) {
                        activity?.runOnUiThread {
                            Glide.with(this@LandmarkFragment)
                                .load(imagePath)
                                .into(binding.imageLandmark)
                            setupViewPager()
                        }
                    }
                }

            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Image Loading Error", "Failed to load image: ${e.message}")
            }
        })
    }
    private fun setupViewPager() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        val args = Bundle().apply {
            putString("xid", xid)
            putString("title", landmarkName)
            putString("description", description)
            putString("imagePath", imagePath)
        }
        val adapter = LandmarkPagerAdapter(childFragmentManager, args)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)?.text = "Обзор"
        tabLayout.getTabAt(1)?.text = "Заметки"
        binding.imageLandmark.visibility = View.VISIBLE
    }
    private fun getImageUrl(html: String): String? {
        val doc = Jsoup.parse(html)
        val ogImageUrl = doc.select("meta[property=og:image]").firstOrNull()?.attr("content")
        return ogImageUrl?.takeIf { it.startsWith("https://upload.wikimedia.org/wikipedia/commons/") }
    }
    class LandmarkPagerAdapter(fm: FragmentManager, private val data: Bundle) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> OverviewFragment().apply {
                    arguments = Bundle(data).apply {
                    }
                }
                1 -> NotesFragment().apply {
                    arguments = Bundle(data).apply {
                    }
                }
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }


override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
