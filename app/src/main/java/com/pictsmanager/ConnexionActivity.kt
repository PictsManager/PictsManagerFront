package com.pictsmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pictsmanager.request.model.UserModel
import com.pictsmanager.request.service.UserService
import com.pictsmanager.util.EnumTypeInput
import kotlinx.android.synthetic.main.activity_connexion.*
/*import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response*/

class ConnexionActivity : AppCompatActivity() {

    private val emailPattern = Regex("""^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}${'$'}""")
    private var isEmailValid = false
    private var isPasswordValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)
/*        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    MainFragment.newInstance()
                )
                .commitNow()
        }*/

        initButtons()

        // TODO : Comment the following line to pass-by the connexion verification
        enableConnexionButton(false)
    }

    private fun initButtons() {
        inputOnTextChange(
            editText = emailInput,
            errorText = "This ins't an email\nex: example.test@mail.com",
            type = EnumTypeInput.EMAIL
        )
        inputOnTextChange(
            editText = passInput,
            errorText = "Password too short",
            type = EnumTypeInput.PASSWORD
        )

        connexionButton.setOnClickListener {
            val emailInputVal = emailInput.text.toString()
            val passInputVal = passInput.text.toString()

            /* TODO : Connect with the real API
            tryConnexion(emailInputVal, passInputVal)
            */
            val intent = Intent(this@ConnexionActivity, HomeActivity::class.java)
//                intent.putExtra("key", "Kotlin")
            startActivity(intent)
        }

        createAccountButtonLink.setOnClickListener {
            val intent = Intent(this@ConnexionActivity, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun inputOnTextChange(
        editText: EditText,
        errorText: String,
        type: EnumTypeInput
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                when (type) {
                    EnumTypeInput.EMAIL -> {
                        isEmailValid = isEmailValid(p0)
                        if (!isEmailValid)
                            editText.error = errorText
                    }
                    EnumTypeInput.PASSWORD -> {
                        isPasswordValid = isPasswordValid(p0)
                        if (!isPasswordValid)
                            editText.error = errorText
                    }
                }
                enableConnexionButton(isEmailValid && isPasswordValid)
            }
        })
    }

    private fun enableConnexionButton(state: Boolean) {
        connexionButton.isEnabled = state
        if (state)
            connexionButton.alpha = 1f
        else
            connexionButton.alpha = .5f
    }

    private fun isEmailValid(email: Editable?): Boolean {
        return email?.matches(emailPattern)!!
    }

    private fun isPasswordValid(password: Editable?): Boolean {
        return password.toString().length > 5
    }

/*    private fun tryConnexion(email: String, password: String) {
        val userModel = UserModel()
        userModel.email = email
        userModel.password = password
        val userConnexionRequest = UserService.service.tryConnexion(userModel)
        userConnexionRequest.enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                val body = response.body()
                body?.let {
                }
                Toast.makeText(this@ConnexionActivity, "Connexion Success", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(this@ConnexionActivity, HomeActivity::class.java)
                startActivity(intent)
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                Toast.makeText(
                    this@ConnexionActivity,
                    "Wrong Email or Password",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }*/
}