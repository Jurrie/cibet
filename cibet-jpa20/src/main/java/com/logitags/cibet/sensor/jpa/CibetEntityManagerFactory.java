package com.logitags.cibet.sensor.jpa;

import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CibetEntityManagerFactory implements EntityManagerFactory {

   private static Log log = LogFactory.getLog(CibetEntityManagerFactory.class);

   private EntityManagerFactory nativeEntityManagerFactory;

   private boolean loadEager;

   public CibetEntityManagerFactory(EntityManagerFactory nativeEMF, boolean lEager) {
      if (nativeEMF == null) {
         throw new IllegalArgumentException("entityManagerFactory may not be null");
      }
      nativeEntityManagerFactory = nativeEMF;
      loadEager = lEager;
   }

   @Override
   public void close() {
      nativeEntityManagerFactory.close();
   }

   @Override
   public EntityManager createEntityManager() {
      EntityManager em = nativeEntityManagerFactory.createEntityManager();
      log.debug("create new CibetEntityManager with native " + em);
      return new CibetEntityManager(this, em, loadEager);
   }

   @Override
   public EntityManager createEntityManager(Map arg0) {
      EntityManager em = nativeEntityManagerFactory.createEntityManager(arg0);
      log.debug("create new CibetEntityManager with native " + em);
      return new CibetEntityManager(this, em, loadEager);
   }

   @Override
   public boolean isOpen() {
      return nativeEntityManagerFactory.isOpen();
   }

   @Override
   public Cache getCache() {
      return nativeEntityManagerFactory.getCache();
   }

   @Override
   public CriteriaBuilder getCriteriaBuilder() {
      return nativeEntityManagerFactory.getCriteriaBuilder();
   }

   @Override
   public Metamodel getMetamodel() {
      return nativeEntityManagerFactory.getMetamodel();
   }

   @Override
   public PersistenceUnitUtil getPersistenceUnitUtil() {
      return nativeEntityManagerFactory.getPersistenceUnitUtil();
   }

   @Override
   public Map<String, Object> getProperties() {
      return nativeEntityManagerFactory.getProperties();
   }

   /**
    * @return the nativeEntityManagerFactory
    */
   public EntityManagerFactory getNativeEntityManagerFactory() {
      return nativeEntityManagerFactory;
   }
}