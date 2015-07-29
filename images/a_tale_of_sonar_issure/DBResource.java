package example.util;

import com.google.common.base.Optional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class DBResource {

    private Object resource;

    private DBResource(Object resource) {
        this.resource = resource;
    }

    static DBResource DBResource(Connection connection) {
        return new DBResource(connection);
    }

    static DBResource DBResource(Statement statement) {
        return new DBResource(statement);
    }

    static DBResource DBResource(ResultSet resultSet) {
        return new DBResource(resultSet);
    }

    public void close() throws SQLException {
        try {
            Optional<Method> optionalClose = getCloseMethod();

            if (optionalClose.isPresent()) {
                optionalClose.get().invoke(resource);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<Method> getCloseMethod() {
        if (resource == null) {
            return Optional.absent();
        }
        try {
            return Optional.of(resource.getClass().getDeclaredMethod("close"));
        } catch (NoSuchMethodException e) {
            return Optional.absent();
        }
    }
}