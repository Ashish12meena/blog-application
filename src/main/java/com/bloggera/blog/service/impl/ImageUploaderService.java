package com.bloggera.blog.service.impl;


import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
// import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service
public class ImageUploaderService {
    @Autowired
    private AmazonS3 client;

    @Value("${app.s3.bucket}")
    private String bucketName;


    
    public String uploadImage(MultipartFile image) {
        if (image == null) {
            return "image cannot be null";
        }
            String actualFilename = image.getOriginalFilename();
        String filename = UUID.randomUUID().toString() + actualFilename.substring(actualFilename.lastIndexOf("."));

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(image.getSize());
        String contentType = URLConnection.guessContentTypeFromName(image.getOriginalFilename());
        metadata.setContentType(contentType != null ? contentType : "image/jpeg");

        try {
            client.putObject(new PutObjectRequest(bucketName, filename, image.getInputStream(), metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead));

            return this.getPublicUrl(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // get Url of all Images
    
    public List<String> allFiles() {
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request().withBucketName(bucketName);

        ListObjectsV2Result listObjectsV2Result = client.listObjectsV2(listObjectsV2Request);
        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
        List<String> listOfFiles = objectSummaries.stream().map(item -> this.preSignedUrl(item.getKey()))
                .collect(Collectors.toList());
        return listOfFiles;
    }

    
    public String preSignedUrl(String filename) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, filename)
                .withMethod(HttpMethod.GET);

        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    //get public url
    
    public String getPublicUrl(String filename) {
        URL url = client.getUrl(bucketName, filename);
        return url.toString(); // Convert URL object to String
    }
}
