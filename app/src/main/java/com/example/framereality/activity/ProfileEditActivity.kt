package com.example.framereality.activity

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.framereality.MyUtils
import com.example.framereality.R
import com.example.framereality.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: AlertDialog
    private val TAG = "ProfileEditActivity"
    private var mUserType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Authentication instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Handle back button click
        binding.ToolBarBackButton.setOnClickListener { finish() }

        // Load user profile data from Firebase
        loadMyInfo()
    }

    /**
     * Loads user information from Firebase Realtime Database.
     */
    private fun loadMyInfo() {
        showProgressDialog()
        Log.d(TAG, "Loading user info...")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve user details from Firebase
                val dob = snapshot.child("dob").getValue(String::class.java) ?: "N/A"
                val name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                val email = snapshot.child("email").getValue(String::class.java) ?: "N/A"
                val phoneCode = snapshot.child("phoneCode").getValue(String::class.java) ?: ""
                val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "N/A"
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                mUserType = snapshot.child("userType").getValue(String::class.java) ?: ""

                // Disable input fields based on user type
                if (mUserType == MyUtils.USER_TYPE_PHONE) {
                    binding.PhoneEt.isEnabled = false
                    binding.PhoneTil.isEnabled = false
                    binding.PhoneCodeEt.isEnabled = false
                } else {
                    binding.EmailEt.isEnabled = false
                    binding.EmailTil.isEnabled = false
                }

                // Populate UI with retrieved data
                binding.nameEt.setText(name)
                binding.EmailEt.setText(email)
                binding.PhoneEt.setText(phoneNumber)
                binding.dobEt.setText(dob)

                // Set phone code if valid
                try {
                    val phoneCodeInt = phoneCode.replace("+", "").toInt()
                    binding.PhoneCodeEt.setCountryForPhoneCode(phoneCodeInt)
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting phone code: ${e.message}")
                }

                // Load profile image using Glide
                try {
                    Glide.with(this@ProfileEditActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.baseline_person_black)
                        .into(binding.shapeableImageView)
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading profile image: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                hideProgressDialog()
            }
        })
        hideProgressDialog()
    }

    /**
     * Displays a progress dialog while processing user actions.
     */
    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(this).setCancelable(false)
        val layout = LinearLayout(this).apply { setPadding(50, 50, 50, 50) }
        layout.addView(ProgressBar(this))
        builder.setView(layout)
        progressDialog = builder.create()
        progressDialog.show()
    }

    /**
     * Hides the progress dialog if it is currently showing.
     */
    private fun hideProgressDialog() {
        progressDialog.takeIf { it.isShowing }?.dismiss()
    }
}
