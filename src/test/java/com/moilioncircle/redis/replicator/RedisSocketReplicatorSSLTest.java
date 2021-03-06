/*
 * Copyright 2016-2017 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moilioncircle.redis.replicator;

import com.moilioncircle.redis.replicator.rdb.RdbListener;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
@SuppressWarnings("resource")
public class RedisSocketReplicatorSSLTest {

    private static void setJvmTrustStore(String trustStoreFilePath, String trustStoreType) {
        Assert.assertTrue(String.format("Could not find trust store at '%s'.", trustStoreFilePath),
                new File(trustStoreFilePath).exists());
        System.setProperty("javax.net.ssl.trustStore", trustStoreFilePath);
        System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
    }

//    @Test
//    public void testSsl() throws IOException {
//        setJvmTrustStore("src/test/resources/keystore/truststore.jceks", "jceks");
//        Replicator replicator = new RedisReplicator("127.0.0.1", 56379, Configuration.defaultSetting().setSsl(true));
//        final AtomicInteger acc = new AtomicInteger(0);
//        Jedis jedis = null;
//        try {
//            jedis = new Jedis("127.0.0.1", 6379);
//            jedis.set("ssl", "true");
//        } finally {
//            jedis.close();
//        }
//        replicator.addRdbListener(new RdbListener() {
//            @Override
//            public void preFullSync(Replicator replicator) {
//            }
//
//            @Override
//            public void handle(Replicator replicator, KeyValuePair<?> kv) {
//                if(kv.getKey().equals("ssl")) acc.incrementAndGet();
//            }
//
//            @Override
//            public void postFullSync(Replicator replicator, long checksum) {
//                Jedis jedis = null;
//                try {
//                    jedis = new Jedis("127.0.0.1", 6379);
//                    jedis.del("ssl");
//                } finally {
//                    jedis.close();
//                }
//                try {
//                    replicator.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        replicator.addCloseListener(new CloseListener() {
//            @Override
//            public void handle(Replicator replicator) {
//                System.out.println("close testSsl");
//                assertEquals(1, acc.get());
//            }
//        });
//        replicator.open();
//    }
//
//    @Test
//    public void testSsl1() throws IOException {
//        setJvmTrustStore("src/test/resources/keystore/truststore.jceks", "jceks");
//        Replicator replicator = new RedisReplicator("localhost", 56379,
//                Configuration.defaultSetting().setSsl(true)
//                        .setReadTimeout(0)
//                        .setSslSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault())
//                        .setHostnameVerifier(new BasicHostnameVerifier())
//                        .setSslParameters(new SSLParameters()));
//        final AtomicInteger acc = new AtomicInteger(0);
//        Jedis jedis = null;
//        try {
//            jedis = new Jedis("127.0.0.1", 6379);
//            jedis.set("ssl1", "true");
//        } finally {
//            jedis.close();
//        }
//        replicator.addRdbListener(new RdbListener() {
//            @Override
//            public void preFullSync(Replicator replicator) {
//            }
//
//            @Override
//            public void handle(Replicator replicator, KeyValuePair<?> kv) {
//                if(kv.getKey().equals("ssl1")) acc.incrementAndGet();
//            }
//
//            @Override
//            public void postFullSync(Replicator replicator, long checksum) {
//                Jedis jedis = null;
//                try {
//                    jedis = new Jedis("127.0.0.1", 6379);
//                    jedis.del("ssl1");
//                } finally {
//                    jedis.close();
//                }
//                try {
//                    replicator.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        replicator.addCloseListener(new CloseListener() {
//            @Override
//            public void handle(Replicator replicator) {
//                System.out.println("close testSsl1");
//                assertEquals(1, acc.get());
//            }
//        });
//        replicator.open();
//    }

    @Test
    public void testSsl2() throws Exception {
        setJvmTrustStore("src/test/resources/keystore/truststore.jceks", "jceks");
        Replicator replicator = new RedisReplicator("localhost", 56379,
                Configuration.defaultSetting().setSsl(true)
                        .setHostnameVerifier(new BasicHostnameVerifier())
                        .setSslParameters(new SSLParameters())
                        .setSslSocketFactory(createTrustStoreSslSocketFactory()));
        final AtomicInteger acc = new AtomicInteger(0);
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            jedis.set("ssl2", "true");
        }
        replicator.addRdbListener(new RdbListener() {
            @Override
            public void preFullSync(Replicator replicator) {
            }

            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                if (kv.getKey().equals("ssl2")) acc.incrementAndGet();
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
                    jedis.del("ssl2");
                }
                try {
                    replicator.close();
                } catch (IOException e) {
                }
            }
        });
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                System.out.println("close testSsl2");
            }
        });
        replicator.open();
        assertEquals(1, acc.get());
    }

    @Test
    public void testSsl3() throws Exception {
        setJvmTrustStore("src/test/resources/keystore/truststore.jceks", "jceks");
        Replicator replicator = new RedisReplicator("localhost", 56379,
                Configuration.defaultSetting().setSsl(true)
                        .setHostnameVerifier(new BasicHostnameVerifier())
                        .setSslParameters(new SSLParameters())
                        .setSslSocketFactory(createTrustNoOneSslSocketFactory()));
        final AtomicInteger acc = new AtomicInteger(0);
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            jedis.set("ssl3", "true");
        }
        replicator.addRdbListener(new RdbListener() {
            @Override
            public void preFullSync(Replicator replicator) {

            }

            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                if (kv.getKey().equals("ssl3")) {
                    acc.incrementAndGet();
                }
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
                    jedis.del("ssl3");
                }
                try {
                    replicator.close();
                } catch (IOException e) {
                }
            }
        });
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                System.out.println("close testSsl3");
            }
        });
        try {
            replicator.open();
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testSsl4() throws Exception {
        setJvmTrustStore("src/test/resources/keystore/truststore.jceks", "jceks");
        Replicator replicator = new RedisReplicator("127.0.0.1", 56379,
                Configuration.defaultSetting().setSsl(true)
                        .setHostnameVerifier(new BasicHostnameVerifier())
                        .setSslParameters(new SSLParameters())
                        .setSslSocketFactory(createTrustStoreSslSocketFactory()));
        final AtomicInteger acc = new AtomicInteger(0);
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            jedis.set("ssl4", "true");
        }
        replicator.addRdbListener(new RdbListener() {
            @Override
            public void preFullSync(Replicator replicator) {
            }

            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
                if (kv.getKey().equals("ssl4")) acc.incrementAndGet();
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
                    jedis.del("ssl4");
                }
                try {
                    replicator.close();
                } catch (IOException e) {
                }
            }
        });
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                System.out.println("close testSsl4");
            }
        });
        try {
            replicator.open();
            fail();
        } catch (IOException e) {
            if (!(e instanceof SocketException)) {
                fail();
            }
        }
        assertEquals(0, acc.get());
    }

    private static SSLSocketFactory createTrustStoreSslSocketFactory() throws Exception {

        KeyStore trustStore = KeyStore.getInstance("jceks");
        try (InputStream inputStream = new FileInputStream("src/test/resources/keystore/truststore.jceks")) {
            trustStore.load(inputStream, null);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
        trustManagerFactory.init(trustStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext.getSocketFactory();
    }

    private static SSLSocketFactory createTrustNoOneSslSocketFactory() throws Exception {
        TrustManager[] unTrustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        throw new RuntimeException(new InvalidAlgorithmParameterException());
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        throw new RuntimeException(new InvalidAlgorithmParameterException());
                    }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, unTrustManagers, new SecureRandom());
        return sslContext.getSocketFactory();
    }

    private static class BasicHostnameVerifier implements HostnameVerifier {

        private static final String COMMON_NAME_RDN_PREFIX = "CN=";

        @Override
        public boolean verify(String hostname, SSLSession session) {
            X509Certificate peerCertificate;
            try {
                peerCertificate = (X509Certificate) session.getPeerCertificates()[0];
            } catch (SSLPeerUnverifiedException e) {
                throw new IllegalStateException("The session does not contain a peer X.509 certificate.");
            }
            String peerCertificateCN = getCommonName(peerCertificate);
            return hostname.equals(peerCertificateCN);
        }

        private String getCommonName(X509Certificate peerCertificate) {
            String subjectDN = peerCertificate.getSubjectDN().getName();
            String[] dnComponents = subjectDN.split(",");
            for (String dnComponent : dnComponents) {
                if (dnComponent.startsWith(COMMON_NAME_RDN_PREFIX)) {
                    return dnComponent.substring(COMMON_NAME_RDN_PREFIX.length());
                }
            }
            throw new IllegalArgumentException("The certificate has no common name.");
        }
    }
}
