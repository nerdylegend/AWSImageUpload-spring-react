package com.nerdylegend.upload.bucket;

public enum BucketName {

    PROFILE_NAME("nerdylegend-image-upload");

    private final String bucketName;

    BucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
