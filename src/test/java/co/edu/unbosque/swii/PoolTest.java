/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.edu.unbosque.swii;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.pool2.BaseObjectPool;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.testng.annotations.Test;

/**
 *
 * @author Alejandro
 */
public class PoolTest {

    public static final String pwd = "YckGwYC8r3";
    FabricaConexiones fcClass = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6_5", pwd);
    ObjectPool<Connection> poolClass = new GenericObjectPool<Connection>(fcClass);

    @Test(expectedExceptions = org.postgresql.util.PSQLException.class,
            expectedExceptionsMessageRegExp = ".*too many connections.*"
    )
    public void soloDebeCrear5Conexiones() throws Exception {
        FabricaConexiones fc = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6_5", pwd);
        ObjectPool<Connection> pool = new GenericObjectPool<Connection>(fc);
        for (int i = 0; i < 6; i++) {
            pool.borrowObject();
        }
    }

    @Test
    public void aprendiendoAControlarLasConexiones() throws Exception {
        FabricaConexiones fc = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6", pwd);
        ObjectPool<Connection> pool = new GenericObjectPool<Connection>(fc);
        for (int i = 0; i < 6; i++) {
            Connection c = pool.borrowObject();
            pool.returnObject(c);
        }
    }

    @Test(expectedExceptions = org.postgresql.util.PSQLException.class,
            expectedExceptionsMessageRegExp = ".*connection has been closed.*"
    )
    public void quePasaCuandoSeCierraUnaConexionAntesDeRetornarla() throws Exception {
        FabricaConexiones fc = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6", pwd);
        ObjectPool<Connection> pool = new GenericObjectPool<Connection>(fc);
        Connection c = pool.borrowObject();
        c.close();
        pool.returnObject(c);
        c.createStatement().executeQuery("SELECT 1");
    }

    @Test
    public void quePasaCuandoSeRetornaUnaconexionContransaccionIniciada() throws Exception {
        FabricaConexiones fc = new FabricaConexiones("aretico.com", 5432, "software_2", "grupo6", pwd);
        ObjectPool<Connection> pool = new GenericObjectPool<Connection>(fc);
        Connection c = pool.borrowObject();
        c.setAutoCommit(false);
        PreparedStatement pst = null;
        pst = c.prepareStatement("UPDATE tbl_hilos set hilo='Batch-2.1' WHERE hilo = 'Batch-2' and registro=86");
        pst.execute();
        pool.returnObject(c);

    }

    @Test(threadPoolSize = 5, invocationCount = 5)
    public void midaTiemposParaInsertar1000RegistrosConSingleton() throws ClassNotFoundException, SQLException {
        Connection conn = SingletonConnection.getConnection();
        PreparedStatement pst = null;
        String query = "INSERT INTO tbl_test(id_hilo,nombre) VALUES(?,?)";
        pst = conn.prepareStatement(query);
        long time_start, time_end;
        time_start = System.currentTimeMillis();
        for (int i = 0; i <= 1000; i++) {
            pst.setString(1, Thread.currentThread().getName());
            pst.setString(2, "Singleton");
            pst.execute();
        }
        time_end = System.currentTimeMillis();
        System.out.println("the task has taken " + (time_end - time_start) + " milliseconds");
        //conn.close();
    }

    @Test(threadPoolSize = 5, invocationCount = 5)
    public void midaTiemposParaInsertar1000RegistrosConObjectPool() throws Exception {
        Connection c = poolClass.borrowObject();
        PreparedStatement pst = null;
        String query = "INSERT INTO tbl_test(id_hilo,nombre) VALUES(?,?)";
        pst = c.prepareStatement(query);
        long time_start, time_end;
        time_start = System.currentTimeMillis();
        for (int i = 1; i <= 1000; i++) {
            pst.setString(1, Thread.currentThread().getName());
            pst.setString(2, Thread.currentThread().getName() + "-" + i);
            pst.execute();
        }
        time_end = System.currentTimeMillis();
        System.out.println("the task has taken " + (time_end - time_start) + " milliseconds");
        poolClass.returnObject(c);
    }

}
