package ma.project.translator.jakarta.hello;

import okhttp3.*;
import java.io.InputStream;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeminiClient {

    private static final String MODEL = "gemini-2.0-flash";
    private static final String ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    private static final OkHttpClient http = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static String apiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Missing GEMINI_API_KEY environment variable");
        }
        return key;
    }

    public static String translateToDarija(String text, String sourceLang) throws Exception {
        if (sourceLang == null || sourceLang.isBlank()) {
            sourceLang = "en"; 
        }

        
        String prompt = "Translate the following " + sourceLang +
                " text into Moroccan Arabic (Darija). " +
                "Return ONLY the translation in exactly two lines: " +
                "Line 1: Darija in Arabic script. " +
                "Line 2: Darija in Latin letters. " +
                "Do not add explanations, examples, or alternatives.\n\n" +
                text;

        String bodyJson = mapper.writeValueAsString(
            Map.of(
                "contents", new Object[] {
                    Map.of(
                        "parts", new Object[] {
                            Map.of("text", prompt)
                        }
                    )
                }
            )
        );

        HttpUrl url = HttpUrl.parse(ENDPOINT)
            .newBuilder()
            .addQueryParameter("key", apiKey())
            .build();

        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(bodyJson, MediaType.parse("application/json")))
            .build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Gemini API error: " + response.code() + " - " +
                        (response.body() != null ? response.body().string() : "no body"));
            }

           
            try (InputStream is = response.body().byteStream()) {
                JsonNode root = mapper.readTree(is);
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).path("text").asText().trim();
                    }
                }
                throw new RuntimeException("Unexpected Gemini response format");
            }
        } catch (OutOfMemoryError oom) {
            throw new RuntimeException("Translation failed due to memory limits", oom);
        }
    }
}
