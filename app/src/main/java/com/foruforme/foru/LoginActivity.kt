package com.foruforme.foru

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.nfc.Tag
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import com.foruforme.foru.databinding.ActivityMainBinding
import com.foruforme.foru.databinding.LoginActivityBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.math.sign

class LoginActivity : AppCompatActivity() {

    private var mBinding: LoginActivityBinding? = null
    private val binding get() = mBinding!!

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            handleSignInResult(result.data)
        }
    private lateinit var client: SignInClient
    private lateinit var auth: FirebaseAuth

    private var mCheckBack = false;

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.login_activity)
        auth = Firebase.auth

        mBinding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener { signIn() }
        binding.logoutButton.setOnClickListener { logOut() }

        client = Identity.getSignInClient(this);

        val currentUser = auth.currentUser
        if (currentUser == null) {
            oneTapSignIn()
        }

        Log.d(TAG, "onCreate!!")
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser

        //인증 성공 시 MainActivity로 화면 전환.
        //차후 MainActivity에서 메뉴바로 로그아웃 버튼 및 기능 만들어야 함.
        //updateUI(currentUser)

        Log.d(TAG, "mCheckBack : " + mCheckBack)
        if(!mCheckBack){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause!!!")

        mCheckBack = true
        finish()
    }


    private fun handleSignInResult(data: Intent?) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            val credential = client.getSignInCredentialFromIntent(data)
            Log.d(TAG, "name: ${credential.displayName}");
            val idToken = credential.googleIdToken
            if (idToken != null) {
                Log.d(TAG, "firebaseAuthWithGoogle: ${credential.id}")
                firebaseAuthWithGoogle(idToken)
            } else {
                // Shouldn't happen.
                Log.d(TAG, "No ID token!")
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
            updateUI(null)
        }
    }

    private fun signIn() {
        // 요청 정보 옵션
        val signInRequest = GetSignInIntentRequest.builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        client.getSignInIntent(signInRequest).addOnSuccessListener { intent ->
            launchSignIn(intent)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Google Sign-in failed", e)
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        showProgressBar()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
                hideProgressBar()
            }

    }

    private fun oneTapSignIn() {
        // Configure One Tap UI
        val oneTapRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()

        // Display the One Tap UI
        client.beginSignIn(oneTapRequest)
            .addOnSuccessListener { result ->
                launchSignIn(result.pendingIntent)
            }
            .addOnFailureListener { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
            }
    }

    private fun launchSignIn(pendingIntent: PendingIntent) {
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent)
                .build()
            signInLauncher.launch(intentSenderRequest)
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Couldn't start Sign In: ${e.localizedMessage}")
        }
    }

    private fun logOut() {
        auth.signOut()
        client.signOut().addOnCompleteListener(this) {
            updateUI(null)
        }
        // todo: UpdateUI
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            binding.usernameTextview.text = user?.displayName;
            binding.loginButton.visibility = View.INVISIBLE;
            binding.logoutButton.visibility = View.VISIBLE;
        } else {
            binding.usernameTextview.text = "Please Login";
            binding.loginButton.visibility = View.VISIBLE;
            binding.logoutButton.visibility = View.VISIBLE;
        }
    }


    fun showProgressBar() {
        binding.progressCircular.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        binding.progressCircular.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

        mBinding = null
        Log.d(TAG, "onDestroy!!")
    }
}