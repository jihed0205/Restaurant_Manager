package com.example.restaurantapp;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class EmailService {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String API_KEY = "";
    private static final String SENDER_EMAIL = "jihedlouini@gmail.com";
    private static final String SENDER_NAME = "Restaurant Manager";

    public static void sendWelcomeEmail(String userName, String userEmail) {
        String htmlContent = "<h2>Hello " + userName + ",</h2>" +
                "<p>Welcome to Restaurant Manager! Your account has been successfully created.</p>" +
                "<p>We are glad to have you on board.</p><br>" +
                "<p>— Restaurant Manager Team</p>";
        
        sendEmail(userName, userEmail, "Welcome to Restaurant Manager", htmlContent);
    }

    public static void sendPasswordResetEmail(String userName, String userEmail) {
        String htmlContent = "<h2>Hello " + userName + ",</h2>" +
                "<p>We received a request to reset your Restaurant Manager password.</p>" +
                "<p>Please check your Firebase email for the reset link.</p>" +
                "<p>If you did not request this, ignore this email.</p><br>" +
                "<p>— Restaurant Manager Team</p>";
        
        sendEmail(userName, userEmail, "Reset your Restaurant Manager password", htmlContent);
    }

    private static void sendEmail(String userName, String userEmail, String subject, String htmlContent) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject sender = new JSONObject();
                sender.put("name", SENDER_NAME);
                sender.put("email", SENDER_EMAIL);

                JSONObject recipient = new JSONObject();
                recipient.put("email", userEmail);
                recipient.put("name", userName);

                JSONArray to = new JSONArray();
                to.put(recipient);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("sender", sender);
                jsonBody.put("to", to);
                jsonBody.put("subject", subject);
                jsonBody.put("htmlContent", htmlContent);

                RequestBody body = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url(BREVO_URL)
                        .addHeader("api-key", API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        System.err.println("Brevo Error: " + response.code() + " " + (response.body() != null ? response.body().string() : response.message()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
