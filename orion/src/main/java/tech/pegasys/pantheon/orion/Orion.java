/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.orion;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Orion {
  private static final MediaType JSON = MediaType.parse("application/json");
  private static final ObjectMapper om = new ObjectMapper();
  private static final Logger LOG = LogManager.getLogger();

  private String url;
  private OkHttpClient client;

  public Orion(final OrionConfiguration orionConfiguration) {
    this.url = orionConfiguration.getUrl();
    this.client = new OkHttpClient();
  }

  public Boolean upCheck() throws IOException {
    Request request = new Request.Builder().url(url + "/upcheck").get().build();

    try (Response response = client.newCall(request).execute()) {
      return response.isSuccessful();
    } catch (IOException e) {
      LOG.error("Orion failed to execute upcheck");
      throw new IOException("Failed to perform upcheck", e);
    }
  }

  public String send(final SendContent content) throws IOException {
    return executePost("/send", om.writeValueAsString(content));
  }

  public String receive(final ReceiveContent content) throws IOException {
    return executePost("/receive", om.writeValueAsString(content));
  }

  private String executePost(final String path, final String content) throws IOException {
    OkHttpClient client = new OkHttpClient();

    RequestBody body = RequestBody.create(JSON, content);
    Request request = new Request.Builder().url(url + path).post(body).build();

    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    } catch (IOException e) {
      LOG.error("Orion failed to execute ", path);
      throw new IOException("Failed to execute post", e);
    }
  }
}
