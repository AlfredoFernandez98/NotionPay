package dat.daos.impl;

import dat.daos.IDAO;
import dat.entities.Customer;
import dat.entities.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SessionDAO implements IDAO<Session> {

    private static SessionDAO instance;
    private static EntityManagerFactory emf;

    public static SessionDAO getInstance(EntityManagerFactory _emf){

        if(instance == null){
            emf = _emf;
            instance= new SessionDAO();
        }
        return instance;

    }

    @Override
    public Session create(Session session) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(session);
            em.getTransaction().commit();
            return session;
        }
    }

    @Override
    public Optional<Session> getById(Long id) {
       try(EntityManager em = emf.createEntityManager()){
           Session session = em.find(Session.class, id);
           return Optional.ofNullable(session);
       }
    }

    @Override
    public Set<Session> getAll() {

        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("SELECT s FROM Session s", Session.class )
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public void update(Session session) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.merge(session);
            em.getTransaction().commit();
        }

    }

    @Override
    public void delete(Long id) {
        try(EntityManager em = emf.createEntityManager()){
            Session session = em.find(Session.class, id);
            em.getTransaction().begin();
            em.remove(session);
            em.getTransaction().commit();
        }

    }

    @Override
    public Optional<Session> findByName(String name) {
        try(EntityManager em = emf.createEntityManager()){
            Session session = em.find(Session.class, name);
            return Optional.ofNullable(session);
        }
    }

    // Need to add more methods for the Séssion entity
}
