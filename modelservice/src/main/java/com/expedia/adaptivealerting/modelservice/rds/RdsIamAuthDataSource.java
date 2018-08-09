/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.modelservice.rds;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Autowired;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;
import com.expedia.adaptivealerting.modelservice.DatabaseSettings;
import com.expedia.adaptivealerting.modelservice.util.ThreadUtil;

/**
 * @author kashah
 *
 */
public class RdsIamAuthDataSource extends DataSource {

    @Autowired
    private DatabaseSettings settings;

    private final static String REGION = "us-west-2"; // FIXME Removed hard coded region [KS]

    @Override
    public ConnectionPool createPool() throws SQLException {
        return pool != null ? pool : createPoolImpl();
    }

    protected synchronized ConnectionPool createPoolImpl() throws SQLException {
        PoolConfiguration poolProperties = new PoolProperties();
        poolProperties.setUrl(settings.getUrl());
        poolProperties.setUsername(settings.getUser());
        poolProperties.setDriverClassName(settings.getDriverName());
        return pool = new RdsIamAuthConnectionPool(poolProperties);
    }

    public static class RdsIamAuthConnectionPool extends ConnectionPool implements Runnable {

        private RdsIamAuthTokenGenerator rdsIamAuthTokenGenerator;
        private String host;
        private int port;
        private String username;
        private Thread tokenThread;

        public RdsIamAuthConnectionPool(PoolConfiguration prop) throws SQLException {
            super(prop);
        }

        @Override
        protected void init(PoolConfiguration prop) throws SQLException {
            try {
                URI uri = new URI(prop.getUrl().substring(5));
                this.host = uri.getHost();
                this.port = uri.getPort();
                this.username = prop.getUsername();
                this.rdsIamAuthTokenGenerator = getRdsIamAuthTokenGenerator();

                updatePassword(prop);

                super.init(prop);
                this.tokenThread = new Thread(this, "RdsIamAuthDataSourceTokenThread");
                this.tokenThread.setDaemon(true);
                this.tokenThread.start();

            } catch (URISyntaxException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                while (this.tokenThread != null) {
                    ThreadUtil.sleep(10);
                    updatePassword(getPoolProperties());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        @Override
        protected void close(boolean force) {
            super.close(force);
            Thread t = tokenThread;
            tokenThread = null;
            if (t != null) {
                t.interrupt();
            }
        }

        private RdsIamAuthTokenGenerator getRdsIamAuthTokenGenerator() {
            return RdsIamAuthTokenGenerator.builder().credentials(new DefaultAWSCredentialsProviderChain())
                    .region(REGION).build();
        }

        private void updatePassword(PoolConfiguration props) {
            GetIamAuthTokenRequest request = GetIamAuthTokenRequest.builder().hostname(host).port(port)
                    .userName(this.username).build();
            String token = rdsIamAuthTokenGenerator.getAuthToken(request);
            props.setPassword(token);
        }
    }
}