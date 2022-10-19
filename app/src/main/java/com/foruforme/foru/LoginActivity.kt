package com.foruforme.foru

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.foruforme.foru.databinding.ActivityMainBinding
import com.foruforme.foru.databinding.LoginActivityBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private var mBinding: LoginActivityBinding? = null
    private val binding get() = mBinding!!

    private lateinit var client: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.login_activity)

        mBinding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate!!")

        auth = Firebase.auth
        setGoogleLogin()
    }

    override fun onDestroy() {
        super.onDestroy()

        mBinding = null

        Log.d(TAG, "onDestroy!!")

    }

    fun setGoogleLogin(){
        // 요청 정보 옵션
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        client = GoogleSignIn.getClient(this, options)

        binding.loginButton.setOnClickListener{
            startActivityForResult(client.signInIntent, 1)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            var account: GoogleSignInAccount? = null
            try {
                account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!.idToken)
            } catch (e: ApiException) {
                Log.e(TAG, "Google Login is Failed")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        // 인증에 성공한 후, 현재 로그인된 유저의 정보를 가져올 수 있습니다.
                        val email = auth.currentUser?.email

                        val user = FirebaseAuth.getInstance().currentUser
                        val name = user?.displayName
                        val photoUrl = user?.photoUrl

                        Log.d(TAG, "Login Success, Email : " + email)
                        Log.d(TAG, "Login Success, Name : " + name)
                        Log.d(TAG, "Login Success, photoUrl : " + photoUrl)

                    }
                })
    }
}