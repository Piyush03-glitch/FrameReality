package com.example.framereality.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.framereality.MyUtils
import com.example.framereality.R
import com.example.framereality.databinding.ActivityLoginOptionsBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase

class LoginOptionsActivity : AppCompatActivity() {

    // View Binding
    private lateinit var binding: ActivityLoginOptionsBinding

    // Debugging Tag
    private val TAG = "LOGIN_OPTIONS_TAG"

    // Progress Dialog for Loading State
    private lateinit var progressDialog: ProgressDialog

    // Firebase Authentication
    private lateinit var firebaseAuth: FirebaseAuth

    // Google Sign-In Client
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Progress Dialog
        progressDialog = ProgressDialog(this).apply {
            setTitle("Please wait")
            setCanceledOnTouchOutside(false)
        }

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set Click Listeners
        binding.skipBtn.setOnClickListener { finish() }

        binding.loginGoogleBtn.setOnClickListener { beginGoogleLogin() }

        binding.loginEmailBtn.setOnClickListener {
            startActivity(Intent(this, LoginEmailActivity::class.java))
        }

        binding.loginPhoneBtn.setOnClickListener {
            startActivity(Intent(this, LoginPhoneActivity::class.java))
        }
    }

    /**
     * Starts the Google Sign-In process.
     */
    private fun beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin: Initiating Google Sign-In")
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    /**
     * Handles the result of the Google Sign-In intent.
     */
    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: Google Account ID: ${account.id}")

                // Authenticate with Firebase using Google account
                firebaseAuthWithGoogleAccount(account.idToken)
            } catch (e: ApiException) {
                Log.e(TAG, "googleSignInARL: Google Sign-In Failed", e)
                MyUtils.toast(this, "Google Sign-In failed. Please try again.")
            }
        } else {
            Log.d(TAG, "googleSignInARL: User cancelled sign-in")
            MyUtils.toast(this, "Sign-In cancelled. Try again.")
        }
    }

    /**
     * Authenticates with Firebase using Google credentials.
     */
    private fun firebaseAuthWithGoogleAccount(idToken: String?) {
        if (idToken == null) {
            Log.e(TAG, "firebaseAuthWithGoogleAccount: ID Token is null")
            MyUtils.toast(this, "Google Sign-In failed. Try again later.")
            return
        }

        Log.d(TAG, "firebaseAuthWithGoogleAccount: Authenticating with Firebase")

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                if (authResult.additionalUserInfo?.isNewUser == true) {
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: New User Detected - Creating Account")
                    updateUserInfoDb()
                } else {
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User Logged In")
                    navigateToMain()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "firebaseAuthWithGoogleAccount: Firebase Authentication Failed", e)
                MyUtils.toast(this, "Sign-In failed: ${getFirebaseErrorMessage(e)}")
            }
    }

    /**
     * Saves user data in Firebase Database.
     */
    private fun updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: Saving user info to database")

        progressDialog.setMessage("Saving user info...")
        progressDialog.show()

        val timestamp = MyUtils.timestamp()
        val user = firebaseAuth.currentUser

        if (user == null) {
            Log.e(TAG, "updateUserInfoDb: Firebase User is null")
            progressDialog.dismiss()
            MyUtils.toast(this, "Failed to retrieve user data. Try again.")
            return
        }

        val userInfo = mapOf(
            "uid" to (user.uid ?: ""),
            "email" to (user.email ?: "No Email"),
            "name" to (user.displayName ?: "No Name"),
            "timestamp" to timestamp,
            "phoneCode" to "",
            "phoneNumber" to "",
            "profileImageUrl" to "",
            "dob" to "",
            "userType" to MyUtils.USER_TYPE_GOOGLE,
            "token" to ""
        )

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user.uid)
            .setValue(userInfo)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfoDb: User info saved successfully")
                progressDialog.dismiss()
                navigateToMain()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateUserInfoDb: Failed to save user info", e)
                progressDialog.dismiss()
                MyUtils.toast(this, "Failed to save user info. Try again later.")
            }
    }

    /**
     * Navigates the user to the MainActivity and clears the back stack.
     */
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    /**
     * Provides user-friendly Firebase error messages.
     */
    private fun getFirebaseErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> "No account found with this email."
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials. Please check and try again."
            is FirebaseAuthUserCollisionException -> "This email is already linked to another account."
            is FirebaseAuthWeakPasswordException -> "Your password is too weak. Try a stronger one."
            is FirebaseAuthRecentLoginRequiredException -> "Please log in again for security reasons."
            is FirebaseNetworkException -> "No internet connection. Please check and try again."
            else -> "Authentication failed. Please try again."
        }
    }
}
