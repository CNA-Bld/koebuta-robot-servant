import com.scienjus.smartqq.client.SmartQQClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Scanner;


public class KoebutaApplication {
    private static final Logger LOGGER = Logger.getLogger(KoebutaApplication.class);

    private static KoebutaApplication instance = new KoebutaApplication();
    private SmartQQClient client;
    private KoebutaMessageCallback koebutaMessageCallback;

    public static void main(String[] args) throws Exception {
        KoebutaApplication.getInstance().start();
    }

    public static KoebutaApplication getInstance() {
        return instance;
    }

    public void start() throws IOException {
        LOGGER.info("Starting API Server");
        KoebutaHTTPServer.getInstance();

        koebutaMessageCallback = new KoebutaMessageCallback();
        try (SmartQQClient client = new SmartQQClient(koebutaMessageCallback)) {
            this.client = client;
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
    }

    public SmartQQClient getClient() {
        return client;
    }

    public KoebutaMessageCallback getKoebutaMessageCallback() {
        return koebutaMessageCallback;
    }
}
