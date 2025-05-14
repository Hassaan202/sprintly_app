package com.example.sprintly_app_smd_finale;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAIHelper {
    private final OpenAIClient client;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final String model;

    public interface OpenAIResponseCallback {
        void onResponse(String textResponse, String codeBlock);
    }

    public OpenAIHelper(String apiKey, String model) {
        // Initialize the client with the provided API key
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
        this.model = model;
    }

    public void askCodingQuestion(String userQuestion, OpenAIResponseCallback callback) {
        executorService.execute(() -> {
            try {
                // Create the parameters for the OpenAI API call
                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .addUserMessage("Please answer this coding question. If your response includes code, wrap the code in triple backticks: " + userQuestion)
                        .model(model)
                        .build();

                // Make the API call
                ChatCompletion chatCompletion = client.chat().completions().create(params);

                // Process the response
                String responseText = chatCompletion.choices().get(0).message().content().orElse("N/A");
                String[] parsedResponse = extractCodeBlock(responseText);
                String textResponse = parsedResponse[0];
                String codeBlock = parsedResponse[1];

                // Return the response on the main thread
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onResponse(textResponse, codeBlock));

            } catch (Exception e) {
                Log.e("OpenAIHelper", "Error getting response", e);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onResponse("Sorry, I encountered an error processing your question: " + e.getMessage(), null));
            }
        });
    }

    private static String[] extractCodeBlock(String response) {
        Pattern pattern = Pattern.compile("```(?:java|kotlin|xml|\\w+)?\\s*([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(response);

        StringBuilder textResponse = new StringBuilder(response);
        String codeBlock = null;

        if (matcher.find()) {
            codeBlock = matcher.group(1).trim();
            textResponse = new StringBuilder(response.substring(0, matcher.start()).trim());
            if (matcher.end() < response.length()) {
                textResponse.append(" ").append(response.substring(matcher.end()).trim());
            }
        }

        return new String[]{textResponse.toString(), codeBlock};
    }
}