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
package com.logitags.cibet.tutorial;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.logitags.cibet.actuator.archive.Archive;
import com.logitags.cibet.actuator.archive.ArchiveLoader;
import com.logitags.cibet.actuator.dc.DcControllable;
import com.logitags.cibet.actuator.lock.LockedObject;
import com.logitags.cibet.context.Context;
import com.logitags.cibet.core.EventResult;

public class TutorialServlet1 extends HttpServlet {

   private static Logger log = Logger.getLogger(TutorialServlet1.class);
   /**
    * 
    */
   private static final long serialVersionUID = -2876846209111174152L;

   @Resource
   private UserTransaction ut;

   @PersistenceContext(unitName = "APPL-UNIT")
   private EntityManager applEman;

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest ,
    * javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      doPost(req, resp);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest ,
    * javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      log.info("------------- call " + req.getRequestURI() + " ---------------");

      try {
         if (req.getRequestURI().endsWith("persist")) {
            persist(req, resp);
         } else if (req.getRequestURI().endsWith("loadPersonArchive")) {
            loadPersonArchive(req, resp);
         } else if (req.getRequestURI().endsWith("clean")) {
            clean(req, resp);
         } else {
            String msg = "ERROR: no functionality found!!";
            PrintWriter writer = resp.getWriter();
            writer.print("ee done: " + msg);
            writer.close();
         }

      } catch (Exception e) {
         log.error(e.getMessage(), e);
         throw new ServletException(e);
      }
   }

   private void clean(HttpServletRequest req, HttpServletResponse resp) throws Exception {
      log.debug("TutorialServlet:clean()");

      ut.begin();

      Query q3 = Context.requestScope().getEntityManager().createNamedQuery(Archive.SEL_ALL);
      List<Archive> alist = q3.getResultList();
      for (Archive ar : alist) {
         Context.requestScope().getEntityManager().remove(ar);
      }

      Query q4 = Context.requestScope().getEntityManager().createQuery("select d from DcControllable d");
      List<DcControllable> dclist = q4.getResultList();
      for (DcControllable dc : dclist) {
         Context.requestScope().getEntityManager().remove(dc);
      }

      Query q5 = Context.requestScope().getEntityManager().createQuery("SELECT a FROM LockedObject a");
      Iterator<LockedObject> itLO = q5.getResultList().iterator();
      while (itLO.hasNext()) {
         Context.requestScope().getEntityManager().remove(itLO.next());
      }

      Query q6 = Context.requestScope().getEntityManager().createQuery("SELECT a FROM EventResult a");
      Iterator<EventResult> itEV = q6.getResultList().iterator();
      while (itEV.hasNext()) {
         Context.requestScope().getEntityManager().remove(itEV.next());
      }

      ut.commit();
   }

   private void persist(HttpServletRequest req, HttpServletResponse resp) throws Exception {
      Person person = new Person("Walter");
      person.getAddresses().add(new Address("Hamburg"));
      person.getAddresses().add(new Address("Aachen"));

      ut.begin();
      applEman.persist(person);
      ut.commit();
      String info = "Person persisted with id " + person.getPersonId();
      log.info(info);
      PrintWriter writer = resp.getWriter();
      writer.print(person.getPersonId());
      writer.close();
   }

   private void loadPersonArchive(HttpServletRequest req, HttpServletResponse resp) throws Exception {
      String id = req.getParameter("id");
      int expected = Integer.valueOf(req.getParameter("expected"));
      List<Archive> archives = ArchiveLoader.loadArchivesByPrimaryKeyId(Person.class.getName(), id);
      if (archives.size() != expected) {
         throw new Exception("Archive list size is not " + expected + " but " + archives.size());
      }

      String response = "no Person Archive found";
      if (expected > 0) {
         Person person = (Person) archives.get(0).getResource().getObject();
         response = person.toString().replaceAll("\n", " ; ");
         log.info(person);
      }
      PrintWriter writer = resp.getWriter();
      writer.print(response);
      writer.close();
   }

}
