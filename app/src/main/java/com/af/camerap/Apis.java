package com.af.camerap;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/09/02 - 18:22
 * Author     Payne.
 * About      类描述：
 */
public class Apis {
    public interface GitHubService {
        @GET("users/{user}/repos")
        Call<List<String>> listRepos(@Path("user") String user);
    }

    public void test(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .build();
        GitHubService service = retrofit.create(GitHubService.class);
        Call<List<String>> repos = service.listRepos("octocat");
    }
}