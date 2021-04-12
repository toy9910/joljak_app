package com.example.dbwithfragment

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_insert.*
import kotlinx.android.synthetic.main.fragment_one_data.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

// 방별 조회
class OneDataFragment : Fragment() {
    val IP_ADDRESS = "3.35.105.27"
    val TAG = "phptest"

    lateinit var mJsonString: String

    lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public fun newInstance(): DataFragment {
        return DataFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_one_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner = one_spinner
        spinner.onItemSelectedListener = SpinnerListener()


        // seekbar 현재 디비값 보내는거
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                one_led.text = "$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                one_led.text = "${seekBar!!.progress}"
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                one_led.text = "${seekBar!!.progress}"

                //현재 어떤 방인지, 빛의 밝기를 데이터 보내는과정
                val room_num = (one_spinner.selectedItemPosition + 1).toString()
                val light = one_led.text.toString()

                val task = InsertData()
                task.execute("http://" + IP_ADDRESS + "/insert.php", room_num, light)
            }
        })
    }

    //데이터값 보내기
    inner class InsertData : AsyncTask<String, Void, String>() {
        lateinit var progressDialog: ProgressDialog
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

                var inputStream: InputStream? = null
                if (responseStatusCode == HttpURLConnection.HTTP_OK)
                    inputStream = httpURLConnection.inputStream
                else
                    inputStream = httpURLConnection.errorStream


                val inputStreamReader = InputStreamReader(inputStream, Charset.defaultCharset())
                val bufferedReader = BufferedReader(inputStreamReader)

                val sb = StringBuilder()
                var line: String? = null

                while (true) {
                    line = bufferedReader.readLine()
                    if (line != null)
                        sb.append(line)
                    else
                        break
                }

                bufferedReader.close()
                return sb.toString()
            } catch (e: Exception) {
                Log.d(TAG, "InsertData: Error ", e);
                val message = "Error: " + e.message
                return message
            }
        }
    }

    inner class SpinnerListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            var pos2 = position
            pos2 += 1
            var str = pos2.toString()

            val task = GetData()
            task.execute("http://" + IP_ADDRESS + "/query.php", str)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    fun setButtons() {
//        button_main_search.setOnClickListener {
//            mArrayList.clear()
//            mAdapter.notifyDataSetChanged()
//
//            val keyWord = editText_main_searchKeyword.text.toString()
//            editText_main_searchKeyword.setText("")
//
//            val task = GetData()
//            task.execute("http://" + IP_ADDRESS + "/query.php", keyWord)
//        }

    }

    inner class GetData : AsyncTask<String, Void, String>() {
        var progressDialog: ProgressDialog? = null
        lateinit var errorString: String
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog.show(activity, "Please Wait", null, true, true)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            progressDialog?.dismiss()

            Log.d(TAG, "response - $result")

            if (result == null)
                Log.e(TAG, errorString)
            else {
                mJsonString = result
                showResult()
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            val serverURL = params[0]
            val postParameters = "room_no=" + params[1]

            Log.d(TAG, "onItemSelected: $postParameters")
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

                var inputStream: InputStream? = null
                if (responseStatusCode == HttpURLConnection.HTTP_OK)
                    inputStream = httpURLConnection.inputStream
                else
                    inputStream = httpURLConnection.errorStream

                val inputStreamReader = InputStreamReader(inputStream, Charset.defaultCharset())
                val bufferedReader = BufferedReader(inputStreamReader)

                val sb = StringBuilder()
                var line: String? = null

                while (true) {
                    line = bufferedReader.readLine()
                    if (line != null)
                        sb.append(line)
                    else
                        break
                }
                bufferedReader.close()
                return sb.toString().trim()
            } catch (e: Exception) {
                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();
                return null
            }
        }
    }

    fun showResult() {
        val TAG_JSON = "joljak_dev"
        val TAG_TEMPERATURE = "temperature"
        val TAG_HUMIDITY = "humidity"
        val TAG_GAS = "gas"
        val TAG_DUST = "dust"
        val TAG_LIGHT = "light"

        try {
            val jsonObject = JSONObject(mJsonString)
            val jsonArray = jsonObject.getJSONArray(TAG_JSON)

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)

                val temperature = item.getString(TAG_TEMPERATURE)
                val humidity = item.getString(TAG_HUMIDITY)
                val gas = item.getString(TAG_GAS)
                val dust = item.getString(TAG_DUST)
                val light = item.getString(TAG_LIGHT)

                one_temp.text = temperature
                one_humidity.text = humidity
                one_gas.text = gas
                one_dust.text = dust
                one_led.text = light
                seekBar.progress = light.toInt()
            }
        } catch (e: JSONException) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}