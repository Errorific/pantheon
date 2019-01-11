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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import tech.pegasys.pantheon.orion.types.ReceiveContent;
import tech.pegasys.pantheon.orion.types.ReceiveResponse;
import tech.pegasys.pantheon.orion.types.SendContent;
import tech.pegasys.pantheon.orion.types.SendResponse;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test makes sure that the orion client works. It is intended to be used with a running Orion
 * instance https://github.com/PegaSysEng/orion
 *
 * <p>Before running this test:
 *
 * <ol>
 *   <li>Pull the project and run with `./gradlew run -Pargs="-g foo"`
 *   <li>Create a file `foo.conf` cloned directory with the content:
 *       <p>nodeurl = "http://127.0.0.1:8080/"
 *       <p>nodeport = 8080
 *       <p>clienturl ="http://127.0.0.1:8888/"
 *       <p>clientport = 8888
 *       <p>publickeys = ["foo.pub"]
 *       <p>privatekeys = ["foo.key"]
 *       <p>tls = "off"
 *   <li>Run Orion with `./gradlew run -Pargs="foo.conf"`
 *   <li>Modify the PUBLIC_KEY variable below with the contents of `foo.pub`
 * </ol>
 */
@Ignore
public class OrionTest {

  private static String PUBLIC_KEY = "<update_with_contents_of_foo.pub>";
  private static String PAYLOAD = "SGVsbG8sIFdvcmxkIQ==";
  private static Orion orion;
  private static Orion broken;

  @BeforeClass
  public static void setUpOnce() {
    OrionConfiguration orionConfiguration = OrionConfiguration.createDefault();
    orion = new Orion(orionConfiguration);

    orionConfiguration.setUrl("http:");
    broken = new Orion(orionConfiguration);
  }

  @Test
  public void testUpCheck() throws IOException {
    assertTrue(orion.upCheck());
  }

  @Test(expected = IOException.class)
  public void whenUpCheckFailsThrows() throws IOException {
    broken.upCheck();
  }

  @Test
  public void testSend() throws IOException {
    SendContent sc = new SendContent(PAYLOAD, PUBLIC_KEY, new String[] {PUBLIC_KEY});
    SendResponse sr = orion.send(sc);

    // example "LcF7I+UnR2XBdSxZesiYE/lTtxVfFeY4EvL9fDXb0Uo=".length() is 44
    assertEquals(44, sr.getKey().length());
  }

  @Test
  public void testReceive() throws IOException {
    SendContent sc = new SendContent(PAYLOAD, PUBLIC_KEY, new String[] {PUBLIC_KEY});
    SendResponse sr = orion.send(sc);

    ReceiveContent rc = new ReceiveContent(sr.getKey(), PUBLIC_KEY);
    ReceiveResponse rr = orion.receive(rc);

    assertEquals(PAYLOAD, rr.getPayload());
  }
}
