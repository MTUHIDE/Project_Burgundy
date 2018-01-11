package controllers.Databases;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.inject.Inject;
import com.google.inject.Provider;
import play.api.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FirestoreDB implements Provider<Firestore> {
    private Environment environment;

    @Inject
    public FirestoreDB(Environment environment){
        this.environment = environment;
    }

    @Override
    public Firestore get() {
        File serviceAccount = environment.getFile("conf/credentials.json");
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccount));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert credentials != null;
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .build();
        if (!(FirebaseApp.getApps().size() > 0)) {
            FirebaseApp.initializeApp(options);
        }
        return FirestoreClient.getFirestore();
    }
}
