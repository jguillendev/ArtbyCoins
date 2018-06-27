package com.ti.jguillendev.utils;

public class SkuPurchaseResult {

    public String OrderId = null;
    public String ProductId = null;
    public String PurchaseTime = null;
    public String PurchaseState  = null;
    public String PackageName = null;
    public String PurchaseToken =  null;

    public SkuPurchaseResult(String orderId, String productId, String purchaseTime, String purchaseState, String packageName){

        this.OrderId = orderId;
        this.ProductId = productId;
        this.PurchaseTime = purchaseTime;
        this.PurchaseState = purchaseState;
        this.PackageName = packageName;
    }

    public void setPurchaseToken(String purchaseToken) {
        PurchaseToken = purchaseToken;
    }
}
