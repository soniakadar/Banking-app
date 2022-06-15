package com.example.spacebank;

public class AccountHelper {

        private String sum, currency, accountNr, cnp, blocked;

         public AccountHelper(){ }

        public AccountHelper(String accountNr, String cnp, String currency, String sum, String blocked) {
            this.accountNr = accountNr;
            this.sum = sum;
            this.currency = currency;
            this.cnp = cnp;
            this.blocked = blocked;
        }

        public String getBlocked() {
            return blocked;
        }

        public void setBlocked(String blocked) {
            this.blocked = blocked;
        }


        public String getSum() {
            return sum;
        }

        public void setSum(String sum) {
            this.sum = sum;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getAccountNr() {
            return accountNr;
        }

        public void setAccountNr(String accountNr) {
            this.accountNr = accountNr;
        }

        public String getCnp() {
            return cnp;
        }

        public void setCnp(String cnp) {
            this.cnp = cnp;
        }
}
