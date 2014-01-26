package org.hifly.geomapviewer;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import junit.framework.Assert;
import org.hifly.geomapviewer.domain.Author;
import org.hifly.geomapviewer.domain.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author
 * @date 26/01/14
 */
public class OrmLite {

    Logger log = LoggerFactory.getLogger(OrmLite.class);

    @org.junit.Test
    public void testOrmLite() throws Exception {
        JdbcConnectionSource connectionSource =
                new JdbcPooledConnectionSource("jdbc:h2:~/test");
        connectionSource.setUsername("sa");
        connectionSource.setPassword("");
        try {
            // work with the data-source and DAOs
            Dao<Author, String> authorDao =
                    DaoManager.createDao(connectionSource, Author.class);
            Dao<Track, String> trackDao =
                    DaoManager.createDao(connectionSource, Track.class);
            TableUtils.dropTable(connectionSource, Author.class, true);
            TableUtils.dropTable(connectionSource, Track.class, true);
            TableUtils.createTableIfNotExists(connectionSource, Author.class);
            TableUtils.createTableIfNotExists(connectionSource, Track.class);

            Author author = new Author();
            author.setId(1);
            author.setName("Pluto");
            author.setEmail("test@test.com");

            authorDao.create(author);

            Track track = new Track();
            track.setId(1);
            track.setName("Test track");
            track.setStartDate(new Date());
            track.setEndDate(new Date());
            track.setAuthor(author);

            trackDao.create(track);

            Author authorFromDb = authorDao.queryForId(String.valueOf(1));
            Track trackFromDb = trackDao.queryForId(String.valueOf(1));
            authorDao.refresh(trackFromDb.getAuthor());

            Assert.assertNotNull(authorFromDb);
            Assert.assertNotNull(trackFromDb);
            Assert.assertNotNull(authorFromDb.getTracks());
            Assert.assertEquals(authorFromDb.getTracks().size(),1);
            Assert.assertNotNull(trackFromDb.getAuthor());
            Assert.assertNotNull(trackFromDb.getAuthor().getName());
        }
        finally {
            connectionSource.close();
        }

    }


}
