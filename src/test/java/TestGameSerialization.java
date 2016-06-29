import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jboss.rhiot.scoreboard.GameScore;

public class TestGameSerialization {
    public static void main(String[] args) throws IOException {
        File cwd = new File(".");
        System.out.printf("CWD=%s\n", cwd.getAbsolutePath());

        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        SessionFactory sessionFactory = null;
        try {
            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            StandardServiceRegistryBuilder.destroy( registry );
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf("Saving game score...");
        GameScore gs = new GameScore("SMS", "B0:B4:48:D6:DA:85", 0, new Date(), 8, 5800);
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save( gs );
        session.getTransaction().commit();
        System.out.printf("done\n");

        session.beginTransaction();
        List<GameScore> result = session.createQuery( "from GameScore" ).list();
        for ( GameScore game : result ) {
            System.out.println(game);
        }
        session.getTransaction().commit();
        session.close();

        sessionFactory.close();
    }
}
