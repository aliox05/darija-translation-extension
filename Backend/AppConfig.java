package ma.project.translator.jakarta.hello;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;
import java.util.HashSet;

@ApplicationPath("/rest")
public class AppConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(TranslatorResource.class);
        classes.add(SignupResource.class); //
        classes.add(LoginResource.class);
        classes.add(CORSFilter.class);
        return classes;
    }
}
