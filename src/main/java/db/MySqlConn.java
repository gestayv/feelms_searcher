/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ichigo
 */
public class MySqlConn 
{
    DataSource dataSource = null;
    
    public MySqlConn(String user, String pass, String host, String port, String db_name)
    {
        boolean valid = true;

        //Intenta obtener el datasource de glassfish (experimental)
        try {
            Context context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/myDB");
        } catch (NamingException e) {
            System.out.print(e.toString() + "\n");

            valid = false;
        }

        //Si no pudo, se conecta usando los datos entregados
        if(!valid) {
            System.out.print("\nCreando conexion manualmente\n\n");
            MysqlDataSource ds = new MysqlDataSource();
            ds.setUser(user);
            ds.setPassword(pass);
            ds.setUrl("jdbc:mysql://"+host+":"+port+"/"+db_name+"?serverTimezone=UTC");

            this.dataSource = ds;
        } else {
            System.out.print("\nConectado al DataSource existente\n\n");
        }
    }
    
    public boolean test() {
        Connection conn = null;
        Boolean valid = true;

        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            System.out.print(e.toString() + "\n");
            valid = false;
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.out.print(e.toString() + "\n");
                    valid = false;

                }
            }
        }
        return valid;
    }
    
    public List<Film> getFilms() throws SQLException
    {
        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet rs = null;

        List<Film> films = new ArrayList<Film>();
        
        try {
            conn = dataSource.getConnection();

            stm = conn.prepareStatement("SELECT title, original_title FROM films");
            rs = stm.executeQuery();

            //Empieza a iterar cada elemento de la query
            while(rs.next()) {
                if(rs.getString("original_title") != null && !rs.getString("original_title").isEmpty())
                {
                    Film pelicula = new Film(rs.getString("original_title"), rs.getString("title"));
                    films.add(pelicula);
                }
                else
                {
                    Film pelicula = new Film(rs.getString("title"), rs.getString("title"));
                    films.add(pelicula);
                }
            }

            rs.close();
            stm.close();

        } catch (SQLException e) {
            throw e;
        } finally {
            if(rs != null && !rs.isClosed()) rs.close();

            if(stm != null && !stm.isClosed()) stm.close();

            if(conn != null && !conn.isClosed()) conn.close();
        }

        
        return films;
    }
}
