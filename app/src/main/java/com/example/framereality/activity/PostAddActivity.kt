package com.example.framereality.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.framereality.MyUtils
import com.example.framereality.R
import com.example.framereality.databinding.ActivityPostAddBinding
import com.google.android.material.tabs.TabLayout

class PostAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostAddBinding
    private val TAG = "PostAddActivity"

    private var purpose = MyUtils.PROPERTY_TYPE_SELL
    private var category = MyUtils.propertyTypes[0]
    private var adapterPropertySubcategory: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set adapter for property size dropdown
        val areaSizeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MyUtils.propertyAreaSizeUnit)
        binding.sizeACTV.setAdapter(areaSizeAdapter)

        setupTabs()
        propertyCategoryHome() // Set default category

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            purpose = radioButton.text.toString()
            Log.d(TAG, "Purpose: $purpose")
        }
    }

    private fun setupTabs() {
        val tabLayout = binding.tabTL

        // Add tabs dynamically
        tabLayout.addTab(tabLayout.newTab().setText("Home").setIcon(R.drawable.home_black))
        tabLayout.addTab(tabLayout.newTab().setText("Plot").setIcon(R.drawable.plot))
        tabLayout.addTab(tabLayout.newTab().setText("Commercial").setIcon(R.drawable.commercial))

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        category = MyUtils.propertyTypes[0]
                        propertyCategoryHome()
                    }
                    1 -> {
                        category = MyUtils.propertyTypes[1]
                        propertyCategoryPlot()
                    }
                    2 -> {
                        category = MyUtils.propertyTypes[2]
                        propertyCategoryCommercial()
                    }
                }
                Log.d(TAG, "Category: $category")
                binding.propertySubcategoryACTV.setAdapter(adapterPropertySubcategory)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun propertyCategoryPlot() {
        showFields(false)
        adapterPropertySubcategory = ArrayAdapter(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesHomes)
        binding.propertySubcategoryACTV.setText("")
    }

    private fun propertyCategoryCommercial() {
        showFields(false)
        adapterPropertySubcategory = ArrayAdapter(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesPlots)
        binding.propertySubcategoryACTV.setText("")
    }

    private fun propertyCategoryHome() {
        showFields(true)
        adapterPropertySubcategory = ArrayAdapter(this, android.R.layout.simple_list_item_1, MyUtils.propertyTypesCommercial)
        binding.propertySubcategoryACTV.setText("")
    }

    private fun showFields(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        binding.floorsTIL.visibility = visibility
        binding.bedRoomsTIL.visibility = visibility
        binding.bathRoomsTIL.visibility = visibility
    }
}
