package com.pictsmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pictsmanager.request.service.GlobalService
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
        val userConnexionRequest = GlobalService.userService.connexion(email, password)
        userConnexionRequest.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
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

            override fun onFailure(call: Call<Any>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@ConnexionActivity,
                    "Error server",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}