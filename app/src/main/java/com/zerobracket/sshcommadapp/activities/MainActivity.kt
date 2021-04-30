package com.zerobracket.sshcommadapp.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.zerobracket.sshcommadapp.adapters.CommandAdapter
import com.zerobracket.sshcommadapp.databinding.ActivityMainBinding
import com.zerobracket.sshcommadapp.models.SshLinkModel
import com.zerobracket.sshcommadapp.network.ApiClient
import com.zerobracket.sshcommadapp.network.ApiInterface
import com.zerobracket.sshcommadapp.utilities.ConnectivityCheck
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.common.channel.Channel
import org.apache.sshd.server.forward.AcceptAllForwardingFilter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var commandList: List<SshLinkModel>? = null
    private var commandAdapter: CommandAdapter? = null
    var channel: ClientChannel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        commandList = ArrayList()

        // Setting user.com property manually
        // since isn't set by default in android
        val key = "user.home"
        val Syscontext: Context
        Syscontext = applicationContext
        val `val` = Syscontext.applicationInfo.dataDir
        System.setProperty(key, `val`)
        if (ConnectivityCheck.isNetworkConnected(this)) getCommands(this) else Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show()
    }

    private fun getCommands(activity: Activity) {
        if (ConnectivityCheck.isNetworkConnected(activity)) {
            binding!!.progressCircular.visibility = View.VISIBLE
            val api = ApiClient.apiClient?.create(ApiInterface::class.java)
            val call = api?.commands
            call?.enqueue(object : Callback<List<SshLinkModel>> {
                override fun onResponse(call: Call<List<SshLinkModel>>, response: Response<List<SshLinkModel>>) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            commandList = response.body()
                            commandAdapter = CommandAdapter(activity, commandList) { sshLinkModel ->
                                binding!!.tvOutput.text = ""
                                binding!!.progressCircular.visibility = View.VISIBLE
                                commandAdapter!!.isClickable = false
                                val client = SshClient.setUpDefaultClient()
                                client.forwardingFilter = AcceptAllForwardingFilter.INSTANCE
                                client.start()

                                // Starting new thread because network processes
                                // can interfere with UI if started in main thread
                                val thread = Thread {
                                    try {
                                        // Connection establishment and authentication
                                        try {
                                            client.connect(sshLinkModel.username, sshLinkModel.host, sshLinkModel.port?.toInt()!!).verify(10000).session.use { session ->
                                                session.addPasswordIdentity(sshLinkModel.password)
                                                session.auth().verify(50000)
                                                println("Connection establihed")

                                                // Create a channel to communicate
                                                channel = session.createChannel(Channel.CHANNEL_SHELL)
                                                println("Starting shell")
                                                val responseStream = ByteArrayOutputStream()
                                                channel!!.setOut(responseStream)

                                                // Open channel
                                                channel!!.open().verify(5, TimeUnit.SECONDS)
                                                channel!!.getInvertedIn().use { pipedIn ->
                                                    pipedIn.write(sshLinkModel.command?.toByteArray())
                                                    pipedIn.flush()
                                                }

                                                // Close channel
                                                channel!!.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                                                        TimeUnit.SECONDS.toMillis(5))

                                                // Output after converting to string type
                                                val responseString = String(responseStream.toByteArray())
                                                println(responseString)
                                                activity.runOnUiThread {
                                                    binding!!.progressCircular.visibility = View.GONE
                                                    commandAdapter!!.isClickable = true
                                                    binding!!.tvOutput.text = "Connection established\n$responseString"
                                                }
                                            }
                                        } catch (e: NoClassDefFoundError) {
                                            e.printStackTrace()
                                            binding!!.tvOutput.text = """
                                                    Connection lost:
                                                    ${e.message}
                                                    """.trimIndent()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            binding!!.tvOutput.text = """
                                                    Connection lost:
                                                    ${e.message}
                                                    """.trimIndent()
                                        } finally {
                                            client.stop()
                                            binding!!.progressCircular.visibility = View.GONE
                                            commandAdapter!!.isClickable = true
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        binding!!.progressCircular.visibility = View.GONE
                                        commandAdapter!!.isClickable = true
                                    }
                                }
                                thread.start()
                            }
                            binding!!.progressCircular.visibility = View.GONE
                            binding!!.recyclerView.layoutManager = GridLayoutManager(activity, 2)
                            binding!!.recyclerView.setHasFixedSize(true)
                            binding!!.recyclerView.adapter = commandAdapter
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Please try again later:" + response.code(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<SshLinkModel>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Something went wrong:" + t.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(activity, "No Internet connection", Toast.LENGTH_SHORT).show()
        }
    }
}


