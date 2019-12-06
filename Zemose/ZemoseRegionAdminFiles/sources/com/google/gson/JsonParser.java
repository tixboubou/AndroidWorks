package com.google.gson;

import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public final class JsonParser {
    public JsonElement parse(String json) throws JsonSyntaxException {
        return parse((Reader) new StringReader(json));
    }

    public JsonElement parse(Reader json) throws JsonIOException, JsonSyntaxException {
        try {
            JsonReader jsonReader = new JsonReader(json);
            JsonElement element = parse(jsonReader);
            if (!element.isJsonNull()) {
                if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                    throw new JsonSyntaxException("Did not consume the entire document.");
                }
            }
            return element;
        } catch (MalformedJsonException e) {
            throw new JsonSyntaxException((Throwable) e);
        } catch (IOException e2) {
            throw new JsonIOException((Throwable) e2);
        } catch (NumberFormatException e3) {
            throw new JsonSyntaxException((Throwable) e3);
        }
    }

    public JsonElement parse(JsonReader json) throws JsonIOException, JsonSyntaxException {
        boolean lenient = json.isLenient();
        json.setLenient(true);
        try {
            JsonElement parse = Streams.parse(json);
            json.setLenient(lenient);
            return parse;
        } catch (StackOverflowError e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed parsing JSON source: ");
            sb.append(json);
            sb.append(" to Json");
            throw new JsonParseException(sb.toString(), e);
        } catch (OutOfMemoryError e2) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Failed parsing JSON source: ");
            sb2.append(json);
            sb2.append(" to Json");
            throw new JsonParseException(sb2.toString(), e2);
        } catch (Throwable th) {
            json.setLenient(lenient);
            throw th;
        }
    }
}
