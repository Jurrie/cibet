/*
 *******************************************************************************
 * L O G I T A G S
 * Software and Programming
 * Dr. Wolfgang Winter
 * Germany
 *
 * All rights reserved
 *
 *******************************************************************************
 */
package com.logitags.cibet.it;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cibethelper.base.CibetTestDataSource;
import com.cibethelper.base.CoreTestBase;
import com.cibethelper.base.DBHelper;
import com.cibethelper.ejb.JdbcEjb;
import com.cibethelper.entities.AbstractTEntity;
import com.cibethelper.entities.ITComplexEntity;
import com.cibethelper.entities.TCompareEntity;
import com.cibethelper.entities.TComplexEntity;
import com.cibethelper.entities.TComplexEntity2;
import com.cibethelper.entities.TEntity;
import com.logitags.cibet.actuator.archive.Archive;
import com.logitags.cibet.actuator.archive.ArchiveActuator;
import com.logitags.cibet.actuator.archive.ArchiveLoader;
import com.logitags.cibet.actuator.dc.DcControllable;
import com.logitags.cibet.actuator.dc.DcLoader;
import com.logitags.cibet.actuator.dc.FourEyesActuator;
import com.logitags.cibet.actuator.info.InfoLogActuator;
import com.logitags.cibet.config.Configuration;
import com.logitags.cibet.config.Setpoint;
import com.logitags.cibet.context.Context;
import com.logitags.cibet.core.CibetUtil;
import com.logitags.cibet.core.ControlEvent;
import com.logitags.cibet.sensor.jdbc.bridge.JdbcBridgeEntityManager;

@RunWith(Arquillian.class)
public class JdbcStatementIT extends DBHelper {

   private static Logger log = Logger.getLogger(JdbcStatementIT.class);

   @EJB
   private JdbcEjb ejb;

   private Setpoint sp;

   @Deployment
   public static WebArchive createDeployment() {
      String warName = JdbcStatementIT.class.getSimpleName() + ".war";
      WebArchive archive = ShrinkWrap.create(WebArchive.class, warName);
      archive.setWebXML("it/web2.xml");

      archive.addClasses(AbstractArquillian.class, CoreTestBase.class, AbstractTEntity.class, TEntity.class,
            TComplexEntity.class, TComplexEntity2.class, ITComplexEntity.class, TCompareEntity.class, JdbcEjb.class,
            CibetTestDataSource.class, DBHelper.class);

      File[] cibet = Maven.resolver().loadPomFromFile("pom.xml").resolve("com.logitags:cibet-jpa20").withTransitivity()
            .asFile();
      archive.addAsLibraries(cibet);

      archive.addAsWebInfResource("META-INF/persistence-jdbc-it.xml", "classes/META-INF/persistence.xml");
      archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

      log.debug(archive.toString(true));
      archive.as(ZipExporter.class).exportTo(new File("target/" + warName), true);

      return archive;
   }

   @After
   public void afterJdbcBridgeEntityManagerIntegrationTest() throws Exception {
      // InitializationService.instance().endContext();
      if (sp != null) {
         Configuration.instance().unregisterSetpoint(sp.getId());
      }
   }

   private class TEntityComparator implements Comparator<TEntity> {
      @Override
      public int compare(TEntity o1, TEntity o2) {
         return o1.getCounter() - o2.getCounter();
      }
   }

   private TEntityComparator comparator = new TEntityComparator();

   private ResultSet query(String sql) throws Exception {
      Connection conn = null;
      try {
         DataSource ds = (DataSource) Context.requestScope().getEntityManager().getDelegate();
         conn = ds.getConnection();
         Statement st = conn.createStatement();
         return st.executeQuery(sql);
      } finally {
         if (conn != null) {
            conn.close();
         }
      }
   }

   private void persistAndCheck() throws Exception {
      int count = ejb.executeJdbc("insert into cib_testentity (id, nameValue, counter, userid, owner) "
            + "values (5, 'rosen', 255, 'Klaus', 'Lalla')", false);
      Assert.assertEquals(1, count);
      ResultSet rs = query("select * from cib_testentity");
      Assert.assertTrue(rs.next());
      Assert.assertEquals(5L, rs.getLong(1));

      rs = query("select * from cib_archive order by createDate");
      Assert.assertTrue(rs.next());
      Assert.assertEquals("INSERT", rs.getString("CONTROLEVENT"));
      Assert.assertEquals("5", rs.getString("PRIMARYKEYID"));
      Assert.assertEquals("cib_testentity", rs.getString("TARGETTYPE"));
      Assert.assertEquals("insert into cib_testentity (id, nameValue, counter, userid, owner) "
            + "values (5, 'rosen', 255, 'Klaus', 'Lalla')", CibetUtil.decode(rs.getBytes("TARGET")));
   }

   @Test
   public void persistWithArchiveAndInfoLogJdbc() throws Exception {
      log.info("start persistWithArchiveAndInfoLogJdbc()");

      List<String> schemes = new ArrayList<String>();
      schemes.add(ArchiveActuator.DEFAULTNAME);
      schemes.add(InfoLogActuator.DEFAULTNAME);
      sp = registerSetpoint("cib_testentity", schemes, ControlEvent.INSERT, ControlEvent.UPDATE, ControlEvent.DELETE);

      persistAndCheck();
   }

   // @Test
   public void persistWith4EyesJdbc() throws Exception {
      log.info("start persistWith4EyesJdbc()");
      List<String> schemes = new ArrayList<String>();
      schemes.add(ArchiveActuator.DEFAULTNAME);
      schemes.add(FourEyesActuator.DEFAULTNAME);
      sp = registerSetpoint("cib_testentity", schemes, ControlEvent.INSERT, ControlEvent.UPDATE, ControlEvent.DELETE);

      int count = ejb.executeJdbc("insert into cib_testentity (id, nameValue, counter, userid, owner) "
            + "values (5, 'rosen', 255, 'Klaus', 'Lalla')", false);
      Assert.assertEquals(0, count);

      TEntity selEnt = applEman.find(TEntity.class, 5);
      Assert.assertNull(selEnt);
      List<Archive> list = ArchiveLoader.loadArchives();
      Assert.assertEquals(1, list.size());
      Assert.assertEquals(ControlEvent.INSERT, list.get(0).getControlEvent());
      Assert.assertEquals("5", list.get(0).getResource().getPrimaryKeyId());

      List<DcControllable> list1 = DcLoader.findUnreleased();
      Assert.assertEquals(1, list1.size());
      Assert.assertEquals(ControlEvent.INSERT, list1.get(0).getControlEvent());
   }

   // @Test
   public void persistRollbackJdbc() throws Exception {
      log.info("start persistRollbackJdbc()");

      List<String> schemes = new ArrayList<String>();
      schemes.add(ArchiveActuator.DEFAULTNAME);
      schemes.add(FourEyesActuator.DEFAULTNAME);
      sp = registerSetpoint("cib_testentity", schemes, ControlEvent.INSERT, ControlEvent.UPDATE, ControlEvent.DELETE);

      ejb.executeJdbc("insert into cib_testentity (id, nameValue, counter, userid, owner) "
            + "values (5, 'rosen', 255, 'Klaus', 'Lalla')", true);

      TEntity selEnt = applEman.find(TEntity.class, 5);
      Assert.assertNull(selEnt);
      List<Archive> list = ArchiveLoader.loadArchives();
      Assert.assertEquals(0, list.size());

      List<DcControllable> list1 = DcLoader.findUnreleased();
      Assert.assertEquals(0, list1.size());
   }

   // @Test
   public void releasePersistJdbc() throws Exception {
      log.info("start releasePersistJdbc()");

      List<String> schemes = new ArrayList<String>();
      schemes.add(FourEyesActuator.DEFAULTNAME);
      schemes.add(ArchiveActuator.DEFAULTNAME);
      sp = registerSetpoint("cib_testentity", schemes, ControlEvent.INSERT, ControlEvent.RELEASE);

      int count = ejb.executeJdbc("insert into cib_testentity (id, nameValue, counter, userid, owner) "
            + "values (5, 'rosen', 255, 'Klaus', 'Lalla')", false);
      Assert.assertEquals(0, count);

      List<DcControllable> l = DcLoader.findUnreleased();
      Assert.assertEquals(1, l.size());

      Context.sessionScope().setUser("test2");
      DataSource dataSource = new CibetTestDataSource();
      Connection con = dataSource.getConnection();
      l.get(0).release(new JdbcBridgeEntityManager(con), null);

      Context.sessionScope().setUser(USER);
      l = DcLoader.findUnreleased();
      Assert.assertEquals(0, l.size());

      List<Archive> list = ArchiveLoader.loadArchives();
      Assert.assertEquals(2, list.size());
      Assert.assertEquals(list.get(0).getResource().getPrimaryKeyId(), list.get(1).getResource().getPrimaryKeyId());

      TEntity te = applEman.find(TEntity.class, Long.parseLong(list.get(0).getResource().getPrimaryKeyId()));
      Assert.assertNotNull(te);
   }

   // @Test
   public void releaseUpdateJdbc() throws Exception {
      log.info("start releaseUpdateJdbc()");

      List<String> schemes = new ArrayList<String>();
      schemes.add(FourEyesActuator.DEFAULTNAME);
      schemes.add(ArchiveActuator.DEFAULTNAME);
      sp = registerSetpoint("cib_testentity", schemes, ControlEvent.UPDATE);

      int count = ejb.executeJdbc("insert into cib_testentity (id, nameValue, counter, userid, owner) "
            + "values (5, 'rosen', 255, 'Klaus', 'Lalla')", false);
      Assert.assertEquals(1, count);

      ResultSet rs = query("select * from cib_testentity");
      Assert.assertTrue(rs.next());

      count = ejb.executeJdbc("update cib_testentity set nameValue='L�b', counter=612 where id=5", false);
      Assert.assertEquals(0, count);

      rs = query("select * from cib_testentity");
      Assert.assertTrue(rs.next());
      Assert.assertEquals("rosen", rs.getString(2));
      Assert.assertEquals(255, rs.getInt(3));

      List<DcControllable> l1 = DcLoader.findUnreleased();
      Assert.assertEquals(1, l1.size());

      Context.sessionScope().setUser("tester2");
      DataSource dataSource = new CibetTestDataSource();
      Connection con = dataSource.getConnection();
      l1.get(0).release(new JdbcBridgeEntityManager(con), null);

      l1 = DcLoader.findUnreleased();
      Assert.assertEquals(0, l1.size());

      rs = query("select * from cib_testentity");
      Assert.assertTrue(rs.next());
      Assert.assertEquals("L�b", rs.getString(2));
      Assert.assertEquals(612, rs.getInt(3));
   }

}