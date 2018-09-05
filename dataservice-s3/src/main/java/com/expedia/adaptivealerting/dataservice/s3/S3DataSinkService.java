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
import java.util.ArrayList;
import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroSchemaConverter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.Schema;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.avro.AvroWriteSupport;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.file.DataFileWriter;
import com.expedia.adaptivealerting.dataservice.s3.MetricData;

public class S3DataSinkService implements DataSinkService {

    private static final CompressionCodecName COMPRESSION_CODEC_NAME = CompressionCodecName.GZIP;
    private static final String S3_DATE_FORMAT = "yyyy-MM-dd";
    private static final int PARQUET_BLOCK_SIZE = 256 * 1024 * 1024;
    private static final int PARQUET_PAGE_SIZE = 1 * 1024 * 1024;

    private String toS3FormatStringFromInstant(Instant instant) {
        Date myDate = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat(S3_DATE_FORMAT);
        return formatter.format(myDate);
    }

    public void put(List<Mpoint> mPoints) {
        String s3FormatDate = toS3FormatStringFromInstant(Instant.now());
        for (Mpoint mpoint : mPoints) {
            String site = mpoint.getMetric().getTag("site");
            String lob = mpoint.getMetric().getTag("lob");
            String directoryName = lob + "/" + site;
            String completeFilePath = lob + "/" + site + "/" + s3FormatDate;
            //File file = writeToFile(completeFilePath, directoryName, mpoint.getValue());
        }

        List<MetricData> metricDataList = new ArrayList<MetricData>();
        for(Mpoint mPoint: mPoints){
            MetricData mData = new MetricData();
            mData.setKey("dummy");
            mData.setLob(mPoint.getMetric().getTag("lob"));
            mData.setPos(mPoint.getMetric().getTag("site"));
            mData.setTags(mPoint.getMetric().getTags());
            mData.setTimestamp(mPoint.getEpochTimeInSeconds());
            mData.setValue(Math.round(mPoint.getValue()));
            metricDataList.add(mData);
        }

        File avroOutput = new File(s3FormatDate + ".avro");
        String parquetFilename = s3FormatDate + ".parquet";

        convertToAvro(avroOutput, metricDataList);
        convertAvroToParquet(parquetFilename, avroOutput);
        storeInS3();
    }

    private void convertToAvro(File avroOutput, List<MetricData> metricDataList) {
        try {
            DatumWriter<MetricData> bdPersonDatumWriter = new SpecificDatumWriter<MetricData>(
                    MetricData.class);
            DataFileWriter<MetricData> dataFileWriter = new DataFileWriter<MetricData>(bdPersonDatumWriter);
            dataFileWriter.create(new MetricData().getSchema(), avroOutput);
            for (MetricData a : metricDataList) {
                dataFileWriter.append(a);
            }
            dataFileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void convertAvroToParquet(String parquetFileName, File avroOutput) {

        File f = new File(parquetFileName);
        if (f.exists()) {
            f.delete();
        }

        try {
            GenericDatumReader<Object> greader = new GenericDatumReader<Object>();
            FileReader<Object> fileReader = DataFileReader.openReader(avroOutput, greader);
            Schema avroSchema = fileReader.getSchema();

            MessageType parquetSchema = new AvroSchemaConverter().convert(avroSchema);
            AvroWriteSupport writeSupport = new AvroWriteSupport(parquetSchema, avroSchema);

            Path outputPath = new Path(parquetFileName);

            AvroParquetWriter parquetWriter = new AvroParquetWriter(
                    outputPath,
                    avroSchema,
                    COMPRESSION_CODEC_NAME,
                    PARQUET_BLOCK_SIZE,
                    PARQUET_PAGE_SIZE);

            DataFileReader<GenericRecord> reader = new DataFileReader<GenericRecord>(avroOutput,
                    new GenericDatumReader<GenericRecord>());

            while (reader.hasNext()) {
                GenericRecord record = reader.next();
                parquetWriter.write(record);
            }
            parquetWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeInS3() {
        String fileName = toS3FormatStringFromInstant(Instant.now());
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion("us-west-2").build();
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
