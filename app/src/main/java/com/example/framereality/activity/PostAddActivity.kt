package com.example.framereality.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.framereality.AdapterImagePicked
import com.example.framereality.ModelImagePicked
import com.example.framereality.MyUtils
import com.example.framereality.databinding.ActivityPostAddBinding
import com.google.android.material.tabs.TabLayout

class PostAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostAddBinding
    private val TAG = "PostAddActivity"

    private var imageUri:Uri? = null

    private var purpose = MyUtils.PROPERTY_TYPE_SELL
    private var category = MyUtils.propertyTypes[0]
    private var adapterPropertySubcategory: ArrayAdapter<String>? = null

    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>

    private lateinit var adapterImagePicked : AdapterImagePicked

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

        imagePickedArrayList = ArrayList()
        loadImages()

        setupTabs()
        propertyCategoryHome() // Set default category



        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            purpose = radioButton.text.toString()
            Log.d(TAG, "Purpose: $purpose")
        }

        binding.pickImagesTV.setOnClickListener {
            showImagePickOptions()
        }
    }

    private fun setupTabs() {
        val tabLayout = binding.propertyCategoryTabLayout


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

    private fun propertyCategoryHome() {
        // For the Home tab, show fields and use the Homes array.
        showFields(true)
        adapterPropertySubcategory = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            MyUtils.propertyTypesHomes
        )
        binding.propertySubcategoryACTV.setText("")
    }

    private fun propertyCategoryPlot() {
        // For the Plot tab, hide fields and use the Plots array.
        showFields(false)
        adapterPropertySubcategory = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            MyUtils.propertyTypesPlots
        )
        binding.propertySubcategoryACTV.setText("")
    }

    private fun propertyCategoryCommercial() {
        // For the Commercial tab, hide fields and use the Commercial array.
        showFields(false)
        adapterPropertySubcategory = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            MyUtils.propertyTypesCommercial
        )
        binding.propertySubcategoryACTV.setText("")
    }

    private fun showFields(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        binding.floorsTIL.visibility = visibility
        binding.bedRoomsTIL.visibility = visibility
        binding.bathRoomsTIL.visibility = visibility
    }

    private fun showImagePickOptions(){
        Log.d(TAG, "showImagePickOptions: ")

        val popupMenu = PopupMenu(this,binding.pickImagesTV)

        popupMenu.menu.add(Menu.NONE,1,1,"Camera")
        popupMenu.menu.add(Menu.NONE,2,2,"Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->

            val itemId = item.itemId
            when(itemId){
                1 -> {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        val permissions = arrayOf(Manifest.permission.CAMERA)
                        requestCameraPermissions.launch(permissions)
                    }
                    else{
                        val permissions = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestCameraPermissions.launch(permissions)
                    }
                }
                2 -> {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        pickImageGallery()
                    }
                    else{
                        val permissions = Manifest.permission.WRITE_EXTERNAL_STORAGE
                        requestStoragePermission.launch(permissions)
                    }
                }
            }

            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if(isGranted){
            pickImageGallery()
        }
        else{

            MyUtils.toast(this,"Storage Permission denied!")
        }
    }

    private val requestCameraPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG, "requestCameraPermissions: result: $result")

        var areAllGranted = true
        for(isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if(areAllGranted){
            pickImageCamera()
        }
        else{
            MyUtils.toast(this,"Camera Permission denied!")
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)

        intent.setType("image/*")
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result->
        Log.d(TAG, "galleryActivityResultLauncher: result: $result")

        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data?.data

            Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")

            val timestamp = "${MyUtils.timestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp,imageUri,null,false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        }
        else{
            MyUtils.toast(this,"Cancelled!")
        }
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP_DESCRIPTION")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)

        cameraActivityResultLauncher.launch(intent)

    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "cameraActivityResultLauncher: result: $result")

        if(result.resultCode == Activity.RESULT_OK){
            Log.d(TAG, "cameraActivityResultLauncher: imageUri: $imageUri")

            val timestamp = "${MyUtils.timestamp()}"

            val modelImagePicked = ModelImagePicked(timestamp,imageUri,null,false)

            imagePickedArrayList.add(modelImagePicked)

            loadImages()
        }
        else{
            MyUtils.toast(this,"Cancelled!")
        }
    }

    private fun loadImages() {
        Log.d(TAG, "loadImages: ")
        adapterImagePicked = AdapterImagePicked(this,imagePickedArrayList)

        binding.recyclerPhotos.adapter = adapterImagePicked
    }
}
