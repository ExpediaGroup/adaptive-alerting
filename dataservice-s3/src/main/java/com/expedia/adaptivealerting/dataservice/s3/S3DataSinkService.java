package com.expedia.adaptivealerting.dataservice.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.expedia.adaptivealerting.core.data.Mpoint;
import com.expedia.adaptivealerting.dataservice.DataSinkService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class S3DataSinkService implements DataSinkService {

  private static final String S3_DATE_FORMAT = "yyyy-MM-dd";

  private String toS3FormatStringFromInstant(Instant instant) {
    Date myDate = Date.from(instant);
    SimpleDateFormat formatter = new SimpleDateFormat(S3_DATE_FORMAT);
    return formatter.format(myDate);
  }

  public void put(List<Mpoint> mpoints) {
    String s3FormatDate = toS3FormatStringFromInstant(Instant.now());
    for (Mpoint mpoint : mpoints) {
      String site = mpoint.getMetric().getTag("site");
      String lob = mpoint.getMetric().getTag("lob");
      String directoryName = lob + "/" + site;
      String completeFilePath = lob + "/" + site + "/" + s3FormatDate;
      //File file = writeToFile(completeFilePath, directoryName, mpoint.getValue());
    }
    storeInS3();
  }

  private void storeInS3() {
    String fileName = toS3FormatStringFromInstant(Instant.now());
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion("us-set-2").build();
    String fileObjKeyName = "bookings/" + "test";
    PutObjectRequest request = new PutObjectRequest("aa-bucket", fileObjKeyName, new File("test"));
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType("plain/text");
    metadata.addUserMetadata("x-amz-meta-title", "someTitle");
    request.setMetadata(metadata);
    s3Client.putObject(request);
  }

  private String getRelevantInfo(Mpoint mpoint) {
    return mpoint.getValue() + "|" + mpoint.getEpochTimeInSeconds();
  }
}
