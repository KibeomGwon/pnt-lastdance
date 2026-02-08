import { S3Client, GetObjectCommand } from "@aws-sdk/client-s3";
import { getSignedUrl } from "@aws-sdk/s3-request-presigner";
import dotenv from "dotenv";

dotenv.config();

const region = process.env.AWS_REGION;
const accessKeyId = process.env.AWS_ACCESS_KEY;
const secretAccessKey = process.env.AWS_SECRET_KEY;
const bucketName = process.env.AWS_S3_BUCKET_NAME;

const clientConfig = {
    region: region,
};

if (accessKeyId && secretAccessKey) {
    clientConfig.credentials = {
        accessKeyId: accessKeyId,
        secretAccessKey: secretAccessKey,
    };
}

const s3Client = new S3Client(clientConfig);

export const getPresignedUrl = async (key) => {
    // 1. 유효성 검사: key가 없거나, 'string'인 경우, 또는 'string'으로 끝나는 경우 처리
    if (!key || key === "string" || key.endsWith("/string")) {
        return null;
    }

    // 2. 이미 http로 시작하는 URL이면 그대로 반환
    if (key.startsWith("http")) return key;

    try {
        const command = new GetObjectCommand({
            Bucket: bucketName,
            Key: key,
        });

        // 3. Presigned URL 생성 (1시간 유효)
        // AWS SDK v3는 기본적으로 가상 호스팅 스타일(Virtual Hosted-Style)을 사용하므로
        // https://<bucket-name>.s3.<region>.amazonaws.com/<key> 형태가 되어야 함.
        const url = await getSignedUrl(s3Client, command, { expiresIn: 3600 });
        return url;
    } catch (error) {
        return key; // 실패 시 원본 키 반환 (또는 null)
    }
};
