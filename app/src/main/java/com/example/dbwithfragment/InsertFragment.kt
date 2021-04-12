package com.example.dbwithfragment

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_insert.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class InsertFragment : Fragment() {
    val IP_ADDRESS = "3.35.105.27"
    val TAG = "phptest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public fun newInstance() : InsertFragment {
        return InsertFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_insert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_main_insert.setOnClickListener {
            val room_no = editText_room_no.text.toString()
            val light = editText_room_light.text.toString()

            val task = InsertData()
            task.execute("http://" + IP_ADDRESS + "/insert.php", room_no, light)

            editText_room_no.setText("")
            editText_room_light.setText("")
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
            val room_no = params[1]
            val light = params[2]

            val serverURL = params[0]
            val postParameters = "room_no=" + room_no + "&light=" + light

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