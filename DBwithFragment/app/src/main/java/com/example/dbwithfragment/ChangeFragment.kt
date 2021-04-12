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
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_change.*
import kotlinx.android.synthetic.main.fragment_login.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
//주석 테스트 섹스
class ChangeFragment : Fragment() {
    val IP_ADDRESS = "3.35.105.27"
    val TAG = "phptest"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        btn_change.setOnClickListener {
            val user_id_str = user_id.text.toString()
            val user_pw_str = user_password.text.toString()


            if(user_id_str.isEmpty() || user_pw_str.isEmpty())
                Toast.makeText(view.context,"아이디와 비밀번호를 모두 입력하세요.",Toast.LENGTH_SHORT).show()
            else {
                val task = InsertData()
                task.execute("http://" + IP_ADDRESS + "/user_change.php", user_id_str, user_pw_str)
            }
            user_id.setText("")
            user_password.setText("")
        }
    }

    inner class InsertData : AsyncTask<String, Void, String>() {
        lateinit var progressDialog : ProgressDialog
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog.show(activity, "Please Wait", null, true, true)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            Log.d(TAG, "POST response  - $result")
        }

        override fun doInBackground(vararg params: String?): String {
            val user_id = params[1]
            val user_pw = params[2]

            val serverURL = params[0]
            val postParameters = "user_id=" + user_id + "&user_pw=" + user_pw

            try {
                val url = URL(serverURL)
                val httpURLConnection = url.openConnection() as HttpURLConnection

                httpURLConnection.readTimeout = 5000
                httpURLConnection.connectTimeout = 5000
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.connect()

                val outputStream = httpURLConnection.outputStream
                outputStream.write(postParameters.toByteArray(Charset.defaultCharset()))
                outputStream.flush()
                outputStream.close()

                val responseStatusCode = httpURLConnection.responseCode
                Log.d(TAG, "POST response code - $responseStatusCode");

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
                return sb.toString()
            } catch (e : Exception) {
                Log.d(TAG, "InsertData: Error ", e);
                val message = "Error: " + e.message
                return message
            }
        }
    }

}