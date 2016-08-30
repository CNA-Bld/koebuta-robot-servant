import fi.iki.elonen.NanoHTTPD;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static java.lang.System.exit;


public class KoebutaHTTPServer extends NanoHTTPD {
    private static final Logger LOGGER = Logger.getLogger(KoebutaHTTPServer.class);

    private static KoebutaHTTPServer instance = new KoebutaHTTPServer();

    private KoebutaHTTPServer() {
        super(Integer.parseUnsignedInt(System.getProperty("port", "8081")));
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            exit(1);
        }
        LOGGER.info("API Server started at port " + getListeningPort());
    }

    public static KoebutaHTTPServer getInstance() {
        return instance;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> params = session.getParms();

        if (params.isEmpty()) {
            return newFixedLengthResponse("Koebuta Robot Servant is Ready!");
        }

        if (params.containsKey("qrcode")) {
            try {
                return newChunkedResponse(Response.Status.OK, "image/png", new FileInputStream("qrcode.png"));
            } catch (FileNotFoundException e) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "", "");
            }
        }

        if (params.containsKey("refresh")) {
            return newFixedLengthResponse(KoebutaApplication.getInstance().getKoebutaMessageCallback().reloadAutoReplies().toString());
        }

        String targetType;
        long id;
        String message;

        try {
            targetType = params.get("target");
            id = Long.parseLong(params.get("id"));
            message = params.get("msg");
            if (targetType == null || message == null || id == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "", "");
        }

        if (targetType.equals("g")) {
            KoebutaApplication.getInstance().getClient().sendMessageToGroup(id, message);
        } else if (targetType.equals("d")) {
            KoebutaApplication.getInstance().getClient().sendMessageToDiscuss(id, message);
        } else if (targetType.equals("u")) {
            KoebutaApplication.getInstance().getClient().sendMessageToFriend(id, message);
        } else {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "", "");
        }

        LOGGER.info(String.format("Sending %s to %d (%s)", message, id, targetType));
        return newFixedLengthResponse("OK");
    }
}
