package ru.netology;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.List;

public class Main {

    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        String url = "https://api.nasa.gov/planetary/apod?api_key=6FeT6YS5HyoAyfxTCJc4osfVzYif6krlq2WsT9ol";

        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse response = httpClient.execute(request);

        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        List<NasaObject> nasaObject = mapper.readValue(response.getEntity().getContent(),
                new TypeReference<>() {
                });

        nasaObject.forEach(System.out::println);
        if (NasaObject.getMediaType() != "video") {
            url = NasaObject.getUrl();
            request = new HttpGet(url);
            response = httpClient.execute(request);

            String namePic = url.substring(url.lastIndexOf('/') + 1, url.length());

            //Файл будет сохранен в создаваемой директории "C:\\PictureNasa"

            File dirPic = new File("C:\\PictureNasa");
            File filePic = new File("C:\\PictureNasa\\" + namePic);

            if (dirPic.mkdir()) {
                System.out.println("Директория создана");
            }

            try {
                if (filePic.createNewFile()) {
                    System.out.println("Файл создан");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            try (FileOutputStream fos = new FileOutputStream(filePic)) {
                byte[] bytes = response.getEntity().getContent().readAllBytes();
                //System.out.println(bytes.length);
                fos.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Упс! Сегодня картинок нет...");
        }
        response.close();
        httpClient.close();
    }
}