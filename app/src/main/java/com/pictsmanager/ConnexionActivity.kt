package com.pictsmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.pictsmanager.ui.main.MainFragment
import kotlinx.android.synthetic.main.activity_connexion.*

class ConnexionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    MainFragment.newInstance()
                )
                .commitNow()
        }
        initButtons()
    }

    private fun initButtons() {
        emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (!p0?.isNotEmpty()!!) {
                    emailInput.error = "This email can not be blank"
                } else if (p0.toString().length <= 3) {
                    emailInput.error = "This email is too short"
                }
            }
        })
        passInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (!p0?.isNotEmpty()!!) {
                    passInput.error = "This pass can not be blank"
                } else if (p0.toString().length <= 3) {
                    passInput.error = "This pass is too short"
                }
            }
        })

        connexionButton.setOnClickListener {    // connexionButton from kotlinx.android.synthetic.main.activity_connexion.*
            val emailInputVal = emailInput.text.toString()
            val passInputVal = passInput.text.toString()

            val intent = Intent(this@ConnexionActivity, CameraManagement::class.java)
            startActivity(intent)

        }


        createAccountButton.setOnClickListener {
//            val intent = Intent(this@ConnexionActivity, CreateAccountActivity::class.java)
            val intent = Intent(this@ConnexionActivity, CreateAccountActivity::class.java)
//                intent.putExtra("key", "Kotlin")
            startActivity(intent)
        }
    }

    private fun isInputsValid(email: String?, password: String?): Boolean {
        if (email != "email" || password != "password") {
            return false
        }
        return true
    }

    private fun tryConnexion() {

    }
}