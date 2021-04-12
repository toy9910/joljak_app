package com.example.dbwithfragment

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class LoginFragment : Fragment() {
    val IP_ADDRESS = "3.35.105.27"
    val TAG = "phptest"

    lateinit var mJsonString : String
    lateinit var pwd : String
    lateinit var edit_password : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user_pwd.setOnEditorActionListener{ textView, action, event ->
            var handled = false

            if (action == EditorInfo.IME_ACTION_DONE) {
                // 키보드 내리기
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(user_pwd.windowToken, 0)
                handled = true
            }

            handled
        }

        btn_login.setOnClickListener {
            val usr_id = user_id.text.toString()
            edit_password = user_pwd.text.toString()
            val task = GetData()
            task.execute("http://" + IP_ADDRESS + "/user_getjson.php", usr_id)
        }
    }

    inner class GetData : AsyncTask<String, Void, String>() {
        var progressDialog : ProgressDialog? = null
        lateinit var errorString : String
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog.show(activity,"Please Wait",null,true,true)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            progressDialog?.dismiss()

            Log.d(TAG, "POST response - $result")

            if(result == null)
                Log.e(TAG,errorString)
            else {
                mJsonString = result
                showResult()
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            val serverURL = params[0]
            val user_id = params[1]
            Log.d(TAG, "doInBackground: "+user_id)
            val postParameters = "user_id=" + user_id

            try {
                val url = URL(serverURL)
                val httpURLConnection = url.openConnection() as HttpURLConnection

                httpURLConnection.readTimeout = 5000
                httpURLConnection.connectTimeout = 5000
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.doInput = true
                httpURLConnection.connect()

                val outputStream = httpURLConnection.outputStream
                outputStream.write(postParameters?.toByteArray(Charset.defaultCharset()))
                outputStream.flush()
                outputStream.close()

                val responseStatusCode = httpURLConnection.responseCode
                Log.d(TAG, "response code - $responseStatusCode");

                var inputStream : InputStream? = null
                if(responseStatusCode == HttpURLConnection.HTTP_OK)
                    inputStream = httpURLConnection.inputStream
                else
                    inputStream = httpURLConnection.errorStream

                val inputStreamReader = InputStreamReader(inputStream, Charset.defaultCharset())
                val bufferedReader = BufferedReader(inputStreamReader)

                val sb = StringBuilder()
                var line : String? = null

                while(true) {
                    line = bufferedReader.readLine()
                    if(line != null)
                        sb.append(line)
                    else
                        break
                }
                bufferedReader.close()
                return sb.toString().trim()
            } catch (e : Exception) {
                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();
                return null
            }
        }
    }

    fun showResult() {
        val TAG_JSON = "joljak_dev"
        val TAG_PWD = "pwd"

        try {
            var isTrue = false
            val jsonObject = JSONObject(mJsonString)
            val jsonArray = jsonObject.getJSONArray(TAG_JSON)

            for(i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)

                pwd = item.getString(TAG_PWD)
            }

            if(edit_password == pwd) {
                isTrue = true
            }


            if(isTrue) {
                var a = activity as MainActivity
                a.replaceFragment(DataFragment())
            }
            else {
                Toast.makeText(context,"비밀 번호가 다릅니다!",Toast.LENGTH_SHORT).show()
                user_pwd.setText("")
                Log.d(TAG, "비밀번호 틀림");
            }
        } catch (e: JSONException) {
            Toast.makeText(context, "아이디가 다릅니다!",Toast.LENGTH_LONG).show()
            Log.d(TAG, "아이디가 없음");
        }
    }
}