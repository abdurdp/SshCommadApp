package com.zerobracket.sshcommadapp.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.zerobracket.sshcommadapp.adapters.CommandAdapter;
import com.zerobracket.sshcommadapp.databinding.ActivityMainBinding;
import com.zerobracket.sshcommadapp.models.SshLinkModel;
import com.zerobracket.sshcommadapp.network.ApiClient;
import com.zerobracket.sshcommadapp.network.ApiInterface;
import com.zerobracket.sshcommadapp.utilities.ConnectivityCheck;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private List<SshLinkModel> commandList;
    private CommandAdapter commandAdapter;
    ClientChannel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        commandList = new ArrayList<>();

        // Setting user.com property manually
        // since isn't set by default in android
        String key = "user.home";
        Context Syscontext;
        Syscontext = getApplicationContext();
        String val = Syscontext.getApplicationInfo().dataDir;
        System.setProperty(key, val);
        if(ConnectivityCheck.isNetworkConnected(this))
            getCommands(this);
        else Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show();
    }

    private void getCommands(Activity activity) {
        if (ConnectivityCheck.isNetworkConnected(activity)) {

            binding.progressCircular.setVisibility(View.VISIBLE);

            ApiInterface api = ApiClient.getApiClient().create(ApiInterface.class);
            Call<List<SshLinkModel>> call = api.getCommands();
            call.enqueue(new Callback<List<SshLinkModel>>() {
                @Override
                public void onResponse(Call<List<SshLinkModel>> call, Response<List<SshLinkModel>> response) {
                    if (response.isSuccessful()) {


                        if (response.body() != null) {
                            commandList = response.body();
                            commandAdapter = new CommandAdapter(activity, commandList, new CommandAdapter.CallBack() {
                                @Override
                                public void commandClicked(SshLinkModel sshLinkModel) {
                                    binding.tvOutput.setText("");
                                    binding.progressCircular.setVisibility(View.VISIBLE);
                                    commandAdapter.isClickable = false;
                                    SshClient client = SshClient.setUpDefaultClient();
                                    client.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
                                    client.start();

                                    // Starting new thread because network processes
                                    // can interfere with UI if started in main thread
                                    Thread thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                // Connection establishment and authentication
                                                try (ClientSession session = client.connect(sshLinkModel.getUsername(), sshLinkModel.getHost(), Integer.parseInt(sshLinkModel.getPort())).verify(10000).getSession()) {
                                                    session.addPasswordIdentity(sshLinkModel.getPassword());
                                                    session.auth().verify(50000);
                                                    System.out.println("Connection establihed");

                                                    // Create a channel to communicate
                                                    channel = session.createChannel(Channel.CHANNEL_SHELL);
                                                    System.out.println("Starting shell");

                                                    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                                                    channel.setOut(responseStream);

                                                    // Open channel
                                                    channel.open().verify(5, TimeUnit.SECONDS);
                                                    try (OutputStream pipedIn = channel.getInvertedIn()) {
                                                        pipedIn.write(sshLinkModel.getCommand().getBytes());
                                                        pipedIn.flush();
                                                    }

                                                    // Close channel
                                                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                                                            TimeUnit.SECONDS.toMillis(5));

                                                    // Output after converting to string type
                                                    String responseString = new String(responseStream.toByteArray());
                                                    System.out.println(responseString);
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            binding.progressCircular.setVisibility(View.GONE);
                                                            commandAdapter.isClickable = true;
                                                            binding.tvOutput.setText("Connection established\n"+responseString);
                                                        }
                                                    });

                                                }catch (NoClassDefFoundError e){
                                                    e.printStackTrace();

                                                    binding.tvOutput.setText("Connection lost:\n"+e.getMessage());


                                                }
                                                catch (Exception e) {
                                                    e.printStackTrace();
                                                    binding.tvOutput.setText("Connection lost:\n"+e.getMessage());
                                                } finally {
                                                    client.stop();
                                                    binding.progressCircular.setVisibility(View.GONE);
                                                    commandAdapter.isClickable = true;

                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                binding.progressCircular.setVisibility(View.GONE);
                                                commandAdapter.isClickable = true;
                                            }
                                        }
                                    });
                                    thread.start();

                                }
                            });

                            binding.progressCircular.setVisibility(View.GONE);

                            binding.recyclerView.setLayoutManager((new GridLayoutManager(activity, 2)));
                            binding.recyclerView.setHasFixedSize(true);
                            binding.recyclerView.setAdapter(commandAdapter);
                        }

                    } else {

                        Toast.makeText(MainActivity.this, "Please try again later:" + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<SshLinkModel>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Something went wrong:" + t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }
        else {
            Toast.makeText(activity, "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }
}