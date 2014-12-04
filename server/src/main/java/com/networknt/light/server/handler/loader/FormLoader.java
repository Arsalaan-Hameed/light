package com.networknt.light.server.handler.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.light.server.LightServer;
import com.networknt.light.util.ServiceLocator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Created by steve on 23/08/14.
 *
 * This has to be called
 */
public class FormLoader extends Loader {
    static public String formFolder = "form";

    public static void loadForm() throws Exception {
        File folder = getFileFromResourceFolder(formFolder);
        if(folder != null) {
            LightServer.start();
            httpclient = HttpClients.createDefault();
            // login as owner here
            login();
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                loadFormFile(listOfFiles[i]);
            }
            httpclient.close();

            LightServer.stop();
        }
    }

    private static void loadFormFile(File file) {
        Scanner scan = null;
        try {
            scan = new Scanner(file, Loader.encoding);
            // the content is only the data portion. convert to map
            String content = scan.useDelimiter("\\Z").next();
            HttpPost httpPost = new HttpPost("http://injector:8080/api/rs");
            httpPost.addHeader("Authorization", "Bearer " + jwt);
            StringEntity input = new StringEntity(content);
            input.setContentType("application/json");
            httpPost.setEntity(input);
            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
                System.out.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();
                BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
                String json = "";
                String line = "";
                while ((line = rd.readLine()) != null) {
                    json = json + line;
                }
                System.out.println("json = " + json);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            /*
            ODatabaseDocumentTx db = ServiceLocator.getInstance().getDb();
            OSchema schema = db.getMetadata().getSchema();
            for (OClass oClass : schema.getClasses()) {
                System.out.println(oClass.getName());
            }
            String formId = file.getName();
            try {
                db.begin();
                // remove the document for the class if there are any.
                OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Form where id = ?");
                List<ODocument> result = db.command(query).execute(formId);
                for (ODocument form : result) {
                    form.delete();
                }
                ODocument doc = new ODocument(schema.getClass("Form"));
                doc.field("id", formId);
                doc.field("content", content);
                doc.save();
                db.commit();
                System.out.println("Form " + formId + " is loaded!");
            } catch (Exception e) {
                db.rollback();
                e.printStackTrace();
            } finally {
                db.close();
            }
            */
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (scan != null) scan.close();
        }
    }
}
