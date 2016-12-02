package org.hni.provider.om;

public class LocationQueryParams {
    
    private double customerLattitudeRad;  
    private double customerLongitudeRad;
    
   
    
    private double maxLattitudeDeg;  
    private double maxLongitudeDeg;
    
    private double minLattitudeDeg;  
    private double minLongitudeDeg;
    
    private double maxLattitudeRad;  
    private double maxLongitudeRad;
    
    private double minLattitudeRad;  
    private double minLongitudeRad;
    
    private boolean meridian180WithinDistance;
    
    private double distnaceByRadius;

    public double getCustomerLattitudeRad() {
        return customerLattitudeRad;
    }

    public void setCustomerLattitudeRad(double customerLattitudeRad) {
        this.customerLattitudeRad = customerLattitudeRad;
    }
    
    public double getCustomerLongitudeRad() {
        return customerLongitudeRad;
    }

    public void setCustomerLongitudeRad(double customerLongitudeRad) {
        this.customerLongitudeRad = customerLongitudeRad;
    }

    public double getMaxLattitudeDeg() {
        return maxLattitudeDeg;
    }

    public void setMaxLattitudeDeg(double maxLattitudeDeg) {
        this.maxLattitudeDeg = maxLattitudeDeg;
    }

    public double getMaxLongitudeDeg() {
        return maxLongitudeDeg;
    }

    public void setMaxLongitudeDeg(double maxLongitudeDeg) {
        this.maxLongitudeDeg = maxLongitudeDeg;
    }

    public double getMinLattitudeDeg() {
        return minLattitudeDeg;
    }

    public void setMinLattitudeDeg(double minLattitudeDeg) {
        this.minLattitudeDeg = minLattitudeDeg;
    }

    public double getMinLongitudeDeg() {
        return minLongitudeDeg;
    }

    public void setMinLongitudeDeg(double minLongitudeDeg) {
        this.minLongitudeDeg = minLongitudeDeg;
    }

    public double getMaxLattitudeRad() {
        return maxLattitudeRad;
    }

    public void setMaxLattitudeRad(double maxLattitudeRad) {
        this.maxLattitudeRad = maxLattitudeRad;
    }

    public double getMaxLongitudeRad() {
        return maxLongitudeRad;
    }

    public void setMaxLongitudeRad(double maxLongitudeRad) {
        this.maxLongitudeRad = maxLongitudeRad;
    }

    public double getMinLattitudeRad() {
        return minLattitudeRad;
    }

    public void setMinLattitudeRad(double minLattitudeRad) {
        this.minLattitudeRad = minLattitudeRad;
    }

    public double getMinLongitudeRad() {
        return minLongitudeRad;
    }

    public void setMinLongitudeRad(double minLongitudeRad) {
        this.minLongitudeRad = minLongitudeRad;
    }

    public boolean isMeridian180WithinDistance() {
        return meridian180WithinDistance;
    }

    public void setMeridian180WithinDistance(boolean meridian180WithinDistance) {
        this.meridian180WithinDistance = meridian180WithinDistance;
    }

    public double getDistnaceByRadius() {
        return distnaceByRadius;
    }

    public void setDistnaceByRadius(double distnaceByRadius) {
        this.distnaceByRadius = distnaceByRadius;
    }
}
