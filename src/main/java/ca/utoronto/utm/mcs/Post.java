package ca.utoronto.utm.mcs;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Post implements HttpHandler {
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> collection;

    @Inject
    public Post(MongoDatabase mongoDatabase, MongoCollection collection) {
        this.mongoDatabase = mongoDatabase;
        this.collection = collection;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            if (httpExchange.getRequestMethod().equals("PUT")) {
                handlePut(httpExchange);
            } else if (httpExchange.getRequestMethod().equals("GET")) {
                handleGet(httpExchange);
            } else if (httpExchange.getRequestMethod().equals("DELETE")) {
                handleDelete(httpExchange);
            } else {
                httpExchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            httpExchange.sendResponseHeaders(500, -1);
        }
    }

    private Document createBlogPost(String title, String author, String content, List<String> tags) throws JSONException {
        Document doc= new Document().append("title", title)
                    .append("author", author)
                    .append("content", content)
                    .append("tags", tags);

        return doc;
    }

    private void handlePut(HttpExchange httpExchange) throws IOException, JSONException {
        String body = Utils.convert(httpExchange.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        Document dbObject;

        try {
            List<String> tags;
            if(deserialized.getString("tags").equals("[]")) {
                tags = new ArrayList<>();
            } else{
                tags = new ArrayList<>(Arrays.asList(deserialized.getString("tags").replaceAll("]|\\\"|\\[", "").split(",")));
            }

            dbObject = createBlogPost(deserialized.getString("title"),
                    deserialized.getString("author"),
                    deserialized.getString("content"),
                    tags);
        } catch (JSONException e) {
            httpExchange.sendResponseHeaders(400, -1);
            return;
        }

        JSONObject deserializedResponse = new JSONObject();
        collection.insertOne(dbObject);
        ObjectId id = (ObjectId)dbObject.get( "_id" );

        String responseBody = deserializedResponse.put("_id", id).toString();
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, responseBody.length());
        OutputStream outputStream = httpExchange.getResponseBody();
        try {
            outputStream.write(responseBody.getBytes(Charset.defaultCharset()));
        } finally {
            outputStream.close();
        }

    }

    private void handleGet(HttpExchange httpExchange) throws JSONException, IOException {
        String body = Utils.convert(httpExchange.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        FindIterable<Document> documents;
        ArrayList<String> objectList = new ArrayList<>();

        if(deserialized.has("_id")) {
            try {
                documents = collection.find(new Document().append("_id",
                        new ObjectId(deserialized.getString("_id"))));

                if (documents.first() == null) {
                    httpExchange.sendResponseHeaders(404, -1);
                    return;
                }
            } catch (IllegalArgumentException e) {
                httpExchange.sendResponseHeaders(404, -1);
                return;
            }

            objectList.add(documents.first().toJson());

        } else {
            try {
                documents = collection.find(new Document().append("title",
                        deserialized.getString("title")));
                if(documents.first() == null) {
                    httpExchange.sendResponseHeaders(404, -1);
                    return;
                }
            } catch (IllegalArgumentException e) {
                httpExchange.sendResponseHeaders(404, -1);
                return;
            } catch (JSONException e) {
                httpExchange.sendResponseHeaders(400, -1);
                return;
            }

            for(Document document : documents) {
                objectList.add(document.toJson());
            }
        }
        String responseBody = objectList.toString();
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, responseBody.length());
        OutputStream outputStream = httpExchange.getResponseBody();
        try {
            outputStream.write(responseBody.getBytes(Charset.defaultCharset()));
        } finally { 
            outputStream.close(); 
        }
    }

    private void handleDelete(HttpExchange httpExchange) throws JSONException, IOException {
        String body = Utils.convert(httpExchange.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        try {
            if (collection.find(new Document().append("_id", new ObjectId(deserialized.getString("_id")))).first() != null) {
                collection.deleteOne(new Document().append("_id", new ObjectId(deserialized.getString("_id"))));
                httpExchange.sendResponseHeaders(200, -1);
            } else {
                httpExchange.sendResponseHeaders(404, -1);
            }
        } catch (JSONException e) {
            httpExchange.sendResponseHeaders(400, -1);
            return;
        } catch (IllegalArgumentException e) {
            httpExchange.sendResponseHeaders(404, -1);
        }
    }
}
