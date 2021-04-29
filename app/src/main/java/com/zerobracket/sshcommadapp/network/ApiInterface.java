package com.zerobracket.sshcommadapp.network;

import com.zerobracket.sshcommadapp.models.SshLinkModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("ssh")
    Call<List<SshLinkModel>> getCommands();
}
