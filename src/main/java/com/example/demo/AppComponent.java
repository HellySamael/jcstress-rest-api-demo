package com.example.demo;

import com.example.demo.adapter.in.web.LikeController;
import dagger.Component;
import jakarta.inject.Singleton;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    LikeController likeController();
    // Potentially other objects that are entry points for other parts of the application
}
