package com.sukeban.user.management;

import com.mongodb.MongoClient;
import com.sukeban.user.management.api.MongodbConsumer;
import com.sukeban.user.management.health.UserHealthCheck;
import com.sukeban.user.management.resources.UserResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class UserApplication extends Application<UserConfiguration> {

    private static final String GROUP_ID = "group1";
    private static final String CONSUMER_TOPIC = "UPDATE-USER-MANAGEMENT-DB";
    private static final String TOPIC_PRODUCER = "UPDATE-CAR-MANAGEMENT-DB";

    private static final String DB_NAME = "USER-MANAGEMENT-DB";
    private Morphia morphia;
    private MongoClient mongoClient;

    public static void main(String[] args) throws Exception {
       
        new UserApplication().run(args);
        new MongodbConsumer(DB_NAME, CONSUMER_TOPIC, GROUP_ID, TOPIC_PRODUCER).start();
    }

    @Override
    public void initialize(Bootstrap<UserConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(UserConfiguration configuration,
            Environment environment) {

        morphia = new Morphia();
        mongoClient = new MongoClient();

        // create the Datastore connecting to the default port on the local host
        final Datastore datastore = morphia.createDatastore(mongoClient, DB_NAME);
        datastore.ensureIndexes();

        environment.healthChecks().register("User", new UserHealthCheck(mongoClient));

        final UserResource resource = new UserResource(datastore, TOPIC_PRODUCER);
        environment.jersey().register(resource);

    }
}
