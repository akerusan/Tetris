package com.example.tetris

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import org.json.JSONException
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.GraphResponse
import com.facebook.GraphRequest
import com.facebook.AccessToken



open class MainActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var loginButton: LoginButton
    private lateinit var circleImageView: CircleImageView
    private lateinit var txtName: TextView

    private lateinit var callBackManager: CallbackManager

    private var imageUrl = ""
    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton = login_button
        circleImageView = profile_pic
        txtName = profile_name

        callBackManager = CallbackManager.Factory.create()
        loginButton.setReadPermissions(arrayListOf("public_profile"))
        checkLoginStatus()

        loginButton.registerCallback(callBackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(loginResult: LoginResult){

            }
            override fun onCancel(){

            }
            override fun onError(error: FacebookException){

            }
        })

        launchGame.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        if(v == launchGame){
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("imageUrl", imageUrl)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callBackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private var tokenTracker = object :  AccessTokenTracker(){
        override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?, currentAccessToken: AccessToken?) {

            if (currentAccessToken == null){
                txtName.text = ""
                circleImageView.setImageResource(0)
                Toast.makeText(this@MainActivity, "User logged out", Toast.LENGTH_LONG).show()
            }
            else {
                loadUserProfile(currentAccessToken)
            }
        }
    }

    private fun loadUserProfile(newAccessToken: AccessToken){
        val request = GraphRequest.newMeRequest(newAccessToken, object : GraphRequest(), GraphRequest.GraphJSONObjectCallback {
            override fun onCompleted(obj: JSONObject, response: GraphResponse){
                try {
                val firstName = obj.getString("first_name")
                val lastName = obj.getString("last_name")
                val id = obj.getString("id")

                imageUrl = "https://graph.facebook.com/$id/picture?type=normal"
                userName = "$firstName $lastName"

                txtName.text = userName

                val requestOption = RequestOptions()
                requestOption.dontAnimate()

                Glide.with(this@MainActivity).load(imageUrl).into(circleImageView)

            } catch (e: JSONException){
                e.printStackTrace()
            }
            }
        })

        val parameters = Bundle()
        parameters.putString("fields", "first_name,last_name,id")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun checkLoginStatus() {
        if (AccessToken.getCurrentAccessToken() != null) {
            loadUserProfile(AccessToken.getCurrentAccessToken())
        }
    }

}


