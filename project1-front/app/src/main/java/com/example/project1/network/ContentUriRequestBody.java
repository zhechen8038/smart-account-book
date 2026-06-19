package com.example.project1.network;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ContentUriRequestBody extends RequestBody {

    private final ContentResolver resolver;
    private final Uri uri;
    private final MediaType mediaType;

    public ContentUriRequestBody(
            ContentResolver resolver,
            Uri uri,
            String mimeType
    ) {
        this.resolver = resolver;
        this.uri = uri;
        this.mediaType = MediaType.parse(
                mimeType == null ? "image/jpeg" : mimeType
        );
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public long contentLength() {
        try {
            return resolver.openAssetFileDescriptor(uri, "r").getLength();
        } catch (Exception exception) {
            return -1;
        }
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        try (InputStream inputStream = resolver.openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("无法读取选择的图片");
            }

            byte[] buffer = new byte[8192];
            int count;

            while ((count = inputStream.read(buffer)) != -1) {
                sink.write(buffer, 0, count);
            }
        }
    }
}