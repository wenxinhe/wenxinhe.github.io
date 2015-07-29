package example.util;

import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static example.util.DBResource.DBResource;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DBResourceTest {
    @Test
    public void should_close_Connection_OK() throws Exception {
        // given
        Connection connection = mock(Connection.class);

        // when
        DBResource(connection).close();

        // then
        verify(connection).close();
    }

    @Test
    public void should_close_null_Connection_OK() throws Exception {
        // given
        Connection connection = null;

        // when
        DBResource(connection).close();

        // then
        assertTrue("close OK", true);
    }

    @Test
    public void should_close_Statement_OK() throws Exception {
        // given
        Statement statement = mock(Statement.class);

        // when
        DBResource(statement).close();

        // then
        verify(statement).close();
    }

    @Test
    public void should_close_null_Statement_OK() throws Exception {
        // given
        Statement statement = null;

        // when
        DBResource(statement).close();

        // then
        assertTrue("close OK", true);
    }

    @Test
    public void should_close_ResultSet_OK() throws Exception {
        // given
        ResultSet resultSet = mock(ResultSet.class);

        // when
        DBResource(resultSet).close();

        // then
        verify(resultSet).close();
    }

    @Test
    public void should_close_null_ResultSet_OK() throws Exception {
        // given
        ResultSet resultSet = null;

        // when
        DBResource(resultSet).close();

        // then
        assertTrue("close OK", true);
    }
}