package com.ti.jguillendev.utils;

public class InAppPurchaseSimpleProduct {

    private int pImageResource;
    private String pSku;
    private String pPrice;
    private String pTokenPurchase;
    private boolean pPurchased;

    public InAppPurchaseSimpleProduct(int imageResource, String sku, String price){

        pImageResource = imageResource;
        pSku = sku;
        pPrice = price;
        pPurchased = false;
    }

    public void setAsPurchased(boolean purchased){
        this.pPurchased = purchased;
    }

    public void setpTokenPurchase(String token){

        this.pTokenPurchase = token;
        this.pPurchased = true;
    }

    public void setPurchasedText(){
        this.pPurchased = true;
        this.pPrice = this.pPrice + " (comprado)";
    }

    public void setNotPurchased(){
        this.pPrice = this.pPrice.replace(" (comprado)","");
        this.pPurchased = false;
    }


    public String getpPrice() {
        return pPrice;
    }

    public String getpSku() {
        return pSku;
    }

    public int getpImageResource() {
        return pImageResource;
    }

    public boolean getpPurchased(){ return pPurchased;}

    public String getpTokenPurchase(){ return pTokenPurchase; }
}
