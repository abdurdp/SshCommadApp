package com.zerobracket.sshcommadapp.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zerobracket.sshcommadapp.databinding.ItemButtonBinding
import com.zerobracket.sshcommadapp.models.SshLinkModel

class CommandAdapter(private val context: Activity, private val commandList: List<SshLinkModel>?,  var callBack: (SshLinkModel) -> Unit) : RecyclerView.Adapter<CommandAdapter.ViewHolder>(){
    var isClickable = true
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemButtonBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sshLinkModel = commandList!![position]
        holder.itemButtonBinding.btnCommand.text = sshLinkModel.name
        holder.itemButtonBinding.btnCommand.setOnClickListener {
            if (isClickable) callBack.invoke(sshLinkModel)
        }

    }

    override fun getItemCount(): Int {
        return commandList?.size ?: 0
    }

    inner class ViewHolder(val itemButtonBinding: ItemButtonBinding) : RecyclerView.ViewHolder(itemButtonBinding.root), View.OnClickListener {
        override fun onClick(v: View) {}
    }
}