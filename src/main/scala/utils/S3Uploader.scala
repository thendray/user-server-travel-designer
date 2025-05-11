package utils

import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.net.URI
import java.util.{Base64, UUID}

object S3Uploader {

  def uploadToYandexCloudObjectStorage(
      bytes: String,
      bucket: String = "travel-designer",
      ak: String = "YCAJEF8iMzZIZk51BF-Gedpvb",
      sk: String = "YCMAzvPvq_MPS6FE-k1UjyoZPhFUrMKmkDwJu-vn",
      region: String = "ru-central1-b"): String = {
    if (bytes.startsWith("http")) bytes
    else {
      val contentType = bytes.split(';')(0).substring(5)
      println(s"contentType: $contentType")

      val endpoint = "https://storage.yandexcloud.net"
      val key = s"uploads-prof/${UUID.randomUUID()}-pic.${contentType.split('/')(1)}"

      val credentials = AwsBasicCredentials.create(ak, sk)
      val s3 = S3Client
        .builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .endpointOverride(URI.create(endpoint))
        .build()

      val req = PutObjectRequest
        .builder()
        .bucket(bucket)
        .key(key)
        .contentType(contentType)
        .acl("public-read")
        .build()

      val decode = Base64.getDecoder.decode(bytes.split(',')(1))
      s3.putObject(req, RequestBody.fromBytes(decode))

      val url = s"https://storage.yandexcloud.net/$bucket/$key"

      s3.close()
      url
    }
  }

}
