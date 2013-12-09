package org.openprovenance.prov.sql;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.openprovenance.prov.model.ProvUtilities;

public class PersistenceUtility {
    static Logger logger = Logger.getLogger(PersistenceUtility.class);
    static private EntityManagerFactory emf;                                                                                                                   
    static private EntityManager entityManager;      

    public PersistenceUtility() {
       
    }
    
    public void setUp() {
	if (emf==null) emf=createEntityManagerFactory();  
	if (entityManager==null) entityManager=createEntityManager();  
	
	//System.out.println("**** merging IdentifierManagement");
        //entityManager.persist(IdentifierManagement.it);
	//entityManager.merge(IdentifierManagement.it);
	
	//entityManager.refresh(IdentifierManagement.it);
	//IdentifierManagement.it=entityManager.find(IdentifierManagement.class, 1);
    }


    protected EntityManagerFactory createEntityManagerFactory() {
         
             try {
               final Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/persistence.xml");
               while (resources.hasMoreElements()) {
                 final URL resource = resources.nextElement();
                 //logger.debug("Detected [" + resource + "].");
               }
         
             }
             catch (IOException ignored) {
         
             }
         
             final Properties properties = getEntityManagerFactoryProperties();
         
             if (properties == null) {
               return javax.persistence.Persistence.createEntityManagerFactory(getPersistenceUnitName());
             }
             else {
               return javax.persistence.Persistence.createEntityManagerFactory(getPersistenceUnitName(), properties);
             }
           }
         
    

    public Properties getEntityManagerFactoryProperties() {

        try {
            final Enumeration<URL> resources = getClass().getClassLoader().getResources(getEntityManagerFactoryPropertiesResourceName());
            
            if (!resources.hasMoreElements()) {
                logger.debug("Entity manager factory properties are not set.");
                return null;
                
            }
            else {
                //logger.debug("Loading entity manager factory properties.");
                final Properties properties = new Properties();
                while (resources.hasMoreElements()) {
                    final URL resource = resources.nextElement();
                    //logger.debug("Loading entity manager factory properties from [" + resource + "].");
                    
                    if (resource == null) {
                        return null;
                    }
                    else {
                        InputStream is = null;
                        try {
                            is = resource.openStream();
                            properties.load(is);
                            return properties;
                        }
                        catch (IOException ex) {
                            return null;
                        }
                        finally {
                            if (is != null) {
                                try {
                                    is.close();
                                }
                                catch (IOException ex) {
                                    // Ignore
                                }
                            }
                        }
                    }
                }
                return properties;
            }
        }
        catch (IOException ex) {
            return null;
        }
    }


    public String getEntityManagerFactoryPropertiesResourceName() {
        return "persistence.properties";
    }

             
    String getPersistenceUnitName() {
        return "org.openprovenance.prov.sql";
    }

    
    public EntityManager createEntityManager () {                                                                                                                                        
        EntityManager em=emf.createEntityManager();                                                                                                                                      
        return em;                                                                                                                                                                       
    }                                                                                                                                                                                    
          
    /*
    static Map<String,String> makeProperties(String filename) {                                                                                                                          
        Map<String,String> properties=new HashMap<String, String>();                                                                                                                     
        properties.put("hibernate.ejb.cfgfile",filename);                                                                                                                                
        properties.put("javax.persistence.provider","org.hibernate.ejb.HibernatePersistence");                                                                                           
        logger.debug(properties);                                                                                                                                                        
        return properties;                                                                                                                                                               
    }                                                                                                                                                                                    
   */
    
    public EntityTransaction getTransaction() {                                             
        return entityManager.getTransaction();                                              
    }
                                                                           
    
    public EntityTransaction beginTransaction() {                                                                                                                                        
        EntityTransaction tx=entityManager.getTransaction();                                                                                                                             
        tx.begin();                                                                                                                                                                      
        return tx;                                                                                                                                                                       
    }                                                                                                                                                                                    
    public EntityTransaction commitTransaction() {                                                                                                                                        
        EntityTransaction tx=entityManager.getTransaction();                                                                                                                             
        tx.commit();                                                                                                                                                                      
        return tx;                                                                                                                                                                       
    }                                                                                                                                                                                    
             
    public void close() {                                                                                                                                                                
        entityManager.close();                                                                                                                                                           
    }          
    
    

    public Document persist(Document doc) {
	try {
            beginTransaction();
            Dagify dagifier=new Dagify(entityManager);
            ProvUtilities u=new ProvUtilities();
            u.forAllStatementOrBundle(doc.getStatementOrBundle(), dagifier);
            entityManager.persist(doc);
            
            return doc;
	} catch (RuntimeException re) {
	    re.printStackTrace();
	    return null;
	}
	finally {
            commitTransaction();
	}
     
    }            
    
    
    public <DOC> DOC find(Class<DOC> cl, Long id) {
        beginTransaction();
        DOC doc=entityManager.find(cl, id);
        commitTransaction();
        return doc;
    }

    public Query createQuery(String q) {
	return entityManager.createQuery(q);
    }            
    
   

    

}
