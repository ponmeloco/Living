package de.living.startup


import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import de.living.R
import de.living.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        setupActionBar()

        binding.btnSignUp.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val email = binding.etEmail.text.toString()
        val pass = binding.etPassword.text.toString()
        val name = binding.etName.text.toString()
        if (email.isBlank() || pass.isBlank() || name.isBlank() ) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }


        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                val uid = auth.uid
                // Create a new user with a first and last name
                val user = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "uid" to uid,
                )

                val groupName: MutableMap<String, Any> = HashMap()
                groupName["group"] = listOf("Your Group")

                if (uid != null) {
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot added with ID: $uid")

                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                    db.collection("users").document(uid).collection("groups").document("groupNames").set(groupName)
                    db.collection("groups").document(email).set(groupName)
                    Toast.makeText(this, "Successfully Singed Up", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignUpActivity, IntroActivity::class.java))
                    ActivityCompat.finishAffinity(this)
                }
            } else {
                Toast.makeText(this, "Singed Up Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(binding.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding.toolbarSignUpActivity.setNavigationOnClickListener { onBackPressed() }
    }


}