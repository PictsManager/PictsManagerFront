package com.pictsmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pictsmanager.request.service.GlobalService
import com.pictsmanager.util.GlobalStatus
import kotlinx.android.synthetic.main.activity_profil.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        initButtons()
    }

    private fun initButtons() {
        logoutButton.setOnClickListener {
            val userConnexionRequest = GlobalService.userService.logoutAccount(GlobalStatus.JWT)
            userConnexionRequest.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.code() == 400 || response.code() == 418) {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        System.out.println(jsonObject)
                        Toast.makeText(this@ProfilActivity, jsonObject.toString(), Toast.LENGTH_SHORT).show()
                    } else if (response.code() == 200) {

                        GlobalStatus.JWT = response.headers().get("JWT").toString()
                        Toast.makeText(this@ProfilActivity, "Aurevoir", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(this@ProfilActivity, ConnexionActivity::class.java)
                        startActivity(intent)
                    } else {
                        System.out.println("Untreated error")
                        Toast.makeText(this@ProfilActivity, "Untreated error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        this@ProfilActivity,
                        "Error server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}