package ma.project.translator.jakarta.hello;

import jakarta.ws.rs.core.Application;

public class HelloApplication extends Application {
    // This class intentionally does not declare @ApplicationPath
    // so that AppConfig.java (which has @ApplicationPath) is the single JAX-RS application
}