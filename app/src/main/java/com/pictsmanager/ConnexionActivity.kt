package com.pictsmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.pictsmanager.request.model.SuccessModel
import com.pictsmanager.request.model.UserModel
import com.pictsmanager.request.service.UserService
import com.pictsmanager.util.EnumTypeInput
import com.pictsmanager.util.GlobalStatus
import kotlinx.android.synthetic.main.activity_connexion.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConnexionActivity : AppCompatActivity() {

    private val emailPattern = Regex("""^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}${'$'}""")
    private var isEmailValid = false
    private var isPasswordValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)

        var emailInput = findViewById(R.id.emailInput) as EditText
        emailInput.setText("pierre@mail.com")
        var pwdInput = findViewById(R.id.passInput) as EditText
        pwdInput.setText("pierre")

        initButtons()

        // Comment the following line to pass-by the connexion verification
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

            tryConnexion(emailInputVal, passInputVal)


/*
            val intent = Intent(this@ConnexionActivity, HomeActivity::class.java)
//                intent.putExtra("key", "Kotlin")
            startActivity(intent)
*/
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

    private fun tryConnexion(email: String, password: String) {
        val userModel = UserModel()
        userModel.email = email
        userModel.password = password
        val userConnexionRequest = UserService.service.tryConnexion(email, password)
        userConnexionRequest.enqueue(object : Callback<SuccessModel> {
            override fun onResponse(call: Call<SuccessModel>, response: Response<SuccessModel>) {
                if (response.code() == 400 || response.code() == 418) {
                    val jsonObject = JSONObject(response.errorBody()!!.string())
                    System.out.println(jsonObject)
                    Toast.makeText(this@ConnexionActivity, jsonObject.toString(), Toast.LENGTH_SHORT).show()
                } else if (response.code() == 200) {
                    GlobalStatus.JWT = response.headers().get("JWT").toString()
                    Toast.makeText(this@ConnexionActivity, "Connexion Success", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this@ConnexionActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    System.out.println("Untreated error")
                    Toast.makeText(this@ConnexionActivity, "Untreated error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@ConnexionActivity,
                    "Wrong Email or Password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}