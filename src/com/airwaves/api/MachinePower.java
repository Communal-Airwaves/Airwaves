package com.airwaves.api;

public interface MachinePower {
   int watts();

   int ratedVolts();

   double wantWatts();

   void feed(double var1, long var3);

   double storedJoules();

   boolean running();

   boolean onRotation();

   boolean onElectricity();

   float fraction();
}
