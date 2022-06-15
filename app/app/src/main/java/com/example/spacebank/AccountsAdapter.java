package com.example.spacebank;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.MyViewHolder> {

    private List<AccountHelper> accountsList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView accountNr, sum, currency, blocked;
        MyViewHolder(View view) {
            super(view);
            accountNr = view.findViewById(R.id.accountNr);
            sum = view.findViewById(R.id.sum);
            currency = view.findViewById(R.id.currency);
            blocked = view.findViewById(R.id.blocked);
        }
    }

    public AccountsAdapter(List<AccountHelper> accountsList) {
        this.accountsList = accountsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.accounts_list, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AccountHelper account = accountsList.get(position);
        holder.accountNr.setText(account.getAccountNr());
        holder.sum.setText(account.getSum());
        holder.currency.setText(account.getCurrency());
        if(!account.getBlocked().equals("")) {
            holder.blocked.setText("Blocked account");
        }else{
            holder.blocked.setText("");
        }


    }
    @Override
    public int getItemCount() {
        return accountsList.size();
    }
}