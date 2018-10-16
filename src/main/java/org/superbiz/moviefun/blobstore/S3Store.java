package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {

    private final AmazonS3 s3;
    private final String bucketName;
    private final Tika tika = new Tika();

    public S3Store(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }


    @Override
    public void put(Blob blob) throws IOException {
        s3.putObject(bucketName, blob.name, blob.inputStream, new ObjectMetadata());
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        if (!s3.doesObjectExist(bucketName, name)) {
            return Optional.empty();
        }

        S3Object s3Object = s3.getObject(bucketName, name);
        S3ObjectInputStream content = s3Object.getObjectContent();

        byte[] bytes = IOUtils.toByteArray(content);

        return Optional.of(new Blob(
                name,
                new ByteArrayInputStream(bytes),
                tika.detect(bytes)
        ));
    }

    @Override
    public void deleteAll() {
        // ...
        // Delete the sample objects.
        DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucketName);
//                .withKeys(keys)
//                .withQuiet(false);

        // Verify that the objects were deleted successfully.
        DeleteObjectsResult delObjRes = s3.deleteObjects(multiObjectDeleteRequest);
        int successfulDeletes = delObjRes.getDeletedObjects().size();
        System.out.println(successfulDeletes + " objects successfully deleted.");
    }
}