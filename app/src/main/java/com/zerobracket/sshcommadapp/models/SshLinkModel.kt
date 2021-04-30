package com.zerobracket.sshcommadapp.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SshLinkModel {
    @SerializedName("id")
    @Expose
    var id = 0

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("host")
    @Expose
    var host: String? = null

    @SerializedName("port")
    @Expose
    var port: String? = null

    @SerializedName("username")
    @Expose
    var username: String? = null

    @SerializedName("password")
    @Expose
    var password: String? = null

    @SerializedName("command")
    @Expose
    var command: String? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("updatedAt")
    @Expose
    var updatedAt: String? = null

    @SerializedName("status")
    @Expose
    var status = 0
}