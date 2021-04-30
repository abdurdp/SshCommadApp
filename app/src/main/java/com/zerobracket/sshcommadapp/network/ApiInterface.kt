package com.zerobracket.sshcommadapp.network

import com.zerobracket.sshcommadapp.models.SshLinkModel
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {
    @get:GET("ssh")
    val commands: Call<List<SshLinkModel>>
}