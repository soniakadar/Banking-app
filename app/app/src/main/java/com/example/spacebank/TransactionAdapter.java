package com.example.spacebank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.MyViewHolder> {

    private List<TransactionHelper> transactionList;


    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView type, account, recipient, date, time, amount,description ;
        MyViewHolder(View view) {
            super(view);
            type = view.findViewById(R.id.txtType);
            account = view.findViewById(R.id.txtAccount);
            recipient = view.findViewById(R.id.txtRecipient);
            date = view.findViewById(R.id.txtDate);
            time = view.findViewById(R.id.txtTime);
            amount = view.findViewById(R.id.txtAmount);
            description = view.findViewById(R.id.txtDescription);

        }
    }

    public TransactionAdapter(List<TransactionHelper> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transactions_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TransactionHelper transaction = transactionList.get(position);
        holder.type.setText(transaction.getType());
        holder.account.setText(transaction.getAccount());
        holder.recipient.setText(transaction.getRecipient());
        holder.date.setText(transaction.getDate());
        holder.time.setText(transaction.getTime());
        holder.amount.setText(transaction.getAmount());
        holder.description.setText(transaction.getDescription());

    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}
