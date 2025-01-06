package camp.coconut.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationService {

    private final OpenAiImageModel imageModel;
    private final S3AsyncClient s3AsyncClient;
    private final ExecutorService executorService;

    private final static String S3_BASE_URL = "https://%s.s3.amazonaws.com/%s";
    private final static String BUCKET_NAME = "ai-image-test-gen";

    /**
     * Use OpenAI to generate an image, then upload it to AWS S3, returning the URL of the stored image.
     */
    public Mono<String> generateImage(String description) {
        ImagePrompt imagePrompt = new ImagePrompt(description,
                OpenAiImageOptions.builder()
                        .withQuality("standard")
                        .withN(1)
                        .withHeight(1024)
                        .withWidth(1024)
                        .withResponseFormat("b64_json").build());

        return Mono.fromCallable(() -> imageModel.call(imagePrompt))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getResult().getOutput().getB64Json())
                .as(this::uploadBase64ToS3)
                .onErrorResume(this::handleError);
    }

    /**
     * Upload a Base64-encoded image to AWS S3.
     */
    private Mono<String> uploadBase64ToS3(Mono<String> base64ImageMono) {
        return base64ImageMono.flatMap(base64Image -> {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            InputStream imageStream = new ByteArrayInputStream(imageBytes);
            String keyName = "generated-images/" + UUID.randomUUID() + ".png";

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(keyName)
                    .build();

            AsyncRequestBody requestBody = AsyncRequestBody.fromInputStream(imageStream, (long) imageBytes.length, executorService);

            return Mono.fromFuture(() -> s3AsyncClient.putObject(putObjectRequest, requestBody))
                    .map(response -> s3Url(keyName));
        });
    }

    /**
     * Constructs the S3 URL for the uploaded image.
     */
    private String s3Url(String keyName) {
        return String.format(S3_BASE_URL, BUCKET_NAME, keyName);
    }

    /**
     * Handles errors during the image generation or upload process.
     */
    private Mono<String> handleError(Throwable e) {
        log.error("Error generating and saving image", e);
        return Mono.error(new RuntimeException("Image generation failed. Please try again later."));
    }
}
