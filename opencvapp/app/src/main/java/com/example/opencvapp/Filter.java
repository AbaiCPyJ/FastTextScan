package com.example.opencvapp;

import org.opencv.core.Mat;

public interface Filter {
    public abstract void apply(final Mat src, final Mat dst);
}