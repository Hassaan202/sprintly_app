package com.example.sprintly_app_smd_finale;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.GenerateContentResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GeminiHelper {
    private final GenerativeModel model;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface GeminiResponseCallback {
        void onResponse(String textResponse, String codeBlock);
    }

    public GeminiHelper(String modelName, String apiKey) {
        this.model = new GenerativeModel(modelName, apiKey);
    }

    public void askCodingQuestion(String userQuestion, GeminiResponseCallback callback) {
        executorService.execute(() -> {
            try {
                String prompt = "Please answer this coding question. If your response includes code, wrap the code in triple backticks: " + userQuestion;
                model.generateContent(prompt, new ContinuationAdapter(callback));
            } catch (Exception e) {
                Log.e("GeminiHelper", "Error getting response", e);
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

    private static class ContinuationAdapter implements Continuation<GenerateContentResponse> {
        private final GeminiResponseCallback callback;

        public ContinuationAdapter(GeminiResponseCallback callback) {
            this.callback = callback;
        }

        @Override
        public CoroutineContext getContext() {
            return EmptyCoroutineContext.INSTANCE;
        }

        @Override
        public void resumeWith(Object o) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            if (o instanceof kotlin.Result) {
                try {
                    // Attempt to cast the result to GenerateContentResponse
                    // GenerateContentResponse response = (GenerateContentResponse) ((kotlin.Result<?>) o).getValue();
                    GenerateContentResponse response = null;
                    if (response != null && response.getText() != null) {
                        String responseText = response.getText();
                        String[] parsedResponse = extractCodeBlock(responseText);
                        String textResponse = parsedResponse[0];
                        String codeBlock = parsedResponse[1];
                        mainHandler.post(() -> callback.onResponse(textResponse, codeBlock));
                    } else {
                        mainHandler.post(() -> callback.onResponse("Sorry, I couldn't generate a response.", null));
                    }
                } catch (Throwable e) {
                    mainHandler.post(() -> callback.onResponse("Error: " + e.getMessage(), null));
                }
            } else {
                mainHandler.post(() -> callback.onResponse("Error: unexpected result type", null));
            }
        }
    }
}