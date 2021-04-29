package com.zerobracket.sshcommadapp.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobracket.sshcommadapp.databinding.ItemButtonBinding;
import com.zerobracket.sshcommadapp.models.SshLinkModel;

import java.util.List;


public class CommandAdapter  extends RecyclerView.Adapter<CommandAdapter.ViewHolder>{
    private final List<SshLinkModel> commandList;
    private final Activity context;
    public boolean isClickable=true;
    CallBack callBack;
    public CommandAdapter(Activity context, List<SshLinkModel> commandList, CallBack callBack) {
        this.commandList = commandList;
        this.context = context;
        this.callBack = callBack;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemButtonBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SshLinkModel sshLinkModel = commandList.get(position);
        holder.itemButtonBinding.btnCommand.setText(sshLinkModel.getName());
        holder.itemButtonBinding.btnCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClickable)
                     callBack.commandClicked(sshLinkModel);
            }
        });

    }

    @Override
    public int getItemCount() {
        return commandList == null ? 0 :
                commandList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemButtonBinding itemButtonBinding;

        public ViewHolder(ItemButtonBinding itemButtonBinding) {
            super(itemButtonBinding.getRoot());
            this.itemButtonBinding = itemButtonBinding;
        }




        @Override
        public void onClick(View v) {



        }
    }

    public interface CallBack{
        void commandClicked(SshLinkModel sshLinkModel);
    }
}
