package modules;

import ApplicationComponents.*;
import com.google.inject.AbstractModule;
import databases.FirestoreHandler;

public class StartupModule extends AbstractModule {

    protected void configure() {
     /* Start required handlers and services */
     bind(ErrorHandler.class).asEagerSingleton();
     bind(CompressorFilter.class).asEagerSingleton();
     bind(FirestoreHandler.class).asEagerSingleton();
     bind(Application.class).asEagerSingleton();
     bind(MailerService.class).asEagerSingleton();
     bind(EmailScheduler.class).asEagerSingleton();
    }
}
