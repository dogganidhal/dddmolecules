package com.ndogga.dddmolecules.example;

public sealed interface Coupon {

    String code();

    double applyDiscount(double originalPrice);

    record PercentageCoupon(String code, double discountPercentage) implements Coupon {

        @Override
        public double applyDiscount(double originalPrice) {
            return originalPrice * (1 - discountPercentage / 100);
        }

    }

    record FixedAmountCoupon(String code, double amount) implements Coupon {

        @Override
        public double applyDiscount(double originalPrice) {
            if (amount >= originalPrice) {
                return 0;
            }

            return originalPrice - amount;
        }
    }


}
