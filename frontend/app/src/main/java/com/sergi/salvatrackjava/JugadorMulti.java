package com.sergi.salvatrackjava;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class JugadorMulti {
    public TextView tvTime;
    public ImageButton btnPlay;
    public LinearLayout llLaps;
    public long startTime = 0L;
    public long elapsedTime = 0L;
    public boolean isRunning = false;
    public long lastLapTime = 0L;
    public int lapCount = 1;
    public java.util.List<long[]> parciales = new java.util.ArrayList<>();
}