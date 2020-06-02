package com.pictsmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import com.pictsmanager.request.model.UserModel
import com.pictsmanager.request.service.UserService
import com.pictsmanager.util.EnumTypeInput
import kotlinx.android.synthetic.main.activity_connexion.emailInput
import kotlinx.android.synthetic.main.activity_connexion.passInput
import kotlinx.android.synthetic.main.activity_create_account.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountActivity : AppCompatActivity() {

    private val emailPattern = Regex("""^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}${'$'}""")
    private var isEmailValid = false
    private var isPasswordValid = false
    private var isPassVerifyValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        initButtons()
        enableCreationAccountButton(false)
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
        inputOnTextChange(
            editText = passVerifInput,
            errorText = "Password and Password Verification need to match",
            type = EnumTypeInput.VERIFY
        )
        creationAccountButton.setOnClickListener {
            val emailInputVal = emailInput.text.toString()
            val passInputVal = passInput.text.toString()

            tryCreateAccount(emailInputVal, passInputVal)

/*            Toast.makeText(
                this@CreateAccountActivity,
                "Creation Account Success",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(this@CreateAccountActivity, ConnexionActivity::class.java)
            startActivity(intent)*/
        }
    }

    private fun inputOnTextChange(
        editText: EditText,
        errorText: String,
        type: EnumTypeInput
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

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
                    EnumTypeInput.VERIFY -> {
                        isPassVerifyValid = isPassVerifyValid(p0)
                        if (!isPassVerifyValid)
                            editText.error = errorText
                    }

                }
                enableCreationAccountButton(isEmailValid && isPasswordValid && isPassVerifyValid)
            }
        })
    }

    private fun enableCreationAccountButton(state: Boolean) {
        creationAccountButton.isEnabled = state
        if (state)
            creationAccountButton.alpha = 1f
        else
            creationAccountButton.alpha = .5f
    }

    private fun isEmailValid(email: Editable?): Boolean {
        return email?.matches(emailPattern)!!
    }

    private fun isPasswordValid(password: Editable?): Boolean {
        return password.toString().length > 5
    }

    private fun isPassVerifyValid(password: Editable?): Boolean {
        if (password == null)
            return false
        return password.toString() == passInput.text.toString()
    }

    private fun tryCreateAccount(email: String, password: String) {
        val userModel = UserModel()
        userModel.email = email
        userModel.password = password
        val userConnexionRequest = UserService.service.tryCreateAccount(userModel)
        userConnexionRequest.enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                val body = response.body()
                body?.let {
                }
                Toast.makeText(
                    this@CreateAccountActivity,
                    "Creation Account Success",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@CreateAccountActivity, ConnexionActivity::class.java)
                startActivity(intent)
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {

                Toast.makeText(
                    this@CreateAccountActivity,
                    "Email already taken",
                    Toast.LENGTH_SHORT
                ).show()
                println(t.message)
            }
        })
    }
}
