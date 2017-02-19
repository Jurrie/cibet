/*
 *******************************************************************************
 * L O G I T A G S
 * Software and Programming
 * Dr. Wolfgang Winter
 * Germany
 *
 * All rights reserved
 *
 * Copyright 2016 Dr. Wolfgang Winter
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
 *******************************************************************************
 */
package com.logitags.cibet.sensor.jdbc.bridge;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class DefaultDataSource implements DataSource {

   private String url;
   private String user;
   private String password;

   public DefaultDataSource(String url, String user, String password) {
      this.url = url;
      this.user = user;
      this.password = password;
   }

   @Override
   public PrintWriter getLogWriter() throws SQLException {
      return null;
   }

   @Override
   public void setLogWriter(PrintWriter out) throws SQLException {
   }

   @Override
   public void setLoginTimeout(int seconds) throws SQLException {
   }

   @Override
   public int getLoginTimeout() throws SQLException {
      return 0;
   }

   @Override
   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      return null;
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
   }

   @Override
   public Connection getConnection() throws SQLException {
      return DriverManager.getConnection(url, user, password);
   }

   @Override
   public Connection getConnection(String username, String password) throws SQLException {
      return null;
   }

}
