package com.example.project;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAiApi {
    // OpenAI Chat Completion API 엔드포인트
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    Call<OpenAiUtils.ChatResponse> getRecommendation(@Body OpenAiUtils.ChatRequest request);
}