package com.example.macksham.friendbuzz;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessagelist){
        mAuth = FirebaseAuth.getInstance();
        this.mMessageList = mMessagelist;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public RelativeLayout singleMessageWrapper;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text_layout);
            singleMessageWrapper = itemView.findViewById(R.id.single_message_wrapper);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, final int position) {
        String current_user_id = mAuth.getCurrentUser().getUid();
        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
        holder.messageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Click Position",""+position);
            }
        });

        if(from_user.equals(current_user_id)){
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_white);
            holder.messageText.setTextColor(Color.BLACK);
            holder.singleMessageWrapper.setGravity(Gravity.RIGHT);
        }else{
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
            holder.singleMessageWrapper.setGravity(Gravity.LEFT);

        }
        holder.messageText.setText(c.getMessage());

    }



    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
